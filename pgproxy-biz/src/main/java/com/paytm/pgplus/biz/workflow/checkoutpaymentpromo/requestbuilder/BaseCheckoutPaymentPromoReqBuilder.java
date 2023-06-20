package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.util.CryptoUtils;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.*;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.Item;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.PaymentDetails;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.ApplyPromoResponseDataV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.Items;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.PromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.PromoUsageData;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BaseCheckoutPaymentPromoReqBuilder implements CheckoutPaymentPromoReqBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCheckoutPaymentPromoReqBuilder.class);

    private static final String CONST_ITEM = "item001";

    @Override
    public CheckoutPromoServiceRequest build(WorkFlowRequestBean workFlowRequestBean) {
        validateWorkflowReqForCheckoutPaymentPromo(workFlowRequestBean);
        CheckoutPromoServiceRequest checkoutPromoServiceRequest = new CheckoutPromoServiceRequest();
        checkoutPromoServiceRequest.setValidationResponse(getValidationResponse(workFlowRequestBean
                .getPaymentOfferCheckoutReqData()));
        checkoutPromoServiceRequest.setTotalTransactionAmount(getNonWalletTxnAmountIfHybrid(workFlowRequestBean));
        checkoutPromoServiceRequest.setChannel(workFlowRequestBean.getMerchantRequestedChannelId());
        checkoutPromoServiceRequest.setPaymentOptions(getPaymentOptions(workFlowRequestBean));
        checkoutPromoServiceRequest.setPromocode(checkoutPromoServiceRequest.getValidationResponse().getPromocode());
        return checkoutPromoServiceRequest;
    }

    @Override
    public CheckoutPromoServiceRequestV2 buildV2(WorkFlowRequestBean workFlowRequestBean) {
        validateWorkflowReqForCheckoutPaymentPromo(workFlowRequestBean);
        CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = new CheckoutPromoServiceRequestV2();
        checkoutPromoServiceRequest.setPaymentDetails(getPaymentDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setUser(getUserDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setClientDetails(getClientDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setValidationResponse(getValidationResponseV2(workFlowRequestBean
                .getPaymentOfferCheckoutReqData()));
        getPromoCode(workFlowRequestBean, checkoutPromoServiceRequest);
        getPromoContextData(workFlowRequestBean, checkoutPromoServiceRequest);
        return checkoutPromoServiceRequest;
    }

    @Override
    public CheckoutPromoServiceRequestV2 buildCoftV2(WorkFlowRequestBean workFlowRequestBean) {
        validateWorkflowReqForCheckoutPaymentPromo(workFlowRequestBean);
        CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = new CheckoutPromoServiceRequestV2();
        checkoutPromoServiceRequest.setPaymentDetails(getPaymentDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setUser(getUserDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setClientDetails(getClientDetails(workFlowRequestBean));
        checkoutPromoServiceRequest.setValidationResponse(getValidationResponseV2(workFlowRequestBean
                .getPaymentOfferCheckoutReqData()));
        getPromoCode(workFlowRequestBean, checkoutPromoServiceRequest);
        getPromoContextData(workFlowRequestBean, checkoutPromoServiceRequest);
        return checkoutPromoServiceRequest;
    }

    private void getPromoCode(WorkFlowRequestBean workFlowRequestBean,
            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest) {
        if (workFlowRequestBean.getPaymentOffersAppliedV2() != null
                && workFlowRequestBean.getPaymentOffersAppliedV2().getPromoCode() != null) {
            checkoutPromoServiceRequest.setPromocode(workFlowRequestBean.getPaymentOffersAppliedV2().getPromoCode());
        } else {
            checkoutPromoServiceRequest.setPromocode(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                    .getOfferBreakup().get(0).getPromocodeApplied());

        }
    }

    private void getPromoContextData(WorkFlowRequestBean workFlowRequestBean,
            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest) {
        if (workFlowRequestBean.getPaymentOffersAppliedV2() != null
                && workFlowRequestBean.getPaymentOffersAppliedV2().getVerificationCode() != null) {
            checkoutPromoServiceRequest.setValidationCode(workFlowRequestBean.getPaymentOffersAppliedV2()
                    .getVerificationCode());
            if (workFlowRequestBean.getPaymentOffersAppliedV2().getPromoContext() != null) {
                checkoutPromoServiceRequest.setPromoContext(workFlowRequestBean.getPaymentOffersAppliedV2()
                        .getPromoContext());
                workFlowRequestBean.setPromoContextICB(checkoutPromoServiceRequest.getPromoContext());
            }
        } else {
            checkoutPromoServiceRequest.setCart(getCartDetails(checkoutPromoServiceRequest.getPaymentDetails()
                    .getTotalTransactionAmount()));

        }
    }

    private ClientDetails getClientDetails(WorkFlowRequestBean workFlowRequestBean) {
        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setChannel(workFlowRequestBean.getMerchantRequestedChannelId());
        return clientDetails;
    }

    private Cart getCartDetails(Long totalTransactionAmount) {
        Cart cart = new Cart();
        cart.setItems(getItemDetails(totalTransactionAmount));
        return cart;
    }

    private Map<String, Item> getItemDetails(Long totalTransactionAmount) {
        Map<String, Item> items = new HashMap<>();
        Item item = new Item();
        item.setPrice(Math.toIntExact(totalTransactionAmount));
        items.put(CONST_ITEM, item);
        return items;
    }

    private User getUserDetails(WorkFlowRequestBean workFlowRequestBean) {
        User user = new User();
        user.setId(workFlowRequestBean.getCustID());
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                && StringUtils.isNotBlank(workFlowRequestBean.getPaymentOfferCheckoutReqData().getEncUserId())) {
            try {
                user.setPaytmUserId(CryptoUtils.decryptAES(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                        .getEncUserId()));
            } catch (Exception e) {
                LOGGER.error("Error while decrypting user id corresponding to vpa :{}", e.getMessage());
                throw new BizPaymentOfferCheckoutException("Error while decrypting user id corresponding to vpa");
            }
        } else if (workFlowRequestBean.getUserDetailsBiz() != null
                && workFlowRequestBean.getSimplifiedPaymentOffers() == null) {
            user.setPaytmUserId(workFlowRequestBean.getUserDetailsBiz().getUserId());
        }
        return user;
    }

    private PaymentDetails getPaymentDetails(WorkFlowRequestBean workFlowRequestBean) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentOptions(getPaymentOptions(workFlowRequestBean));
        paymentDetails.setTotalTransactionAmount(getNonWalletTxnAmountIfHybrid(workFlowRequestBean));
        return paymentDetails;
    }

    private ApplyPromoResponseData getValidationResponse(PaymentOffer paymentOffer) {
        PaymentOfferDetails paymentOfferDetails = paymentOffer.getOfferBreakup().get(0);
        ApplyPromoResponseData applyPromoResponseData = new ApplyPromoResponseData();
        applyPromoResponseData.setPromocode(paymentOfferDetails.getPromocodeApplied());
        applyPromoResponseData.setPromotext(paymentOfferDetails.getPromotext());
        applyPromoResponseData.setPromoVisibility(Boolean.parseBoolean(paymentOfferDetails.getPromoVisibility()));
        applyPromoResponseData.setResponseCode(paymentOfferDetails.getResponseCode());
        applyPromoResponseData.setSavings(getPromoSavings(paymentOfferDetails));
        applyPromoResponseData.setStatus(1);
        return applyPromoResponseData;
    }

    private ApplyPromoResponseDataV2 getValidationResponseV2(PaymentOffer paymentOffer) {
        if (paymentOffer == null) {
            return null;
        } else {
            PaymentOfferDetails paymentOfferDetails = paymentOffer.getOfferBreakup().get(0);
            ApplyPromoResponseDataV2 applyPromoResponseData = new ApplyPromoResponseDataV2();
            applyPromoResponseData.setPromotext(paymentOfferDetails.getPromotext());
            applyPromoResponseData.setPromoVisibility(Boolean.parseBoolean(paymentOfferDetails.getPromoVisibility()));
            applyPromoResponseData.setResponseCode(paymentOfferDetails.getResponseCode());
            applyPromoResponseData.setPromoResponse(getPromoResponse(paymentOfferDetails));
            applyPromoResponseData.setStatus(1);
            return applyPromoResponseData;
        }
    }

    private Map<String, PromoResponseData> getPromoResponse(PaymentOfferDetails paymentOfferDetails) {
        Map<String, PromoResponseData> promoResponseDataMap = new HashMap<>();
        PromoResponseData promoResponseData = new PromoResponseData();
        promoResponseData.setItems(getItems(paymentOfferDetails));
        promoResponseDataMap.put(paymentOfferDetails.getPromocodeApplied(), promoResponseData);
        return promoResponseDataMap;
    }

    private Map<String, Items> getItems(PaymentOfferDetails paymentOfferDetails) {
        Map<String, Items> itemsMap = new HashMap<>();
        Items items = new Items();
        items.setUsage_data(getPromoUsageData(paymentOfferDetails));
        itemsMap.put(CONST_ITEM, items);
        return itemsMap;
    }

    private List<PromoUsageData> getPromoUsageData(PaymentOfferDetails paymentOfferDetails) {
        List<PromoUsageData> promoUsageDataList = new ArrayList<>();
        if (StringUtils.isNotBlank(paymentOfferDetails.getCashbackAmount())) {
            PromoUsageData promoUsageData = new PromoUsageData();
            promoUsageData.setRedemptionType("cashback");
            promoUsageData.setAmount(Integer.parseInt(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getCashbackAmount())));
            promoUsageDataList.add(promoUsageData);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getInstantDiscount())) {
            PromoUsageData promoUsageData = new PromoUsageData();
            promoUsageData.setRedemptionType("discount");
            promoUsageData.setAmount(Integer.parseInt(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getInstantDiscount())));
            promoUsageDataList.add(promoUsageData);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getPaytmCashbackAmount())) {
            PromoUsageData promoUsageData = new PromoUsageData();
            promoUsageData.setRedemptionType("paytm_cashback");
            promoUsageData.setAmount(Integer.parseInt(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getPaytmCashbackAmount())));
            promoUsageDataList.add(promoUsageData);
        }
        return promoUsageDataList;
    }

    private long getNonWalletTxnAmountIfHybrid(WorkFlowRequestBean workFlowRequestBean) {
        // promoTxnAmount is here to support promo amount for flipkart
        String promoTxnAmount = (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null && StringUtils
                .isNotBlank(workFlowRequestBean.getPaymentOfferCheckoutReqData().getTotalTransactionAmount())) ? (AmountUtils
                .getTransactionAmountInPaise(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                        .getTotalTransactionAmount()))
                : (StringUtils.isNotBlank(workFlowRequestBean.getPromoAmount())) ? workFlowRequestBean.getPromoAmount()
                        : workFlowRequestBean.getTxnAmount();
        String actualTxnAmount = StringUtils.isNotBlank(workFlowRequestBean.getPromoAmount()) ? workFlowRequestBean
                .getPromoAmount() : workFlowRequestBean.getTxnAmount();

        // For Hybrid we are not changing txn amount
        if (EPayMode.HYBRID == workFlowRequestBean.getPaytmExpressAddOrHybrid()) {
            return Long.parseLong(actualTxnAmount) - Long.parseLong(workFlowRequestBean.getWalletAmount());
        }
        return Long.parseLong(promoTxnAmount);
    }

    // Hybrid txn not supported by promoservice sending only non wallet
    // payMethod if txn is hybrid
    private List<PaymentOption> getPaymentOptions(WorkFlowRequestBean workFlowRequestBean) {
        List<PaymentOption> paymentOptions = new ArrayList<>();
        PayMethod payMethod = null;
        try {
            payMethod = PayMethod.getPayMethodByMethod(workFlowRequestBean.getPayMethod());
        } catch (FacadeInvalidParameterException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
        }
        PaymentOption paymentOption = new PaymentOption();
        if (workFlowRequestBean.getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY
                && workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                && CollectionUtils.isNotEmpty(workFlowRequestBean.getPaymentOfferCheckoutReqData().getOfferBreakup())
                && com.paytm.pgplus.enums.PayMethod.BALANCE.equals(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                        .getOfferBreakup().get(0).getPayMethod())) {
            paymentOption.setPayMethod(PayMethod.BALANCE);
        } else {
            String issuingBank = StringUtils.isNotBlank(workFlowRequestBean.getBankCode()) ? workFlowRequestBean
                    .getBankCode() : workFlowRequestBean.getInstId();
            // hack for PPBL
            if (PayMethod.PPBL.getMethod().equals(issuingBank)) {
                paymentOption.setPayMethod(PayMethod.PPBL);
            } else {
                paymentOption.setPayMethod(payMethod);
                paymentOption.setIssuingBank(issuingBank);
            }
            paymentOption.setVpa(workFlowRequestBean.getVirtualPaymentAddress());
        }
        paymentOption.setTransactionAmount(getNonWalletTxnAmountIfHybrid(workFlowRequestBean));
        if (PayMethod.EMI.getMethod().equals(workFlowRequestBean.getPayMethod())
                || PayMethod.EMI_DC.getMethod().equals(workFlowRequestBean.getPayMethod())) {
            LOGGER.info("setting tenure for emi");
            paymentOption.setTenure(getTenure(workFlowRequestBean));
        }
        paymentOptions.add(paymentOption);
        return paymentOptions;
    }

    private Integer getTenure(WorkFlowRequestBean workFlowRequestBean) {
        String emiPlanId = null;
        PaymentRequestBean paymentRequestBean = workFlowRequestBean.getPaymentRequestBean();
        if (null != paymentRequestBean && StringUtils.isNotBlank(paymentRequestBean.getEmiPlanID())) {
            emiPlanId = paymentRequestBean.getEmiPlanID();
            String emiMonth = StringUtils.substringAfter(emiPlanId, "|");
            return Integer.valueOf(emiMonth);
        }
        return null;
    }

    private List<PromoSaving> getPromoSavings(PaymentOfferDetails paymentOfferDetails) {
        List<PromoSaving> promoSavings = new ArrayList<>();
        if (StringUtils.isNotBlank(paymentOfferDetails.getCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType("cashback");
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getCashbackAmount())));
            promoSavings.add(promoSaving);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getInstantDiscount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType("discount");
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getInstantDiscount())));
            promoSavings.add(promoSaving);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getPaytmCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType("paytm_cashback");
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getPaytmCashbackAmount())));
            promoSavings.add(promoSaving);
        }
        return promoSavings;
    }

    public void validateWorkflowReqForCheckoutPaymentPromo(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean != null
                && workFlowRequestBean.getPayMethod() != null
                && StringUtils.isNotBlank(workFlowRequestBean.getTxnAmount())
                && (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null || workFlowRequestBean
                        .getPaymentOffersAppliedV2() != null)) {
            try {
                PayMethod.getPayMethodByMethod(workFlowRequestBean.getPayMethod());
                // Add offers is not allow for add and pay
                if (workFlowRequestBean.getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY) {
                    // Additional handling to suppory addnpay for flipkart
                    if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                            && CollectionUtils.isNotEmpty(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                                    .getOfferBreakup())) {
                        if (!com.paytm.pgplus.enums.PayMethod.BALANCE.equals(workFlowRequestBean
                                .getPaymentOfferCheckoutReqData().getOfferBreakup().get(0).getPayMethod())) {
                            throw new BizPaymentOfferCheckoutException(
                                    "Payment Offers not allow for add and pay transactions");
                        }
                    } else if (workFlowRequestBean.getPaymentOffersAppliedV2() != null) {
                        throw new BizPaymentOfferCheckoutException(
                                "Payment Offers not allow for add and pay transactions");
                    }

                }
                return;
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
    }

}
