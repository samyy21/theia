package com.paytm.pgplus.theia.emiSubvention.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.ExpressCardModel;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.emisubvention.constants.EmiSubventionConstants;
import com.paytm.pgplus.facade.emisubvention.enums.EmiType;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.models.request.BanksRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.TenuresRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.TenuresResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.emisubvention.utils.SubventionUtils;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.models.SimplifiedSubvention;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.model.PaymentDetails;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponseBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponseBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponseBody;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.models.IdentifierPaymentOption;
import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.impl.ExpressPaymentService;
import com.paytm.pgplus.theia.utils.BinUtils;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.common.enums.CardTypeEnum.DINERS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CARDTOKENINFO;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.ENABLE_GCIN_ON_COFT_PROMO;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component("subventionEmiServiceHelper")
public class SubventionEmiServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubventionEmiServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SubventionEmiServiceHelper.class);

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    private ExpressPaymentService expressPaymentService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private BizRequestResponseMapperImpl bizRequestResponseMapper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardService;

    @Autowired
    @Qualifier("coftTokenDataService")
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    private static final int AMOUNT_BASED_QUANTITY = 1;
    private static final String SUB_RULE_SPLITTER = ",";

    public Map<String, String> prepareQueryParams(String mid, String custId, String orderId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(PG_MERCHANT_ID, mid);
        if (StringUtils.isNotBlank(custId)) {
            queryParams.put(EMI_SUBVENTION_CUSTOMER_ID, custId);
        }
        return queryParams;
    }

    public ValidateRequest prepareEmiServiceRequest(ValidateEmiRequestBody validateEmiRequestBody, String token,
            String tokenType) {
        if (CollectionUtils.isEmpty(validateEmiRequestBody.getItems())) {
            validateEmiRequestBody.setPrice(SubventionUtils.formatAmount(validateEmiRequestBody.getPrice()));
            validateEmiRequestBody.setSubventionAmount(SubventionUtils.formatAmount(validateEmiRequestBody
                    .getSubventionAmount()));
        }

        ValidateRequest validateEmiServiceRequest = new ValidateRequest();
        if (!CollectionUtils.isEmpty(validateEmiRequestBody.getItems())) {
            validateEmiServiceRequest.setItems(validateEmiRequestBody.getItems());
            validateEmiServiceRequest.setItemBasedRequest(true);
            setDefaultValuesForItemBasedParams(validateEmiServiceRequest.getItems());
        } else {
            validateEmiServiceRequest.setItems(getItemsListForAmountBased(validateEmiRequestBody));
        }

        PaymentDetail serviceRequestPaymentDetail = new PaymentDetail();
        PaymentOption paymentOption = null;

        // SavedCardId is required in validateEMI Request from UI via txnToken
        // flow only as bin6 is not available to UI in APP_DATA
        boolean isEnableGcinOnCoftPromo = ff4jUtils.isFeatureEnabledOnMid(validateEmiRequestBody.getMid(),
                ENABLE_GCIN_ON_COFT_PROMO, false);

        if (!isEnableGcinOnCoftPromo) {
            SavedCard savedCard = null;
            if (validateEmiRequestBody.getPaymentDetails() != null
                    && StringUtils.isNotEmpty(validateEmiRequestBody.getPaymentDetails().getSavedInstrumentId())
                    && token != null) {
                NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(token);
                if (cashierInfoResponse != null) {
                    savedCard = bizRequestResponseMapper.getSavedCardDetails(cashierInfoResponse,
                            validateEmiRequestBody.getPaymentDetails().getSavedInstrumentId());
                }
            }
            if (savedCard != null) {
                paymentOption = getPaymentOptionWithSavedCard(validateEmiRequestBody, savedCard);
            } else {
                paymentOption = getPaymentOptionWithCardDetails(validateEmiRequestBody);
            }
        } else {
            SavedCard savedCard = null;
            String merchantCoftConfig = coftTokenDataService.getMerchantConfig(validateEmiRequestBody.getMid());
            if (validateEmiRequestBody.getPaymentDetails() != null
                    && StringUtils.isNotEmpty(validateEmiRequestBody.getPaymentDetails().getSavedInstrumentId())
                    && token != null) {
                token = getUpdatedTxnToken(token, tokenType, validateEmiRequestBody);
                NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(token);
                if (cashierInfoResponse != null) {
                    savedCard = coftTokenDataService.fetchTokenDataFromFPO(validateEmiRequestBody.getPaymentDetails()
                            .getSavedInstrumentId(), cashierInfoResponse);
                }
            }
            if (savedCard != null) {
                paymentOption = getPaymentOptionWithSavedCardForCoftTxn(validateEmiRequestBody, savedCard,
                        merchantCoftConfig);
            } else {
                paymentOption = getPaymentOptionWithCardDetailsForCoftTxn(validateEmiRequestBody, merchantCoftConfig);
            }
        }
        // array of paymentOption or single payment option
        List<PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);

        serviceRequestPaymentDetail.setPaymentOptions(paymentOptions);
        if (null != validateEmiRequestBody.getPaymentDetails()) {
            serviceRequestPaymentDetail.setTotalTransactionAmount(validateEmiRequestBody.getPaymentDetails()
                    .getTotalTransactionAmount());
        }
        validateEmiServiceRequest.setCustomerId(validateEmiRequestBody.getCustomerId());
        validateEmiServiceRequest.setPaymentDetails(serviceRequestPaymentDetail);
        validateEmiServiceRequest.setPlanId(validateEmiRequestBody.getPlanId());
        validateEmiServiceRequest.setSiteId(EMI_SUBVENTION_SITEID);
        validateEmiServiceRequest.setMid(validateEmiRequestBody.getMid());

        if (CollectionUtils.isEmpty(validateEmiRequestBody.getItems())) {
            validateEmiServiceRequest.setPrice(validateEmiRequestBody.getPrice());
            validateEmiServiceRequest.setSubventionAmount(validateEmiRequestBody.getSubventionAmount());
        }
        return validateEmiServiceRequest;
    }

    private String toJsonString(Object payloadData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(payloadData);
        } catch (JsonProcessingException e) {
            return "Unable to convert payloadData to json  :";
        }
    }

    public BanksRequest prepareEmiServiceRequest(EmiBanksRequest emiBanksRequest) {
        BanksRequest banksRequest = new BanksRequest();
        banksRequest.setItems(emiBanksRequest.getBody().getItems());
        banksRequest.setSiteId(EMI_SUBVENTION_SITEID);
        banksRequest.setMid(emiBanksRequest.getBody().getMid());
        banksRequest.setItemBasedRequest(true);
        setDefaultValuesForItemBasedParams(banksRequest.getItems());
        return banksRequest;
    }

    public BanksRequest prepareEmiServiceRequestAmountBased(EmiBanksRequest emiBanksRequest) {
        BanksRequest banksRequest = new BanksRequest();
        List<Item> itemList = new ArrayList<>();
        double subventedAmount = (emiBanksRequest.getBody().getPrice() - emiBanksRequest.getBody()
                .getSubventionAmount());
        String subventionprodId = emiBanksRequest.getBody().getMid() + "_"
                + emiBanksRequest.getBody().getSubventionAmount() + "_" + SUBVENTION_PRODUCT;
        String standardProductId = emiBanksRequest.getBody().getMid() + "_" + subventedAmount + "_" + STANDARD_PRODUCT;
        Item dummySubventionItem = new Item(false, false, true, subventionprodId, subventionprodId, emiBanksRequest
                .getBody().getSubventionAmount(), emiBanksRequest.getBody().getSubventionAmount(),
                AMOUNT_BASED_QUANTITY);
        Item dummyStandardItem = new Item(true, false, true, standardProductId, standardProductId, subventedAmount,
                subventedAmount, AMOUNT_BASED_QUANTITY);
        itemList.add(dummySubventionItem);
        if (subventedAmount > 0) {
            itemList.add(dummyStandardItem);
        }
        banksRequest.setItems(itemList);
        banksRequest.setSiteId(EMI_SUBVENTION_SITEID);
        banksRequest.setMid(emiBanksRequest.getBody().getMid());
        return banksRequest;
    }

    public List<Item> prepareItemListForAmountBasedSubvention(InitiateTransactionRequestBody orderDetail) {
        List<Item> itemList = new ArrayList<>();
        double subventedAmount = (Double.parseDouble(orderDetail.getTxnAmount().getValue()) - orderDetail
                .getSimplifiedSubvention().getSubventionAmount());
        String subventionprodId = orderDetail.getMid() + "_"
                + orderDetail.getSimplifiedSubvention().getSubventionAmount() + "_" + SUBVENTION_PRODUCT;
        String standardProductId = orderDetail.getMid() + "_" + subventedAmount + "_" + STANDARD_PRODUCT;
        Item dummySubventionItem = new Item(false, false, true, subventionprodId, subventionprodId, orderDetail
                .getSimplifiedSubvention().getSubventionAmount(), orderDetail.getSimplifiedSubvention()
                .getSubventionAmount(), AMOUNT_BASED_QUANTITY);
        Item dummyStandardItem = new Item(true, false, true, standardProductId, standardProductId, subventedAmount,
                subventedAmount, AMOUNT_BASED_QUANTITY);
        itemList.add(dummySubventionItem);
        if (subventedAmount > 0) {
            itemList.add(dummyStandardItem);
        }
        return itemList;
    }

    public TenuresRequest prepareEmiServiceRequest(EmiTenuresRequest emiTenuresRequest) {
        TenuresRequest tenuresRequest = new TenuresRequest();
        tenuresRequest.setItems(emiTenuresRequest.getBody().getItems());
        tenuresRequest.setFilters(emiTenuresRequest.getBody().getFilters());
        tenuresRequest.setSiteId(EMI_SUBVENTION_SITEID);
        tenuresRequest.setMid(emiTenuresRequest.getBody().getMid());
        tenuresRequest.setItemBasedRequest(true);
        setDefaultValuesForItemBasedParams(tenuresRequest.getItems());
        return tenuresRequest;
    }

    public TenuresRequest prepareEmiServiceRequestAmountBased(EmiTenuresRequest emiTenuresRequest) {
        TenuresRequest tenuresRequest = new TenuresRequest();
        String subventionprodId = emiTenuresRequest.getBody().getMid() + "_"
                + emiTenuresRequest.getBody().getSubventionAmount() + "_" + SUBVENTION_PRODUCT;
        List<Item> itemList = new ArrayList<>();
        double subventedAmount = (emiTenuresRequest.getBody().getPrice() - emiTenuresRequest.getBody()
                .getSubventionAmount());
        String standardProductId = emiTenuresRequest.getBody().getMid() + "_" + subventedAmount + "_"
                + STANDARD_PRODUCT;
        ;
        Item dummySubventionItem = new Item(false, false, true, subventionprodId, subventionprodId, emiTenuresRequest
                .getBody().getSubventionAmount(), emiTenuresRequest.getBody().getSubventionAmount(),
                AMOUNT_BASED_QUANTITY);
        Item dummyStandardItem = new Item(true, false, true, standardProductId, standardProductId, subventedAmount,
                subventedAmount, AMOUNT_BASED_QUANTITY);
        itemList.add(dummySubventionItem);
        if (subventedAmount > 0) {
            itemList.add(dummyStandardItem);
        }
        tenuresRequest.setItems(itemList);
        tenuresRequest.setFilters(emiTenuresRequest.getBody().getFilters());
        tenuresRequest.setSiteId(EMI_SUBVENTION_SITEID);
        tenuresRequest.setMid(emiTenuresRequest.getBody().getMid());
        tenuresRequest.setOriginalPrice(emiTenuresRequest.getBody().getOriginalPrice());
        String req = toJsonString(tenuresRequest);
        return tenuresRequest;
    }

    public EmiBanksResponse prepareBanksEmiResponse(GenericEmiSubventionResponse<BanksResponse> banksResponse,
            EmiBanksRequest request) {
        EmiBanksResponse emiBanksResponse = new EmiBanksResponse();
        emiBanksResponse.setHead(EmiSubventionUtils.createResponseHeader());
        emiBanksResponse.setBody(new EmiBanksResponseBody());
        emiBanksResponse.getHead().setRequestId(request.getHead().getRequestId());

        if (banksResponse != null && banksResponse.getStatus() == 0) {
            LOGGER.error("banks EMI failure response {}", emiBanksResponse);
            throw new BaseException(new ResultInfo(ResultCode.FAILED.getResultStatus(), banksResponse.getError()
                    .getCode(), banksResponse.getError().getCode(), banksResponse.getError().getMessage()));
        }

        emiBanksResponse.getBody().setEmiTypes(banksResponse.getData().getEmiTypes());
        return emiBanksResponse;
    }

    public EmiTenuresResponse prepareTenuresEmiResponse(GenericEmiSubventionResponse<TenuresResponse> tenuresResponse,
            EmiTenuresRequest request) {

        EmiTenuresResponse emiTenuresResponse = new EmiTenuresResponse();
        EmiTenuresResponseBody emiTenuresResponseBody = new EmiTenuresResponseBody();

        emiTenuresResponse.setHead(EmiSubventionUtils.createResponseHeader());
        emiTenuresResponse.getHead().setRequestId(request.getHead().getRequestId());

        if (tenuresResponse != null && tenuresResponse.getStatus() == 0) {
            LOGGER.error("tenures EMI failure response {}", emiTenuresResponse);
            throw new BaseException(new ResultInfo(ResultCode.FAILED.getResultStatus(), tenuresResponse.getError()
                    .getCode(), tenuresResponse.getError().getCode(), tenuresResponse.getError().getMessage()));
        }
        emiTenuresResponseBody.setBankName(tenuresResponse.getData().getBankName());
        emiTenuresResponseBody.setBankCode(tenuresResponse.getData().getBankCode());
        emiTenuresResponseBody.setCardType(tenuresResponse.getData().getCardType());
        emiTenuresResponseBody.setBankLogoUrl(tenuresResponse.getData().getBankLogoUrl());
        emiTenuresResponseBody.setPlanDetails(tenuresResponse.getData().getPlanDetails());
        emiTenuresResponse.setBody(emiTenuresResponseBody);

        List<PlanDetail> planDetails = tenuresResponse.getData().getPlanDetails();
        Double discountedGratifiedAmount;
        Double nonDiscountedAmount = 0.0;
        Double providedNonDiscountedAmount = 0.0;
        Double nonDiscountedAmountWithOriginalPrice = 0.0;
        if (CollectionUtils.isEmpty(request.getBody().getItems())) {
            providedNonDiscountedAmount = request.getBody().getPrice();
        } else {
            nonDiscountedAmount = totalCalculatedPrice(request.getBody().getItems());
            nonDiscountedAmountWithOriginalPrice = totalCalculatedPriceWithOriginalPrice(request.getBody().getItems());
        }
        Double providedNonDiscountedAmountTemp;
        Double nonDiscountedAmountTemp;
        if (planDetails.size() > 0) {
            for (int i = 0; i < planDetails.size(); i++) {
                /*
                 * setting subvention/standardId in itrmBreakupList
                 */

                providedNonDiscountedAmountTemp = providedNonDiscountedAmount;
                nonDiscountedAmountTemp = nonDiscountedAmount;
                List<ItemBreakUp> itemBreakUpList = planDetails.get(i).getItemBreakUp();
                if (!CollectionUtils.isEmpty(itemBreakUpList)) {
                    for (ItemBreakUp item : itemBreakUpList) {
                        if (item.getId().contains("SUB")) {
                            item.setId("SUBVENTION");
                        } else if (item.getId().contains("STAN")) {
                            item.setId("STANDARD");
                        }
                    }
                }
                /*
                 * setting providedNonDiscountedAmount/nonDiscountedAmount as
                 * originalPrice of the order for the tenures on which offer is
                 * not applicable
                 */
                if (!CollectionUtils.isEmpty(request.getBody().getApplicableTenures())
                        && !request.getBody().getApplicableTenures()
                                .contains(planDetails.get(i).getInterval().toString())) {
                    if (!CollectionUtils.isEmpty(request.getBody().getItems())
                            && nonDiscountedAmountWithOriginalPrice != 0.0) {
                        nonDiscountedAmount = nonDiscountedAmountWithOriginalPrice;
                    } else if (request.getBody().isAmountBasedSubvention()
                            && request.getBody().getOriginalPrice() != 0.0) {
                        providedNonDiscountedAmount = request.getBody().getOriginalPrice();
                    }
                }
                if ((EmiType.STANDARD).equals(tenuresResponse.getData().getPlanDetails().get(i).getEmiType())) {
                    if (!StringUtils.isBlank(String.valueOf(providedNonDiscountedAmount))
                            && providedNonDiscountedAmount > 0) {
                        emiTenuresResponse.getBody().getPlanDetails().get(i)
                                .setFinalTransactionAmount(String.valueOf(providedNonDiscountedAmount));
                    } else {
                        emiTenuresResponse.getBody().getPlanDetails().get(i)
                                .setFinalTransactionAmount(String.valueOf(nonDiscountedAmount));
                    }
                } else if ((EmiType.SUBVENTION).equals(tenuresResponse.getData().getPlanDetails().get(i).getEmiType())) {
                    // gratification size equal
                    discountedGratifiedAmount = getGratifiedDiscountAmount(tenuresResponse.getData().getPlanDetails()
                            .get(i).getGratifications());

                    if (CollectionUtils.isEmpty(request.getBody().getItems())) {
                        emiTenuresResponse
                                .getBody()
                                .getPlanDetails()
                                .get(i)
                                .setFinalTransactionAmount(
                                        String.valueOf(providedNonDiscountedAmount - discountedGratifiedAmount));
                    } else {
                        emiTenuresResponse
                                .getBody()
                                .getPlanDetails()
                                .get(i)
                                .setFinalTransactionAmount(
                                        String.valueOf(nonDiscountedAmount - discountedGratifiedAmount));
                    }
                }
                providedNonDiscountedAmount = providedNonDiscountedAmountTemp;
                nonDiscountedAmount = nonDiscountedAmountTemp;
            }
        }
        return emiTenuresResponse;
    }

    public ValidateEmiResponse prepareValidateEmiResponse(
            GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse, ValidateEmiRequest request,
            ValidateRequest validateRequest) {
        ValidateEmiResponse validateEmiResponse = new ValidateEmiResponse();
        validateEmiResponse.setHead(EmiSubventionUtils.createResponseHeader());
        validateEmiResponse.setBody(new ValidateEmiResponseBody());
        validateEmiResponse.getHead().setRequestId(request.getHead().getRequestId());

        if (validateEmiServiceResponse != null && validateEmiServiceResponse.getStatus() == 0) {
            LOGGER.error("Validate EMI failure response {}", validateEmiServiceResponse);
            throw new BaseException(new ResultInfo(ResultCode.FAILED.getResultStatus(), validateEmiServiceResponse
                    .getError().getCode(), validateEmiServiceResponse.getError().getCode(), validateEmiServiceResponse
                    .getError().getMessage()));
        }
        validateEmiResponse.getBody().setBankId(validateEmiServiceResponse.getData().getBankId());
        validateEmiResponse.getBody().setBankName(validateEmiServiceResponse.getData().getBankName());
        validateEmiResponse.getBody().setBankCode(validateEmiServiceResponse.getData().getBankCode());
        validateEmiResponse.getBody().setCardType(validateEmiServiceResponse.getData().getCardType());
        validateEmiResponse.getBody().setBankLogoUrl(validateEmiServiceResponse.getData().getBankLogoUrl());
        validateEmiResponse.getBody().setPlanId(validateEmiServiceResponse.getData().getPlanId());
        validateEmiResponse.getBody().setPgPlanId(validateEmiServiceResponse.getData().getPgPlanId());
        validateEmiResponse.getBody().setRate(String.valueOf(validateEmiServiceResponse.getData().getRate()));
        validateEmiResponse.getBody().setInterval(String.valueOf(validateEmiServiceResponse.getData().getInterval()));
        // emi data type to be decided
        validateEmiResponse.getBody().setEmi(String.valueOf(validateEmiServiceResponse.getData().getEmi()));
        validateEmiResponse.getBody().setInterest(String.valueOf(validateEmiServiceResponse.getData().getInterest()));
        validateEmiResponse.getBody().setEmiType(validateEmiServiceResponse.getData().getEmiType().getType());
        validateEmiResponse.getBody().setEmiLabel(validateEmiServiceResponse.getData().getEmiLabel());
        // set emiSubventedTransactionAmount
        Double discountedGratifiedAmount = getGratifiedDiscountAmount(validateEmiServiceResponse.getData()
                .getGratifications());
        Map<String, String> items = validateRequest.getItems().stream()
                .filter(item -> org.apache.commons.lang3.StringUtils.isNotEmpty(item.getBrandId()))
                .collect(Collectors.toMap(Item::getId, Item::getBrandId));
        discountedGratifiedAmount = updateGratificationAmountForSpecificBanks(validateEmiResponse,
                validateEmiServiceResponse, discountedGratifiedAmount, items);
        validateEmiResponse.getBody().setGratifications(validateEmiServiceResponse.getData().getGratifications());
        validateEmiResponse.getBody().setItemBreakUpList(validateEmiServiceResponse.getData().getItemBreakUp());
        List<Item> itemList = validateRequest.getItems();
        Double providedNonDiscountedAmount = 0.0;

        List<ItemBreakUp> itemBreakUpList = validateEmiServiceResponse.getData().getItemBreakUp();
        if (!CollectionUtils.isEmpty(itemBreakUpList)) {
            for (ItemBreakUp item : itemBreakUpList) {
                if (item.getId().contains("SUB")) {
                    item.setId("SUBVENTION");
                } else if (item.getId().contains("STAN")) {
                    item.setId("STANDARD");
                }
            }
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())) {
            providedNonDiscountedAmount = request.getBody().getPrice();
            validateEmiResponse.getBody().setFinalTransactionAmount(
                    AmountUtils.formatNumberToTwoDecimalPlaces(String.valueOf(providedNonDiscountedAmount
                            - discountedGratifiedAmount)));
        } else {
            validateEmiResponse.getBody().setFinalTransactionAmount(
                    AmountUtils.formatNumberToTwoDecimalPlaces(String.valueOf(request.getBody().getPaymentDetails()
                            .getTotalTransactionAmount()
                            - discountedGratifiedAmount)));
        }

        Map<String, Object> emiSubventionRedisData = new HashMap<>();
        if (request.getBody().isGenerateTokenForIntent()) {
            if (SUBVENTION.equals(validateEmiServiceResponse.getData().getEmiType().getType())) {
                emiSubventionRedisData.put(VALIDATE_EMI_RESPONSE, validateEmiServiceResponse.getData());
                emiSubventionRedisData.put(VALIDATE_EMI_REQUEST, validateRequest);
                emiSubventionRedisData.put(EMI_SUBVENTED_TRANSACTION_AMOUNT, AmountUtils
                        .formatNumberToTwoDecimalPlaces(validateEmiResponse.getBody().getFinalTransactionAmount()));
                emiSubventionRedisData.put(EMI_SUBVENTION_CUSTOMER_ID, request.getBody().getCustomerId());

                // persist in redis the validateEmiResponse with emiToken
                String emiSubventionToken = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
                theiaSessionRedisUtil.set(emiSubventionToken, emiSubventionRedisData, EMI_SUBVENTION_EXPIRY_TIME);
                validateEmiResponse.getBody().setEmiSubventionToken(emiSubventionToken);
                LOGGER.info("emiSubvention token generated is {}", emiSubventionToken);
            } else {
                throw new RequestValidationException(new ResultInfo(ResultCode.FAILED.getResultStatus(),
                        ResultCode.FAILED.getResultCodeId(), ResultCode.FAILED.getResultMsg(),
                        EMI_SUBVENTION_NOT_APPLICABLE));
            }
        }

        return validateEmiResponse;
    }

    private Double updateGratificationAmountForSpecificBanks(ValidateEmiResponse response,
            GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse,
            Double discountedGratifiedAmount, Map<String, String> items) {
        if (discountedGratifiedAmount > 0 && validateEmiServiceResponse.getData().getGratifications().size() == 1) {
            List<ItemBreakUp> itemBreakUpList = validateEmiServiceResponse.getData().getItemBreakUp();
            List<String> brandIdsList = new ArrayList<>();
            Double updatedDiscountedGratifiedAmount = 0.0;
            String dummyBrandIds = ff4jUtils.getPropertyAsStringWithDefault(ff4jUtils.DUMMY_BRAND_LIST, "100");
            if (StringUtils.isNotEmpty(dummyBrandIds)) {
                dummyBrandIds = dummyBrandIds.replaceAll("\\s+", "");
                brandIdsList = Arrays.asList(dummyBrandIds.split(SUB_RULE_SPLITTER));
            }
            LOGGER.info("validate Emi - dummyItems: {}, dummyBrandIds: {}", items, dummyBrandIds);
            if (!CollectionUtils.isEmpty(itemBreakUpList)
                    && ConfigurationUtil.getProperty("MERCHANT_ONLY_CONTRIBUTION_EMI_BANKS", "").contains(
                            response.getBody().getBankCode())) {
                for (ItemBreakUp itemBreakUp : itemBreakUpList) {
                    if (itemBreakUp.getAmountBearer() != null
                            && itemBreakUp.getAmountBearer().getBrand() > 0
                            && !(MapUtils.isNotEmpty(items) && items.containsKey(itemBreakUp.getId())
                                    && null != items.get(itemBreakUp.getId()) && !CollectionUtils.isEmpty(brandIdsList) && brandIdsList
                                        .contains(items.get(itemBreakUp.getId())))) {
                        updatedDiscountedGratifiedAmount = updatedDiscountedGratifiedAmount
                                + itemBreakUp.getAmountBearer().getMerchant();
                        response.getBody().setMerchantOnlyContribution(true);
                    }
                }
            }
            if (response.getBody().getMerchantOnlyContribution() != null
                    && response.getBody().getMerchantOnlyContribution()) {
                discountedGratifiedAmount = updatedDiscountedGratifiedAmount;
            }
        }
        return discountedGratifiedAmount;
    }

    public Map<String, String> prepareHeaderMapForValidateEmiRequest(ValidateEmiRequestBody validateEmiRequestBody) {
        Map<String, String> headerMapForValidateEmi = new HashMap<>();
        headerMapForValidateEmi.put(USER_ID, validateEmiRequestBody.getCustomerId());
        return headerMapForValidateEmi;
    }

    private int parseFistNDigit(String number, int n) {
        if (NumberUtils.isNumber(number)) {
            if (n > number.length()) {
                n = number.length();
            }
            return NumberUtils.toInt(number.substring(0, n));
        }
        return 0;
    }

    private BinDetail binDetail(int binNumber) {
        try {
            BinUtils.logSixDigitBinLength(Integer.toString(binNumber));
            BinDetail binDetail = binFetchService.getCardBinDetail((long) binNumber);
            EXT_LOGGER.customInfo("Mapping response - BinDetail :: {}", binDetail);
            if (binDetail != null) {
                return binDetail;
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        throw BaseException.getException();
    }

    private double totalCalculatedPrice(List<Item> items) {
        Double nonDiscountedPrice = 0.0;
        for (Item item : items) {
            nonDiscountedPrice += item.getPrice();
        }
        return nonDiscountedPrice;
    }

    private double totalCalculatedPriceWithOriginalPrice(List<Item> items) {
        Double nonDiscountedPrice = 0.0;
        for (Item item : items) {
            nonDiscountedPrice += item.getOriginalPrice();
        }
        return nonDiscountedPrice;
    }

    public PaymentOption getPaymentOptionWithSavedCard(ValidateEmiRequestBody validateEmiRequestBody,
            SavedCard savedCard) {
        PaymentOption paymentOption = new PaymentOption();
        paymentOption.setTransactionAmount(validateEmiRequestBody.getPaymentDetails().getTotalTransactionAmount());
        if (savedCard.getCardDetails().getCardType() == null || savedCard.getIssuingBank() == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        paymentOption.setBin6(Integer.parseInt(savedCard.getCardDetails().getFirstSixDigit()));
        paymentOption.setIssuingBank(savedCard.getIssuingBank());
        paymentOption.setIssuingNetworkCode(savedCard.getInstId());
        paymentOption.setPayMethod(savedCard.getCardDetails().getCardType());
        return paymentOption;
    }

    public PaymentOption getPaymentOptionWithCardDetails(ValidateEmiRequestBody validateEmiRequestBody) {

        // cardIndex to be find by hitting the cacheCard(if token is present
        // find from redis else find from cacheCard token api
        PaymentOption paymentOption = new PaymentOption();
        paymentOption.setTransactionAmount(validateEmiRequestBody.getPaymentDetails().getTotalTransactionAmount());

        String cardNumber = validateEmiRequestBody.getPaymentDetails().getCardNumber();
        int bin6;
        if (StringUtils.isBlank(validateEmiRequestBody.getPaymentDetails().getCardBin6())) {
            setCardIndexNumberAndFirstSixdigit(validateEmiRequestBody, paymentOption);
            bin6 = paymentOption.getBin6();
        } else {
            bin6 = Integer.parseInt(validateEmiRequestBody.getPaymentDetails().getCardBin6());
            paymentOption.setBin6(bin6);
        }

        BinDetail binDetail = binDetail(bin6);

        if (binDetail.getCardType() == null || binDetail.getBankCode() == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        String bankCode = binDetail.getBankCode();
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        paymentOption.setPayMethod(binDetail.getCardType());
        if (StringUtils.isNotBlank(cardNumber)) {
            paymentOption.setBin8(parseFistNDigit(cardNumber, 8));
        }

        return paymentOption;
    }

    private void setCardIndexNumberAndFirstSixdigit(ValidateEmiRequestBody validateEmiRequestBody,
            PaymentOption paymentOption) {
        String cacheToken = validateEmiRequestBody.getCacheCardToken();
        String cardIndexNumber = null;
        Integer firstSixDigitCardNumber = null;

        if (isBlank(validateEmiRequestBody.getCacheCardToken())
                && validateEmiRequestBody.getPaymentDetails().getCardNumber() != null) {
            ExpressCardTokenResponse expressCardTokenResponse = expressPaymentService.getCardToken(
                    buildExpressCardTokenRequest(validateEmiRequestBody), false);
            cacheToken = expressCardTokenResponse.getToken();
        }
        String expressTokenDetailsKey = TheiaConstant.ExtraConstants.EXPRESS_CARD_TOKEN + cacheToken;
        ExpressCardModel tokenDetails = (ExpressCardModel) theiaTransactionalRedisUtil.get(expressTokenDetailsKey);
        if (tokenDetails == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getResultStatus(),
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getResultCodeId(),
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        cardIndexNumber = tokenDetails.getCardIndexNo();
        firstSixDigitCardNumber = Integer.valueOf(tokenDetails.getCardBin());
        paymentOption.setCardIndexNo(cardIndexNumber);
        paymentOption.setBin6(firstSixDigitCardNumber);

        return;
    }

    private ExpressCardTokenRequest buildExpressCardTokenRequest(ValidateEmiRequestBody validateEmiRequestBody) {
        ExpressCardTokenRequest expressCardTokenRequest = new ExpressCardTokenRequest();

        String cardInfo = validateEmiRequestBody.getPaymentDetails().getCardNumber();

        String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
        String requiredCardDetails = "";
        if (cardDetails.length != 4) {
            LOGGER.error("Invalid cardDetails length: {}", cardDetails.length);
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        expressCardTokenRequest.setCvv(cardDetails[2].trim());
        expressCardTokenRequest.setCardNumber(cardDetails[1].trim());
        expressCardTokenRequest.setExpiryMonth(cardDetails[3].trim().substring(0, 2));
        expressCardTokenRequest.setExpiryYear(cardDetails[3].trim().substring(2, 6));
        expressCardTokenRequest.setMid(validateEmiRequestBody.getMid());
        expressCardTokenRequest.setUserId(validateEmiRequestBody.getCustomerId());
        if (validateEmiRequestBody.getPaymentDetails().getCardTokenInfo() != null) {
            expressCardTokenRequest.setCardTokenInfo(validateEmiRequestBody.getPaymentDetails().getCardTokenInfo());
        }
        return expressCardTokenRequest;
    }

    double getGratifiedDiscountAmount(List<Gratification> gratifications) {
        Double discountedGratifiedAmount = 0.0;

        for (Gratification gratification : gratifications) {
            if (GratificationType.DISCOUNT.equals(gratification.getType())) {
                discountedGratifiedAmount += gratification.getValue();
            }
        }
        return discountedGratifiedAmount;
    }

    public void checkIfMidExist(String mid) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
    }

    public void validateSimplifiedEmiRequest(SimplifiedSubvention simplifiedSubvention) {
        ValidateEmiRequestBody body = transform(simplifiedSubvention);
        validateMandatoryParams(body);
        validateConditionalParams(body);
    }

    public static ValidateEmiRequestBody transform(SimplifiedSubvention simplifiedSubvention) {
        ValidateEmiRequestBody validateEmiRequestBody = new ValidateEmiRequestBody();
        validateEmiRequestBody.setPlanId(simplifiedSubvention.getPlanId());
        validateEmiRequestBody.setMid(simplifiedSubvention.getMid());
        validateEmiRequestBody.setCustomerId(simplifiedSubvention.getCustomerId());
        validateEmiRequestBody.setOrderId(simplifiedSubvention.getOrderId());
        validateEmiRequestBody.setCacheCardToken(simplifiedSubvention.getCacheCardToken());
        validateEmiRequestBody.setPrice(simplifiedSubvention.getPrice());
        validateEmiRequestBody.setSubventionAmount(simplifiedSubvention.getSubventionAmount());
        if (Objects.nonNull(simplifiedSubvention.getItems())) {
            validateEmiRequestBody.setItems(simplifiedSubvention.getItems().stream()
                    .map(SubventionEmiServiceHelper::transform).collect(Collectors.toList()));
        }
        validateEmiRequestBody.setPaymentDetails(transform(simplifiedSubvention.getPaymentDetails()));
        validateEmiRequestBody.setOfferDetails(transform(simplifiedSubvention.getOfferDetails()));
        return validateEmiRequestBody;
    }

    public static Item transform(com.paytm.pgplus.models.Item item) {
        if (item == null) {
            return null;
        }
        Item newItem = new Item();
        newItem.setId(item.getId());
        newItem.setProductId(item.getProductId());
        newItem.setBrandId(item.getBrandId());
        newItem.setCategoryList(item.getCategoryList());
        newItem.setMerchantId(item.getMerchantId());
        newItem.setModel(item.getModel());
        newItem.setEan(item.getEan());
        newItem.setPrice(item.getPrice());
        newItem.setListingPrice(item.getListingPrice());
        newItem.setQuantity(item.getQuantity());
        newItem.setDiscoverability(item.getDiscoverability());
        newItem.setVerticalId(item.getVerticalId());
        newItem.setIsPhysical(item.getIsPhysical());
        newItem.setIsEmiEnabled(item.getIsEmiEnabled());
        newItem.setOfferDetails(transform(item.getOfferDetails()));
        newItem.setIsStandardEmi(item.getIsStandardEmi());
        return newItem;
    }

    private static OfferDetail transform(com.paytm.pgplus.models.OfferDetail offerDetail) {
        if (offerDetail == null) {
            return null;
        }
        OfferDetail newOfferDetail = new OfferDetail();
        newOfferDetail.setOfferId(offerDetail.getOfferId());
        return newOfferDetail;
    }

    private static PaymentDetails transform(com.paytm.pgplus.models.PaymentDetails paymentDetails) {
        if (paymentDetails == null) {
            return null;
        }
        PaymentDetails newPaymentDetails = new PaymentDetails();
        newPaymentDetails.setCardBin6(paymentDetails.getCardBin6());
        newPaymentDetails.setCardNumber(paymentDetails.getCardNumber());
        newPaymentDetails.setTotalTransactionAmount(paymentDetails.getTotalTransactionAmount());
        return newPaymentDetails;
    }

    public void validateRequestBody(ValidateEmiRequestBody body) {
        validateMandatoryParams(body);
        validateConditionalParams(body);
        if (StringUtils.isBlank(body.getPaymentDetails().getCardNumber())
                && StringUtils.isBlank(body.getCacheCardToken())
                && StringUtils.isBlank(body.getPaymentDetails().getCardBin6())
                && StringUtils.isBlank(body.getPaymentDetails().getSavedInstrumentId())
                && ((ObjectUtils.isEmpty(body.getPaymentDetails().getCardTokenInfo())
                        || StringUtils.isBlank(body.getPaymentDetails().getCardTokenInfo().getCardToken()) || StringUtils
                            .isBlank(body.getPaymentDetails().getCardTokenInfo().getPanUniqueReference())))) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.CARD_DETAILS_MISSING));
        }
    }

    private void validateConditionalParams(ValidateEmiRequestBody body) {
        boolean isAmountBasedSubvention = CollectionUtils.isEmpty(body.getItems());
        if (isAmountBasedSubvention) {
            if (body.getSubventionAmount() == null && body.getPrice() == null) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                        ResponseMessage.BOTH_ITEM_LIST_AMOUNT_EMPTY));
            }
            if (body.getSubventionAmount() == null || body.getPrice() == null) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                        ResponseMessage.BOTH_LISTINGPRICE_PRICE_REQUIRED));
            }
            if (body.getSubventionAmount() <= 0.0d || body.getPrice() <= 0.0d) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                        ResponseMessage.INVALID_AMOUNT_DETAILS));
            }
            if (Double.compare(body.getSubventionAmount(), body.getPrice()) > 0) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                        ResponseMessage.SUBVENTION_AMOUNT_GREATER));
            }
        } else {
            if (body.getPrice() != null || body.getSubventionAmount() != null) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                        ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                        ResponseMessage.BOTH_ITEM_LIST_AMOUNT_NON_EMPTY));
            }
        }
    }

    private void validateMandatoryParams(ValidateEmiRequestBody body) {
        this.checkIfMidExist(body.getMid());
        if (body.getMid() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_MID));
        }
        if (body.getPaymentDetails() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.PAYMENT_DETAILS_MISSING));
        }

        if (body.getPaymentDetails().getTotalTransactionAmount() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.TOTAL_TRANSACTION_AMOUNT_MISSING));
        }
        if (body.getPaymentDetails().getTotalTransactionAmount() <= 0d) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.INVALID_TOTAL_TRANSACTION_AMOUNT));
        }

        if (!isSubventionValidateAmountValid(body)) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.AMOUNT_MISMATCH));
        }
    }

    private boolean isSubventionValidateAmountValid(ValidateEmiRequestBody body) {
        double sumTotal = 0.0;
        if (!CollectionUtils.isEmpty(body.getItems())) {
            List<Item> items = body.getItems();
            for (Item item : items) {
                sumTotal = sumTotal + ((item.getPrice() == null) ? 0 : item.getPrice());
            }
        } else {
            sumTotal = body.getPrice();
        }
        return body.getPaymentDetails() == null || sumTotal == body.getPaymentDetails().getTotalTransactionAmount();
    }

    public static List<Item> getItemsListForAmountBased(ValidateEmiRequestBody validateEmiRequestBody) {
        List<Item> itemList = new ArrayList<>();
        String mid = validateEmiRequestBody.getMid();
        double subventionAmount = validateEmiRequestBody.getSubventionAmount();
        double standardAmount = validateEmiRequestBody.getPrice() - subventionAmount;
        String subventionProductId = SubventionUtils.getProductIdForAmountBased(mid, subventionAmount, false);
        String standardProductId = SubventionUtils.getProductIdForAmountBased(mid, standardAmount, true);
        Item dummySubventionItem = new Item(false, false, true, subventionProductId, subventionProductId,
                validateEmiRequestBody.getSubventionAmount(), validateEmiRequestBody.getSubventionAmount(),
                AMOUNT_BASED_QUANTITY);
        Item dummyStandardItem = new Item(true, false, true, standardProductId, standardProductId, standardAmount,
                standardAmount, AMOUNT_BASED_QUANTITY);
        OfferDetail offerDetails = validateEmiRequestBody.getOfferDetails();
        dummySubventionItem
                .setOfferDetails(offerDetails != null && StringUtils.isNotBlank(offerDetails.getOfferId()) ? offerDetails
                        : new OfferDetail());
        // sending null offerId for standard emi item
        dummyStandardItem.setOfferDetails(new OfferDetail());
        itemList.add(dummySubventionItem);
        if (standardAmount > 0) {
            itemList.add(dummyStandardItem);
        }
        return itemList;
    }

    public void setDefaultValuesForItemBasedParams(List<Item> items) {
        if (items.get(0).getVerticalId() == null) {
            items.get(0).setVerticalId(EmiSubventionConstants.DEFAULT_VERTICAL_ID);
        }
        if (items.get(0).getIsPhysical() == null) {
            items.get(0).setIsPhysical(EmiSubventionConstants.DEFAULT_IS_PHYSICAL_VALUE);
        }
        if (items.get(0).getIsEmiEnabled() == null) {
            items.get(0).setIsEmiEnabled(EmiSubventionConstants.DEFAULT_IS_ENABLED_ENABLED_VALUE);
        }
    }

    public PaymentOption getPaymentOptionWithSavedCardForCoftTxn(ValidateEmiRequestBody validateEmiRequestBody,
            SavedCard savedCard, String merchantCoftConfig) {
        PaymentOption paymentOption = new PaymentOption();
        paymentOption.setTransactionAmount(validateEmiRequestBody.getPaymentDetails().getTotalTransactionAmount());
        if (savedCard.getCardDetails().getCardType() == null || savedCard.getIssuingBank() == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        paymentOption.setBin6(Integer.parseInt(savedCard.getCardDetails().getFirstSixDigit()));
        paymentOption.setIssuingBank(savedCard.getIssuingBank());
        paymentOption.setIssuingNetworkCode(savedCard.getInstId());
        paymentOption.setPayMethod(savedCard.getCardDetails().getCardType());
        /*
         * if(merchantCoftConfig.equals("PAR")) { String savedId = null; if
         * (savedCard.getPar() != null) { savedId= savedCard.getPar();
         * paymentOption.setCardIndexNo(savedId); } else { String savedCardId =
         * validateEmiRequestBody.getPaymentDetails().getSavedInstrumentId();
         * String requestType = null; { if (savedCardId.length() > 15 &&
         * savedCardId.length() < 45) { requestType = "TIN"; } else if
         * (savedCardId.length() > 15) { requestType = "CIN"; } } savedId =
         * coftTokenDataService.getTokenData(validateEmiRequestBody.getMid(),
         * savedCardId, requestType, merchantCoftConfig);
         * 
         * } paymentOption.setCardIndexNo(savedId); } else {
         * paymentOption.setCardIndexNo(savedCard.getGcin()); }
         */
        return paymentOption;
    }

    public PaymentOption getPaymentOptionWithCardDetailsForCoftTxn(ValidateEmiRequestBody validateEmiRequestBody,
            String merchantCoftConfig) {

        PaymentOption paymentOption = new PaymentOption();
        paymentOption.setTransactionAmount(validateEmiRequestBody.getPaymentDetails().getTotalTransactionAmount());

        String cardNumber = validateEmiRequestBody.getPaymentDetails().getCardNumber();
        int bin6;
        if (StringUtils.isBlank(validateEmiRequestBody.getPaymentDetails().getCardBin6())) {
            setCardIndexNumberAndFirstSixdigitForCoftTxn(validateEmiRequestBody, paymentOption, merchantCoftConfig);
            bin6 = paymentOption.getBin6();
        } else {
            bin6 = Integer.parseInt(validateEmiRequestBody.getPaymentDetails().getCardBin6());
            paymentOption.setBin6(bin6);
        }

        BinDetail binDetail = binDetail(bin6);

        if (binDetail.getCardType() == null || binDetail.getBankCode() == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        String bankCode = binDetail.getBankCode();
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        paymentOption.setPayMethod(binDetail.getCardType());
        if (StringUtils.isNotBlank(cardNumber)) {
            paymentOption.setBin8(parseFistNDigit(cardNumber, 8));
        }

        return paymentOption;
    }

    private void setCardIndexNumberAndFirstSixdigitForCoftTxn(ValidateEmiRequestBody validateEmiRequestBody,
            PaymentOption paymentOption, String merchantCoftConfig) {

        String cacheToken = validateEmiRequestBody.getCacheCardToken();
        Integer firstSixDigitCardNumber = null;

        if (isBlank(validateEmiRequestBody.getCacheCardToken())
                && (validateEmiRequestBody.getPaymentDetails().getCardNumber() != null || validateEmiRequestBody
                        .getPaymentDetails().getCardTokenInfo() != null)) {
            ExpressCardTokenResponse expressCardTokenResponse = expressPaymentService.getCardToken(
                    buildExpressCardTokenRequestForCoftTxns(validateEmiRequestBody), false);
            cacheToken = expressCardTokenResponse.getToken();
        }
        String expressTokenDetailsKey = TheiaConstant.ExtraConstants.EXPRESS_CARD_TOKEN + cacheToken;
        ExpressCardModel tokenDetails = (ExpressCardModel) theiaTransactionalRedisUtil.get(expressTokenDetailsKey);
        if (tokenDetails == null) {
            throw new PaymentRequestValidationException(new ResultInfo(
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getResultStatus(),
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getResultCodeId(),
                    ResultCode.SESSION_EXPIRED_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
        }
        firstSixDigitCardNumber = Integer.valueOf(tokenDetails.getCardBin());
        if (merchantCoftConfig.equals("PAR")) {
            paymentOption.setCardIndexNo(tokenDetails.getUniqueCardIdentifier());
        } else if (merchantCoftConfig.equals("GCIN")) {
            paymentOption.setCardIndexNo(tokenDetails.getUniqueCardIdentifier());
        }
        paymentOption.setBin6(firstSixDigitCardNumber);

        return;

    }

    private String getUpdatedTxnToken(String token, String tokenType, ValidateEmiRequestBody validateEmiRequestBody) {
        if (TokenType.SSO.equals(tokenType)) {
            String mid = validateEmiRequestBody.getMid();
            token = nativeSessionUtil.createTokenForMidSSOFlow(token, mid);
        }
        return token;
    }

    private ExpressCardTokenRequest buildExpressCardTokenRequestForCoftTxns(
            ValidateEmiRequestBody validateEmiRequestBody) {
        ExpressCardTokenRequest expressCardTokenRequest = new ExpressCardTokenRequest();

        String cardInfo = validateEmiRequestBody.getPaymentDetails().getCardNumber();
        if (StringUtils.isNotEmpty(cardInfo)) {
            String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
            if (cardDetails.length != 4) {
                LOGGER.error("Invalid cardDetails length: {}", cardDetails.length);
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseMessage.INVALID_CARD_DETAILS));
            }
            expressCardTokenRequest.setCvv(cardDetails[2].trim());
            expressCardTokenRequest.setCardNumber(cardDetails[1].trim());
            expressCardTokenRequest.setExpiryMonth(cardDetails[3].trim().substring(0, 2));
            expressCardTokenRequest.setExpiryYear(cardDetails[3].trim().substring(2, 6));
        }

        if (validateEmiRequestBody.getPaymentDetails().getCardTokenInfo() != null) {
            expressCardTokenRequest.setCardTokenInfo(validateEmiRequestBody.getPaymentDetails().getCardTokenInfo());
            expressCardTokenRequest.setCardNumber(validateEmiRequestBody.getPaymentDetails().getCardTokenInfo()
                    .getCardToken());
        }
        expressCardTokenRequest.setUserId(validateEmiRequestBody.getCustomerId());
        expressCardTokenRequest.setMid(validateEmiRequestBody.getMid());

        return expressCardTokenRequest;
    }

    public PaymentOption updatePaymentOptionPopulateCardIndexNo(HttpServletRequest request, PaymentOption paymentOption) {
        String mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        IdentifierPaymentOption identifierPaymentOption = buildIdentifierPaymentOption(request, paymentOption);
        buildCardIndexNoForCoftTxns(identifierPaymentOption, mid, request.getParameter(Native.TXN_TOKEN), paymentOption);
        paymentOption.setBin6(parseFistNDigit(identifierPaymentOption.getCardNo(), 6));
        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!identifierPaymentOption.getPayMethod().getMethod().equals(binDetail.getCardType())
                && !((PayMethod.EMI == identifierPaymentOption.getPayMethod() && PayMethod.CREDIT_CARD.getMethod()
                        .equals(binDetail.getCardType())) || (PayMethod.EMI_DC == identifierPaymentOption
                        .getPayMethod() && PayMethod.DEBIT_CARD.getMethod().equals(binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw RequestValidationException.getException();
        }
        String bankCode = binDetail.getBankCode();
        if (StringUtils.isNotBlank(paymentOption.getIssuingBank()) && !paymentOption.getIssuingBank().equals(bankCode)) {
            LOGGER.error("BankCode from card bin is different from bankcode received in the request");
            throw RequestValidationException.getException();
        }
        return paymentOption;
    }

    public void getSavedCardsAndSetUniqueIdentifier(String merchantCoftConfig,
            IdentifierPaymentOption identifierPaymentOption, String savedCardId, String txnToken) {
        if (null != txnToken) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
            SavedCard savedCard = coftTokenDataService.fetchTokenDataFromFPO(savedCardId, cashierInfoResponse);
            if (null != savedCard) {
                if (merchantCoftConfig.equals("PAR")) {
                    identifierPaymentOption.setUniqueIdentifier(savedCard.getPar());
                } else if (merchantCoftConfig.equals("GCIN")) {
                    identifierPaymentOption.setUniqueIdentifier(savedCard.getGcin());
                }
                if (StringUtils.isNotEmpty(identifierPaymentOption.getUniqueIdentifier())) {
                    identifierPaymentOption.setCardNo(savedCard.getCardDetails().getFirstSixDigit());
                }
            }
        }
    }

    public void processForCoftCardNumber(IdentifierPaymentOption emiPaymentOption, String mid, String merchantCoftConfig) {
        String cardNo = emiPaymentOption.getCardNo();
        String savedId = coftTokenDataService.getSavedCardIdFromCardNumber(mid, cardNo, merchantCoftConfig);
        if (StringUtils.isNotEmpty(savedId)) {
            emiPaymentOption.setUniqueIdentifier(savedId);
        } else {
            LOGGER.error("Unable to fetch Saved Card ID for Card Number");
            throw BaseException.getException();
        }
    }

    public void processForCoftTokenCards(IdentifierPaymentOption identifierPaymentOption, String mid,
            String merchantCoftConfig) {
        CardTokenInfo cardTokenInfo = identifierPaymentOption.getCardTokenInfo();
        if (StringUtils.isBlank(cardTokenInfo.getCardToken())) {
            throw RequestValidationException.getException();
        }
        BinDetail binDetail = coftTokenDataService.getCardBinDetails(cardTokenInfo.getCardToken());
        if (binDetail != null && DINERS.getName().equalsIgnoreCase(binDetail.getCardName())) {
            LOGGER.error("For DINERS scheme third-party card token transactions, uniqueIdentifier is not available");
            throw BaseException.getException();
        } else {
            if (StringUtils.isBlank(cardTokenInfo.getPanUniqueReference())) {
                throw RequestValidationException.getException();
            }
            if (merchantCoftConfig.equals("PAR")) {
                identifierPaymentOption.setUniqueIdentifier(cardTokenInfo.getPanUniqueReference());
            } else {
                String uniqueIdentifier = coftTokenDataService.getTokenData(mid, cardTokenInfo.getPanUniqueReference(),
                        "PAR", merchantCoftConfig);
                if (StringUtils.isNotEmpty(uniqueIdentifier)) {
                    identifierPaymentOption.setUniqueIdentifier(uniqueIdentifier);
                } else {
                    LOGGER.error("Unable to fetch uniqueIdentifier for Token");
                    throw BaseException.getException();
                }
            }
            if (binDetail != null && binDetail.getBinAttributes() != null) {
                String accountRangeCardBin = binDetail.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
                if (StringUtils.isNotEmpty(accountRangeCardBin)) {
                    identifierPaymentOption.setCardNo(accountRangeCardBin);
                }
            }
        }
    }

    public void processForCoftSavedCardId(IdentifierPaymentOption emiPaymentOption, String mid,
            String merchantCoftConfig, String txnToken, boolean eightBinHashSupported) {
        {
            String savedCardId = emiPaymentOption.getSavedCardId();

            getSavedCardsAndSetUniqueIdentifier(merchantCoftConfig, emiPaymentOption, savedCardId, txnToken);
            if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                if (StringUtils.isEmpty(emiPaymentOption.getUniqueIdentifier())) {
                    String savedId = coftTokenDataService.getTokenData(mid, savedCardId, "TIN", merchantCoftConfig);
                    if (StringUtils.isNotEmpty(savedId)) {
                        emiPaymentOption.setUniqueIdentifier(savedId);
                    } else {
                        LOGGER.error("Unable to fetch Saved Card ID for TIN");
                        throw BaseException.getException();
                    }
                }
            } else if (savedCardId.length() > 15) {
                if (StringUtils.isEmpty(emiPaymentOption.getUniqueIdentifier())) {
                    QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                            .queryNonSensitiveAssetInfo(null, savedCardId);
                    String savedId = coftTokenDataService.getSavedCardIdFromCardIndexNumber(mid, merchantCoftConfig,
                            savedCardId, queryNonSensitiveAssetInfoResponse);
                    if (StringUtils.isNotEmpty(savedId)) {
                        emiPaymentOption.setUniqueIdentifier(savedId);
                    } else {
                        LOGGER.error("Unable to fetch Saved Card ID for CIN");
                        throw BaseException.getException();
                    }
                }
            }

        }
    }

    private void buildCardIndexNoForCoftTxns(IdentifierPaymentOption identifierPaymentOption, String mid,
            String txnToken, PaymentOption paymentOption) {
        String merchantCoftConfig = coftTokenDataService.getMerchantConfig(mid);
        try {
            if (StringUtils.isEmpty(identifierPaymentOption.getUniqueIdentifier())) {
                if (StringUtils.isNotEmpty(identifierPaymentOption.getSavedCardId())) {
                    processForCoftSavedCardId(identifierPaymentOption, mid, merchantCoftConfig, txnToken, false);
                } else if (Objects.nonNull(identifierPaymentOption.getCardTokenInfo())) {
                    processForCoftTokenCards(identifierPaymentOption, mid, merchantCoftConfig);
                } else if (StringUtils.isNotEmpty(identifierPaymentOption.getCardNo())) {
                    processForCoftCardNumber(identifierPaymentOption, mid, merchantCoftConfig);
                } else {
                    throw RequestValidationException.getException();
                }
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }

        paymentOption.setCardIndexNo(identifierPaymentOption.getUniqueIdentifier());
    }

    private IdentifierPaymentOption buildIdentifierPaymentOption(HttpServletRequest request, PaymentOption paymentOption) {
        IdentifierPaymentOption identifierPaymentOption = new IdentifierPaymentOption();
        identifierPaymentOption.setPayMethod(PayMethod.getPayMethodByMethod(paymentOption.getPayMethod()));
        String cardInfo = request.getParameter(CARD_INFO);
        if (StringUtils.isEmpty(cardInfo)) {
            cardInfo = request.getParameter(PAYMENT_DETAILS);
            if (StringUtils.isNotEmpty(cardInfo) && cardInfo.contains("|")) {
                String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
                if (cardDetails.length == 2) {
                    identifierPaymentOption.setSavedCardId(cardInfo.split(Pattern.quote("|"), -1)[0]);
                } else {
                    identifierPaymentOption.setCardNo(cardInfo.split(Pattern.quote("|"), -1)[0]);
                }
            }
        } else if (cardInfo.contains("|")) {
            String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
            if (cardDetails.length > 1) {
                if (StringUtils.isNotEmpty(cardDetails[1]))
                    identifierPaymentOption.setCardNo(cardDetails[1]);
                if (StringUtils.isNotEmpty(cardDetails[0]))
                    identifierPaymentOption.setSavedCardId(cardDetails[0]);
            }
        }
        String savedId = request.getParameter(TheiaConstant.ExtendedInfoPay.SAVED_CARD_ID);
        if (StringUtils.isNotEmpty(savedId) || StringUtils.isEmpty(identifierPaymentOption.getSavedCardId())) {
            identifierPaymentOption.setSavedCardId(savedId);
        }
        String cardTokenInfo = request.getParameter(CARDTOKENINFO);
        if (StringUtils.isNotEmpty(cardTokenInfo)) {
            try {
                identifierPaymentOption
                        .setCardTokenInfo(JsonMapper.mapJsonToObject(cardTokenInfo, CardTokenInfo.class));
            } catch (FacadeCheckedException fce) {
                LOGGER.error("Error while parsing Card Token");
            }
        }
        LOGGER.info("PaymentOption for EMI : {}", identifierPaymentOption);
        return identifierPaymentOption;
    }
}
