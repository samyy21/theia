package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.exception.BizSubventionOfferCheckoutException;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.facade.emisubvention.utils.SubventionUtils;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import com.paytm.pgplus.common.config.ConfigurationUtil;

import java.util.Iterator;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.SUBVENTION_ORDERSTAMP_KEY;

@Component
public class SimplifiedSubventionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedSubventionHelper.class);
    private static final String SUBVENTION = "SUBVENTION";
    private static final String SUB_RULE_SPLITTER = ",";

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private ISubventionEmiService subventionEmiService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public void validateSimplifiedEmi(WorkFlowTransactionBean workFlowTransactionBean) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        if (workFlowRequestBean.getEmiSubventionValidateRequestData() != null
                && workFlowRequestBean.isProcessSimplifiedEmi() && !subventionAlreadyOrderStamped(workFlowRequestBean)) {
            updateItemPrices(workFlowRequestBean.getEmiSubventionValidateRequestData(),
                    Double.parseDouble(workFlowRequestBean.getTxnAmount()));
            ValidateRequest validateRequest = workFlowRequestBean.getEmiSubventionValidateRequestData();
            double txnAmountAfterPromoInPaise = Double.parseDouble(workFlowRequestBean.getTxnAmount());
            double txnAmountAfterPromoInRs = AmountUtils.getAmountInRs(txnAmountAfterPromoInPaise);
            // setting current txn amount (in Rs) in validate subvention request
            validateRequest.getPaymentDetails().setTotalTransactionAmount(txnAmountAfterPromoInRs);
            if (CollectionUtils.isNotEmpty(validateRequest.getPaymentDetails().getPaymentOptions())) {
                validateRequest.getPaymentDetails().getPaymentOptions().get(0)
                        .setTransactionAmount(txnAmountAfterPromoInRs);
            }
            try {
                GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse = subventionEmiService
                        .validateSubventionEmi(validateRequest);
                validateSimplifiedSubventionResponse(validateEmiServiceResponse, workFlowRequestBean);
                ValidateResponse validateResponse = validateEmiServiceResponse.getData();
                // setting response data for checkout processing
                workFlowRequestBean.setEmiSubventionOfferCheckoutReqData(validateResponse);
                double discountedGratifiedAmountInRs = validateResponse.getGratifications().stream()
                        .filter(gratification -> GratificationType.DISCOUNT.equals(gratification.getType()))
                        .mapToDouble(Gratification::getValue).sum();
                Map<String, String> items = validateRequest.getItems().stream()
                        .filter(item -> StringUtils.isNotEmpty(item.getBrandId()))
                        .collect(Collectors.toMap(Item::getId, Item::getBrandId));
                discountedGratifiedAmountInRs = updateGratificationAmountForSpecificBanks(validateResponse,
                        discountedGratifiedAmountInRs, items);
                double discountedGratifiedAmountInPaise = AmountUtils.getAmountInPaise(discountedGratifiedAmountInRs);
                if (discountedGratifiedAmountInPaise > 0d) {
                    workFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(String.valueOf(AmountUtils
                            .getAmountInRs(txnAmountAfterPromoInPaise - discountedGratifiedAmountInPaise))));
                    workFlowTransactionBean.setModifyOrderRequired(true);
                    workFlowTransactionBean.setFailTxnIfModifyOrderFails(true);
                }
                LOGGER.info(
                        "updating txn amount : {} Paise -> {} Paise after deducting discountedGratifiedAmount : {} Paise ",
                        txnAmountAfterPromoInPaise, workFlowRequestBean.getTxnAmount(),
                        discountedGratifiedAmountInPaise);
                // For EMI Subvention All-in-one SDK flow, validate txn amount
                // from v1/ptc request's finalTxnAmount that is evaluated by
                // validateEMI call on cashier page
                if (null != workFlowRequestBean.getEmiSubventionInfo()
                        && null != workFlowRequestBean.getEmiSubventionInfo().getFinalTransactionAmount()) {
                    if (Double
                            .compare(Double.valueOf(AmountUtils.getTransactionAmountInRupee(workFlowRequestBean
                                    .getTxnAmount())), Double.valueOf(workFlowRequestBean.getEmiSubventionInfo()
                                    .getFinalTransactionAmount().getValue())) != 0) {
                        throw new BizSubventionOfferCheckoutException(
                                "Error in validating final transaction amount in Simplified Subvention All in one SDK");
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Error in validating simplified subvention ", e);
                String message = "Error in validating subvention";
                if (e instanceof BizSubventionOfferCheckoutException && StringUtils.isNotBlank(e.getMessage())) {
                    message = e.getMessage();
                }
                throw new BizSubventionOfferCheckoutException(message);
            }
        }
    }

    private Double updateGratificationAmountForSpecificBanks(ValidateResponse validateResponse,
            Double discountedGratifiedAmount, Map<String, String> items) {
        if (discountedGratifiedAmount > 0 && validateResponse.getGratifications().size() == 1) {
            List<ItemBreakUp> itemBreakUpList = validateResponse.getItemBreakUp();
            List<String> brandIdsList = new ArrayList<>();
            Double updatedDiscountedGratifiedAmount = 0.0;
            boolean merchantOnlyContribution = false;
            String dummyBrandIds = ff4jUtils.getPropertyAsStringWithDefault(ff4jUtils.DUMMY_BRAND_LIST, "100");
            if (StringUtils.isNotEmpty(dummyBrandIds)) {
                dummyBrandIds = dummyBrandIds.replaceAll("\\s+", "");
                brandIdsList = Arrays.asList(dummyBrandIds.split(SUB_RULE_SPLITTER));
            }
            LOGGER.info("dummyItems: {}, dummyBrandIds: {}", items, dummyBrandIds);
            if (!CollectionUtils.isEmpty(itemBreakUpList)
                    && ConfigurationUtil.getProperty("MERCHANT_ONLY_CONTRIBUTION_EMI_BANKS", "").contains(
                            validateResponse.getBankCode())) {
                for (ItemBreakUp itemBreakUp : itemBreakUpList) {
                    if (itemBreakUp.getAmountBearer() != null
                            && itemBreakUp.getAmountBearer().getBrand() > 0
                            && !(MapUtils.isNotEmpty(items) && items.containsKey(itemBreakUp.getId())
                                    && null != items.get(itemBreakUp.getId())
                                    && CollectionUtils.isNotEmpty(brandIdsList) && brandIdsList.contains(items
                                    .get(itemBreakUp.getId())))) {
                        updatedDiscountedGratifiedAmount = updatedDiscountedGratifiedAmount
                                + itemBreakUp.getAmountBearer().getMerchant()
                                + itemBreakUp.getAmountBearer().getPlatform();
                        merchantOnlyContribution = true;
                    }
                }
            }
            if (merchantOnlyContribution) {
                discountedGratifiedAmount = updatedDiscountedGratifiedAmount;
            }
        }
        return discountedGratifiedAmount;
    }

    public void updateItemPrices(ValidateRequest validateRequest, double txnAmountAfterPromoInPaise) {
        double txnAmountBeforePromoInPaise = AmountUtils.getAmountInPaise(validateRequest.getPaymentDetails()
                .getTotalTransactionAmount());
        double promoDiscountInPaise = txnAmountBeforePromoInPaise - txnAmountAfterPromoInPaise;
        boolean isAmountBasedSubvention = SubventionUtils.isAmountBasedSubventionRequest(validateRequest);
        if (promoDiscountInPaise > 0d) {
            Iterator<Item> itemIterator = validateRequest.getItems().iterator();
            double sumOfItemPrice = 0d;
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                double itemPriceInPaise = AmountUtils.getAmountInPaise(item.getPrice());
                if (itemIterator.hasNext()) {
                    double partOfDiscountToBeDeductedInPaise = (itemPriceInPaise / txnAmountBeforePromoInPaise)
                            * promoDiscountInPaise;
                    double updatedItemPriceInPaise = itemPriceInPaise - partOfDiscountToBeDeductedInPaise;
                    item.setPrice(AmountUtils.getAmountInRs(updatedItemPriceInPaise));
                    item.setListingPrice(AmountUtils.getAmountInRs(updatedItemPriceInPaise / item.getQuantity()));
                    sumOfItemPrice += updatedItemPriceInPaise;
                } else {
                    // setting the price of last item as totalAmount -
                    // sumOfItemPrice till now, in order to avoid mismatch in
                    // total txn amount and item price sum
                    double lastItemPrice = txnAmountAfterPromoInPaise - sumOfItemPrice;
                    item.setPrice(AmountUtils.getAmountInRs(lastItemPrice));
                    item.setListingPrice(AmountUtils.getAmountInRs(lastItemPrice / item.getQuantity()));
                    sumOfItemPrice += lastItemPrice;
                }
                if (isAmountBasedSubvention) {
                    String productId = SubventionUtils.getProductIdForAmountBased(validateRequest.getMid(),
                            item.getPrice(), item.getIsStandardEmi());
                    item.setProductId(productId);
                    item.setId(productId);
                }
            }
        }
    }

    private void validateSimplifiedSubventionResponse(
            GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse,
            WorkFlowRequestBean workFlowRequestBean) throws BizSubventionOfferCheckoutException {
        String pgPlanId = workFlowRequestBean.getPaymentRequestBean().getEmiPlanID();
        if (validateEmiServiceResponse != null && validateEmiServiceResponse.getError() != null) {
            throw new BizSubventionOfferCheckoutException(validateEmiServiceResponse.getError().getMessage());
        }
        if (validateEmiServiceResponse == null || validateEmiServiceResponse.getStatus() == 0
                || validateEmiServiceResponse.getData() == null) {
            throw new BizSubventionOfferCheckoutException();
        }
        if (validateEmiServiceResponse.getData().getEmiType() == null
                || !SUBVENTION.equals(validateEmiServiceResponse.getData().getEmiType().getType())) {
            throw new BizSubventionOfferCheckoutException(
                    "EMI Type in subvention validate response is not 'SUBVENTION'");
        }
        if (!validateEmiServiceResponse.getData().getPgPlanId().equals(pgPlanId)) {
            throw new BizSubventionOfferCheckoutException(
                    "'planId' provided in request does not match with 'pgPlanId' in validate subvention response");
        }
    }

    public boolean subventionAlreadyOrderStamped(WorkFlowRequestBean workFlowRequestBean) {
        String key = SUBVENTION_ORDERSTAMP_KEY + "_" + workFlowRequestBean.getPaytmMID() + "_"
                + workFlowRequestBean.getOrderID();
        return theiaTransactionalRedisUtil.get(key) != null;
    }
}
