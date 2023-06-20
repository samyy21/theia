package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.enums.BankTransferCheckoutFlow;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.checksum.utils.AggregatorMidKeyUtil;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.InitiateTransactionResponseBody;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.enhancenative.AppInvokeRedirectionUrlData;
import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
import com.paytm.pgplus.theia.nativ.model.token.TrxInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_TPV_FOR_ALL_REQUEST_TYPES;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.EMI_SUBVENTED_TRANSACTION_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.VALIDATE_EMI_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_ENABLE_LINK_FLOW_ON_DQR;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.AUTO_REFUND_MAX_ACCEPTABLE_AMOUNT;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("nativeInitiateTransactionRequestProcessor")
public class NativeInitiateTransactionRequestProcessor
        extends
        AbstractRequestProcessor<NativeInitiateRequest, InitiateTransactionResponse, NativeInitiateRequest, TrxInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeInitiateTransactionRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(NativeInitiateTransactionRequestProcessor.class);

    @Autowired
    @Qualifier("nativePaymentService")
    private INativePaymentService nativePaymentService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;
    @Autowired
    @Qualifier("nativeInitiateUtil")
    private NativeInitiateUtil nativeInitiateUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private Environment environment;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    protected NativeInitiateRequest preProcess(NativeInitiateRequest request) {

        EventUtils.pushTheiaEvents(
                EventNameEnum.ORDER_INITIATED,
                new ImmutablePair<>("REQUEST_TYPE", String
                        .valueOf(RequestProcessorFactory.RequestType.INITIATE_TRANSACTION_REQUEST)));

        setPromoNullIfRequired(request);
        validateSavedCardAutoRefundFlow(request);
        validateAmountForAutoRefundFlow(request);
        getNewPaymentMode(request);
        updateValidationResponseAndItemList(request);
        updateOrderPricingInfo(request);
        NativePersistData validatedNativePersistData = validate(request);
        validatePromoSubventionOrdering(request);
        updateSimplifiedDetailsForMinimalSubventionMerchant(request);
        preProcessSimplifiedSubvention(request);
        updateTxnAmountForPromo(request);
        validateLinkBasedPayment(request);
        updateSimplifiedDetailsForMinimalPromoMerchant(request);
        validatePaymentOfferV2request(request);

        request.setNativePersistData(validatedNativePersistData);
        validateVanCheckoutFlowDetails(request);
        validateAppCallbackUrlFlow(request);
        validateProductAmount(request);

        return request;
    }

    private void validateAppCallbackUrlFlow(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().isNeedAppIntentEndpoint()) {
            if (StringUtils.isEmpty(request.getInitiateTxnReq().getBody().getAppCallbackUrl())
                    || StringUtils.isEmpty(request.getInitiateTxnReq().getBody().getBrowserName())) {
                LOGGER.error("AppCallbackUrl/BrowserName is not passed by Merchant.");
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
    }

    private void validateVanCheckoutFlowDetails(NativeInitiateRequest request) {
        /**
         * Validations which checks for mandatory params in case of Merchant
         * controlled Checkout
         */
        InitiateTransactionRequestBody body = request.getInitiateTxnReq().getBody();
        String bankTransferCheckoutFlow = merchantPreferenceService.getBankTransferCheckoutFlow(body.getMid());
        if (body.getVanInfo() != null) {
            body.getVanInfo().setCheckoutFlow(bankTransferCheckoutFlow);
            if (BankTransferCheckoutFlow.PAYTM_CONTROLLED.getValue().equals(bankTransferCheckoutFlow)
                    && request.getNativePersistData() != null) {
                body.getVanInfo().setIdentificationNo(request.getNativePersistData().getUserDetails().getMobileNo());
            }
        }
    }

    private void updateSimplifiedDetailsForMinimalSubventionMerchant(NativeInitiateRequest request) {

        if (merchantPreferenceService.isMinimalSubventionMerchant(request.getInitiateTxnReq().getBody().getMid())
                && request.getInitiateTxnReq().getBody().getSimplifiedSubvention() == null
                && !StringUtils.isNotBlank(request.getInitiateTxnReq().getBody().getEmiSubventionToken())) {
            SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention();
            simplifiedSubvention.setCustomerId(request.getInitiateTxnReq().getBody().getUserInfo().getCustId());
            simplifiedSubvention.setSelectPlanOnCashierPage(true);
            OfferDetail offerDetails = new OfferDetail();
            offerDetails.setOfferId("1");
            simplifiedSubvention.setOfferDetails(offerDetails);
            if (request.getInitiateTxnReq().getBody().getTxnAmount() != null
                    && StringUtils.isNotBlank(request.getInitiateTxnReq().getBody().getTxnAmount().getValue())) {
                simplifiedSubvention.setSubventionAmount(Double.parseDouble(request.getInitiateTxnReq().getBody()
                        .getTxnAmount().getValue()));
            }
            request.getInitiateTxnReq().getBody().setSimplifiedSubvention(simplifiedSubvention);
        }
    }

    private void preProcessSimplifiedSubvention(NativeInitiateRequest request) {
        if (request != null && request.getInitiateTxnReq() != null && request.getInitiateTxnReq().getBody() != null
                && request.getInitiateTxnReq().getBody().getSimplifiedSubvention() != null) {
            LOGGER.info("Validating contract for simplified subvention");
            SimplifiedSubvention simplifiedSubvention = request.getInitiateTxnReq().getBody().getSimplifiedSubvention();
            simplifiedSubvention.setMid(request.getInitiateTxnReq().getBody().getMid());
            simplifiedSubvention.setCacheCardToken(StringUtils.EMPTY);
            if (request.getInitiateTxnReq().getBody().getTxnAmount() != null
                    && StringUtils.isNotBlank(request.getInitiateTxnReq().getBody().getTxnAmount().getValue())) {
                // temporarily setting total txn amount for validation, txn
                // amount is updated at the time of transaction
                double instantPromoDiscount = 0;
                PaymentOffer paymentOffer = request.getInitiateTxnReq().getBody().getPaymentOffersApplied();
                PaymentOfferV2 paymentOfferV2 = request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2();
                if (Objects.nonNull(paymentOffer) && StringUtils.isNotBlank(paymentOffer.getTotalInstantDiscount())) {
                    instantPromoDiscount = Double.parseDouble(paymentOffer.getTotalInstantDiscount());
                } else if (Objects.nonNull(paymentOfferV2)) {
                    instantPromoDiscount = Double.parseDouble(getDiscountedAmount(paymentOfferV2));
                }
                double totalTxnAmount = Double.parseDouble(request.getInitiateTxnReq().getBody().getTxnAmount()
                        .getValue())
                        - instantPromoDiscount;
                boolean isAmountBasedSubvention = CollectionUtils.isEmpty(simplifiedSubvention.getItems());
                if (isAmountBasedSubvention) {
                    simplifiedSubvention.setPrice(totalTxnAmount);
                }
                if (simplifiedSubvention.getPaymentDetails() != null) {
                    simplifiedSubvention.getPaymentDetails().setCardNumber(StringUtils.EMPTY);
                    simplifiedSubvention.getPaymentDetails().setCardBin6(StringUtils.EMPTY);
                } else {
                    simplifiedSubvention.setPaymentDetails(new PaymentDetails());
                }
                simplifiedSubvention.getPaymentDetails().setTotalTransactionAmount(totalTxnAmount);
            }
            validateSimplifiedSubvention(simplifiedSubvention);
        }
    }

    private void validateSimplifiedSubvention(SimplifiedSubvention request) {
        if (StringUtils.isBlank(request.getCustomerId())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if ((request.getSelectPlanOnCashierPage() == null || !request.getSelectPlanOnCashierPage())
                && StringUtils.isBlank(request.getPlanId())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        try {
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(request);
        } catch (Exception e) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void updateOrderPricingInfo(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().getOrderPricingInfo() != null
                && request.getInitiateTxnReq().getBody().getOrderPricingInfo().getOrderTotalAmount() != null) {
            String val = request.getInitiateTxnReq().getBody().getOrderPricingInfo().getOrderTotalAmount().getValue();
            if (StringUtils.isNotEmpty(val)) {
                request.getInitiateTxnReq().getBody().getOrderPricingInfo().getOrderTotalAmount()
                        .setValue(AmountUtils.getTransactionAmountInPaise(val));
            }
            if (!CollectionUtils.isEmpty(request.getInitiateTxnReq().getBody().getOrderPricingInfo()
                    .getAmountInfoList())) {
                for (OrderPricingInfo.AmountInfoList infoList : request.getInitiateTxnReq().getBody()
                        .getOrderPricingInfo().getAmountInfoList()) {
                    if (infoList.getAmount() == null) {
                        continue;
                    }
                    String value = infoList.getAmount().getValue();
                    if (StringUtils.isNotEmpty(value)) {
                        infoList.getAmount().setValue(AmountUtils.getTransactionAmountInPaise(value));
                    }
                }
            }
        }
    }

    private void validatePromoSubventionOrdering(NativeInitiateRequest request) {

        if (request != null && request.getInitiateTxnReq() != null) {
            InitiateTransactionRequestBody initiateTransactionRequestBody = request.getInitiateTxnReq().getBody();

            /*
             * validating final txn amount with one sent in subvention
             * validation response
             */
            if (initiateTransactionRequestBody != null
                    && initiateTransactionRequestBody.getEmiSubventionToken() != null) {
                Map<String, Object> emiSubventionValidationResponse = initiateTransactionRequestBody
                        .getEmiSubventionValidationResponse();
                if (emiSubventionValidationResponse != null) {
                    String subventedAmount = emiSubventionValidationResponse.get(EMI_SUBVENTED_TRANSACTION_AMOUNT)
                            .toString();
                    ValidateRequest validateRequest = (ValidateRequest) emiSubventionValidationResponse
                            .get(VALIDATE_EMI_REQUEST);

                    if (Double.parseDouble(subventedAmount) != Double.parseDouble(initiateTransactionRequestBody
                            .getPayableAmount().getValue())) {
                        throw RequestValidationException.getException(ResultCode.INVALID_TXN_AMOUNT);
                    }
                    String totalInstantDiscount = "0";
                    if (initiateTransactionRequestBody.getPaymentOffersApplied() != null
                            && StringUtils.isNotBlank(initiateTransactionRequestBody.getPaymentOffersApplied()
                                    .getTotalInstantDiscount())) {
                        totalInstantDiscount = initiateTransactionRequestBody.getPaymentOffersApplied()
                                .getTotalInstantDiscount();
                    } else if (initiateTransactionRequestBody.getPaymentOffersAppliedV2() != null) {
                        PaymentOfferV2 paymentOfferV2 = initiateTransactionRequestBody.getPaymentOffersAppliedV2();
                        totalInstantDiscount = getDiscountedAmount(paymentOfferV2);
                    }

                    Double subventionValidateAmount = Double.parseDouble(initiateTransactionRequestBody.getTxnAmount()
                            .getValue()) - Double.parseDouble(totalInstantDiscount);
                    if (Double.compare(subventionValidateAmount, validateRequest.getPaymentDetails()
                            .getTotalTransactionAmount()) != 0) {
                        throw RequestValidationException.getException(ResultCode.INVALID_TXN_AMOUNT);
                    }
                }
            }
        }
        /*
         * validation for promo being called before emi subvention
         */
    }

    private void updateValidationResponseAndItemList(NativeInitiateRequest request) {
        if (request != null && request.getInitiateTxnReq() != null) {
            InitiateTransactionRequestBody initiateTransactionRequestBody = request.getInitiateTxnReq().getBody();

            if (initiateTransactionRequestBody != null
                    && StringUtils.isNotBlank(initiateTransactionRequestBody.getEmiSubventionToken())) {
                initiateTransactionRequestBody
                        .setEmiSubventionValidationResponse(getContentForSubventionFromRedis(initiateTransactionRequestBody
                                .getEmiSubventionToken()));

                if (initiateTransactionRequestBody.getEmiSubventionValidationResponse() == null
                        || initiateTransactionRequestBody.getEmiSubventionValidationResponse().size() == 0) {
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.SESSION_EXPIRED_EXCEPTION)
                            .isHTMLResponse(false).build();
                }
            }
        }

    }

    private Map<String, Object> getContentForSubventionFromRedis(String subventionToken) {
        return nativeSessionUtil.getEmiSubventionValidationContent(subventionToken);
    }

    private void updateTxnAmountForPromo(NativeInitiateRequest request) {
        if (request != null && request.getInitiateTxnReq() != null) {
            InitiateTransactionRequestBody initiateTransactionRequestBody = request.getInitiateTxnReq().getBody();
            if (initiateTransactionRequestBody != null) {
                /*
                 * PayableAmount is the amount which has been reduced in case
                 * promo has been applied
                 */
                // should be null in case simplifiedPaymentOffer is not null
                Money tempAmount = null;
                if (initiateTransactionRequestBody.getPayableAmount() != null) {

                    LOGGER.info("old txnAmount {}, old payableAmount {}", initiateTransactionRequestBody.getTxnAmount()
                            .getValue(), initiateTransactionRequestBody.getPayableAmount().getValue());

                    /*
                     * we are swapping txnAmount with
                     * totalDiscountedAmount(payableAmount)
                     */
                    tempAmount = initiateTransactionRequestBody.getPayableAmount();
                    updateTxnAmountAndPayableAmount(initiateTransactionRequestBody, tempAmount);
                    LOGGER.info("new txnAmount {}, new payableAmount {}", initiateTransactionRequestBody.getTxnAmount()
                            .getValue(), initiateTransactionRequestBody.getPayableAmount().getValue());
                } else if (null == initiateTransactionRequestBody.getSimplifiedPaymentOffers()
                        && null != initiateTransactionRequestBody.getPaymentOffersApplied()
                        && StringUtils.isNotEmpty(initiateTransactionRequestBody.getPaymentOffersApplied()
                                .getTotalInstantDiscount())) {
                    tempAmount = new Money(String.valueOf(Double.parseDouble(initiateTransactionRequestBody
                            .getTxnAmount().getValue())
                            - Double.parseDouble(initiateTransactionRequestBody.getPaymentOffersApplied()
                                    .getTotalInstantDiscount())));
                    updateTxnAmountAndPayableAmount(initiateTransactionRequestBody, tempAmount);
                } else if (null == initiateTransactionRequestBody.getSimplifiedPaymentOffers()
                        && null != initiateTransactionRequestBody.getPaymentOffersAppliedV2()) {
                    PaymentOfferV2 paymentOfferV2 = initiateTransactionRequestBody.getPaymentOffersAppliedV2();
                    tempAmount = new Money(String.valueOf(Double.parseDouble(initiateTransactionRequestBody
                            .getTxnAmount().getValue()) - Double.parseDouble(getDiscountedAmount(paymentOfferV2))));

                    updateTxnAmountAndPayableAmount(initiateTransactionRequestBody, tempAmount);
                }
            }
        }
    }

    private void updateTxnAmountAndPayableAmount(InitiateTransactionRequestBody initiateTransactionRequestBody,
            Money amount) {
        initiateTransactionRequestBody.setPayableAmount(initiateTransactionRequestBody.getTxnAmount());
        initiateTransactionRequestBody.setTxnAmount(amount);
    }

    private void validateLoginViaCookie(InitiateTransactionRequest request) {
        if (StringUtils.isBlank(request.getBody().getPaytmSsoToken())) {
            String mid = request.getBody().getMid();

            UserDetailsBiz userDetailsBiz = nativeValidationService.validateLoginViaCookie(mid);

            if (userDetailsBiz != null) {
                request.getBody().setPaytmSsoToken(userDetailsBiz.getUserToken());
                request.getBody().setAutoLoginViaCookie(true);
            }

        } else {
            LOGGER.info("Skipping the login via cookie as SSO token is not blank");
        }

    }

    @Override
    protected TrxInfoResponse onProcess(NativeInitiateRequest request, NativeInitiateRequest serviceReq)
            throws Exception {
        TrxInfoResponse trxInfoResponse = new TrxInfoResponse();
        InitiateTokenBody initiateTokenBody = nativePaymentService.initiateTransaction(request);
        if (initiateTokenBody.isIdempotent()) {
            if (!nativeSessionUtil.isIdempotentRequest(request.getInitiateTxnReq().getBody(),
                    initiateTokenBody.getTxnId())) {
                // Fail for Modified request if not idempotent
                throw RequestValidationException.getException(ResultCode.REPEAT_REQUEST_INCONSISTENT);
            }
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put(TheiaConstant.RequestParams.Native.MID, request.getInitiateTxnReq().getBody().getMid());
            HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
            if ((StringUtils.equals(TheiaConstant.ExtraConstants.NATIV_INITIATE_TRANSACTION_URL,
                    servletRequest.getRequestURI())
                    || StringUtils.equals(TheiaConstant.ExtraConstants.NATIV_CUSTOM_INITIATE_TRANSACTION_URL,
                            servletRequest.getRequestURI()) || request.isOrderCreateInInitiate())
                    && !iPgpFf4jClient.checkWithdefault(TheiaConstant.ExtraConstants.BLACKLIST_CREATE_ORDER_INT_TXN,
                            context, true)
                    && iPgpFf4jClient.checkWithdefault(TheiaConstant.ExtraConstants.CREATE_ORDER_INT_TXN, context,
                            false)) {
                try {
                    if ((!ERequestType.NATIVE_ST.getType().equals(
                            request.getInitiateTxnReq().getBody().getRequestType())
                            && !(ERequestType.NATIVE_MF.getType().equals(
                                    request.getInitiateTxnReq().getBody().getRequestType()) && !(request
                                    .getInitiateTxnReq().getBody().getVanInfo() != null || request.getInitiateTxnReq()
                                    .getBody().getTpvInfo() != null)) && !isLinkBasedMutualFundOrStockTradeRequest(request
                            .getInitiateTxnReq().getBody()))
                            && !(merchantPreferenceService.isEligibleForMultipleMBIDFlow(request.getInitiateTxnReq()
                                    .getBody().getMid()) && iPgpFf4jClient.checkWithdefault(
                                    ENABLE_TPV_FOR_ALL_REQUEST_TYPES, context, false))) {
                        processCreateOrder(request.getInitiateTxnReq().getBody(), initiateTokenBody);
                    }
                } catch (Exception e) {
                    LOGGER.error("Create order failed, removing redis keys. Exception details - {}", e.getMessage());
                    nativeSessionUtil.deleteKey(
                            nativeSessionUtil.getMidOrderIdKeyForRedis(request.getInitiateTxnReq()),
                            initiateTokenBody.getTxnId());
                    throw e;
                }
            }
        }
        trxInfoResponse.setTxntoken(initiateTokenBody.getTxnId());
        trxInfoResponse.setIdempotent(initiateTokenBody.isIdempotent());
        return trxInfoResponse;
    }

    public void processCreateOrder(final InitiateTransactionRequestBody initTxnReqBody,
            InitiateTokenBody initiateTokenBody) throws Exception {

        if (TxnType.AUTH.equals(initTxnReqBody.getTxnType()) || TxnType.ESCROW.equals(initTxnReqBody.getTxnType())) {
            LOGGER.info("PreAuth Request, No Order Creation Required");
            return;
        }

        if (nativeInitiateUtil.isMidBlockedCreateOrderInitiateTxnApi(initTxnReqBody.getMid())) {
            LOGGER.info("Mid is blocked for createOrder in InitiateTxnApi");
            return;
        }

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        InitiateTransactionResponse initiateTransactionResponse = null;
        paymentRequestBean.setRequest(servletRequest);
        paymentRequestBean.setRequestType(initTxnReqBody.getRequestType());
        paymentRequestBean.setCreateOrderForInitiateTxnRequest(true);
        paymentRequestBean.setAutoRefund(initTxnReqBody.isAutoRefund());
        paymentRequestBean.setDeviceId(initTxnReqBody.getDeviceId());
        paymentRequestBean
                .setDealsFlow(paymentOffersServiceHelperV2.isDealsFlow(initTxnReqBody.getAffordabilityInfo()));
        ERequestType requestType = ERequestType.getByRequestType(initTxnReqBody.getRequestType());
        if (requestType == null) {
            paymentRequestBean.setRequestType(TheiaConstant.RequestTypes.NATIVE);
        }
        nativeInitiateUtil.transformInitReqBodyToPaymentReqBean(initTxnReqBody, paymentRequestBean);
        initiateTransactionResponse = createInitiateTxnResponse(initiateTokenBody.getTxnId(), initTxnReqBody);
        String paymentPromoCheckoutPromoCode = null;
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.Native.MID, initTxnReqBody.getMid());
        if (initTxnReqBody.getPaymentOffersApplied() != null
                && !CollectionUtils.isEmpty(initTxnReqBody.getPaymentOffersApplied().getOfferBreakup())
                && iPgpFf4jClient.checkWithdefault(
                        TheiaConstant.ExtraConstants.POPULATE_CHECKOUT_PROMO_CODE_IN_CREATE_ORDER, context, false)) {
            paymentPromoCheckoutPromoCode = initTxnReqBody.getPaymentOffersApplied().getOfferBreakup().get(0)
                    .getPromocodeApplied();
        } else if (initTxnReqBody.getPaymentOffersAppliedV2() != null
                && initTxnReqBody.getPaymentOffersAppliedV2().getPromoCode() != null
                && iPgpFf4jClient.checkWithdefault(
                        TheiaConstant.ExtraConstants.POPULATE_CHECKOUT_PROMO_CODE_IN_CREATE_ORDER, context, false)) {
            paymentPromoCheckoutPromoCode = initTxnReqBody.getPaymentOffersAppliedV2().getPromoCode();
        }

        if (initTxnReqBody.isNativeAddMoney()) {
            nativeInitiateUtil.createTopup(initiateTransactionResponse.getBody().getTxnToken(), paymentRequestBean);
        } else {
            nativeInitiateUtil.createOrder(initiateTransactionResponse, paymentRequestBean,
                    paymentPromoCheckoutPromoCode);
        }
    }

    private InitiateTransactionResponse createInitiateTxnResponse(final String token,
            InitiateTransactionRequestBody initTxnReqBody) {

        boolean authentic = !isEmpty(initTxnReqBody.getPaytmSsoToken());
        InitiateTransactionResponseBody body = new InitiateTransactionResponseBody(null, token, authentic);
        return new InitiateTransactionResponse(null, body);
    }

    @Override
    protected InitiateTransactionResponse postProcess(NativeInitiateRequest request, NativeInitiateRequest serviceReq,
            TrxInfoResponse trxInfoResponse) throws Exception {
        boolean isPromoCodeValid = nativeValidationService.validatePromoCode(request.getInitiateTxnReq());
        InitiateTransactionResponse response = createResponse(request.getInitiateTxnReq(), trxInfoResponse,
                isPromoCodeValid);
        return response;
    }

    private NativePersistData validate(NativeInitiateRequest request) {

        NativePersistData validatedNativePersistData = nativeValidationService.validate(request);

        if (TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equals(request.getInitiateTxnReq().getHead()
                .getWorkFlow())) {
            validateLoginViaCookie(request.getInitiateTxnReq());
        }

        if (TheiaConstant.RequestTypes.NATIVE_MF.equalsIgnoreCase(request.getInitiateTxnReq().getBody()
                .getRequestType())
                || (request.getInitiateTxnReq().getBody().getLinkDetailsData() != null && ERequestType.NATIVE_MF
                        .getType().equals(
                                request.getInitiateTxnReq().getBody().getLinkDetailsData().getSubRequestType()))) {
            validateMF(request.getInitiateTxnReq());
        } else if (request.getInitiateTxnReq().getBody().isNativeAddMoney()) {
            validateNativeAddMoney(request.getInitiateTxnReq());
        }

        InitiateTransactionRequestBody body = request.getInitiateTxnReq().getBody();
        if (Objects.nonNull(body.getCardPreAuthType())
                && (body.getTxnType() != TxnType.AUTH && body.getTxnType() != TxnType.ESCROW)) {
            LOGGER.error("Invalid Txn Type : {}, txnType should be \"AUTH\" or \"ESCROW\" for using CardPreAuth",
                    body.getTxnType());
            throw RequestValidationException.getException();
        }

        if (Objects.nonNull(body.getPreAuthBlockSeconds()) && body.getPreAuthBlockSeconds() <= 0) {
            LOGGER.error("Invalid preAuthBlockSeconds : {}", body.getPreAuthBlockSeconds());
            throw RequestValidationException.getException();
        }

        return validatedNativePersistData;
    }

    private void validateNativeAddMoney(InitiateTransactionRequest request) {
        if (StringUtils.isBlank(request.getBody().getPaytmSsoToken())) {
            LOGGER.error("sso token missing in Native Add MOney request");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private boolean isAggregatorMid(String mid) {
        return AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(mid);

    }

    private void validateMF(InitiateTransactionRequest request) {

        try {
            String additionalInfo = JsonMapper.mapObjectToJson(request.getBody().getAdditionalInfo());
            if (additionalInfo != null && additionalInfo.length() > 4096) {
                LOGGER.error("additionalInfo length is greater than 4096 Characters");
                throw RequestValidationException.getException(ResultCode.LARGE_ADDITIONAL_INFO);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(
                    "Exception occured while converting additionalInfo in Json, additionalInfo is not in correct format ",
                    e);
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private InitiateTransactionResponse createResponse(InitiateTransactionRequest request,
            TrxInfoResponse trxInfoResponse, boolean isPromoCodeValid) throws Exception {
        InitiateTransactionResponseBody responseBody = new InitiateTransactionResponseBody();
        responseBody.setTxnToken(trxInfoResponse.getTxntoken());
        responseBody.setAuthenticated(!isEmpty(request.getBody().getPaytmSsoToken()));
        responseBody.setPromoCodeValid(isPromoCodeValid);
        if (trxInfoResponse.isIdempotent()) {
            responseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS_IDEMPOTENT_ERROR));
        }

        setInitiateTxnResponseToCache(trxInfoResponse, responseBody);
        cacheLinkDetailsForQR(request);

        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        InitiateTransactionResponse response = new InitiateTransactionResponse(responseHeader, responseBody);
        setAppIntentUrl(request.getBody(), response);
        return response;
    }

    private void setAppIntentUrl(InitiateTransactionRequestBody request, InitiateTransactionResponse response)
            throws MalformedURLException {
        if (request.isNeedAppIntentEndpoint()) {
            // setting Merchant's App Callback/Intent URL in theia's callback in
            // v1/transactionStatus.
            String appInvokeCallbackURLKey = request.getOrderId().concat("_").concat(request.getMid());
            nativeSessionUtil.setField(appInvokeCallbackURLKey, TheiaConstant.ExtraConstants.APP_INVOKE_CALLBACK_URL,
                    request.getAppCallbackUrl(), 900);

            boolean isV3AppInvokeRequired = processTransactionUtil.isV3AppInvokeRequired(request.getMid());
            AppInvokeRedirectionUrlData url = processTransactionUtil.getUrlDataProvidedSourceUrlForAppInvoke(
                    request.getAppCallbackUrl(), request, response, null, request.getBrowserName());
            String redirectionUrl = isV3AppInvokeRequired ? url.getV3RedirectionUrl() : url.getRedirectionUrl();
            if (StringUtils.isNotBlank(redirectionUrl)) {
                response.getBody().setAppIntentUrl(redirectionUrl);
            }
        }
    }

    private void setInitiateTxnResponseToCache(TrxInfoResponse trxInfoResponse,
            InitiateTransactionResponseBody responseBody) {
        if (!trxInfoResponse.isIdempotent()) {
            nativeSessionUtil.setInitiateTxnResponse(responseBody.getTxnToken(), responseBody);
        }
    }

    private void getNewPaymentMode(NativeInitiateRequest request) {
        List<PaymentMode> disablePaymentMode = request.getInitiateTxnReq().getBody().getDisablePaymentMode();
        List<PaymentMode> enablePaymentMode = request.getInitiateTxnReq().getBody().getEnablePaymentMode();
        List<PaymentMode> newDisablePaymentMode = new ArrayList<>();
        List<PaymentMode> newEnablePaymentMode = new ArrayList<>();

        if (disablePaymentMode != null && !disablePaymentMode.isEmpty()) {
            for (PaymentMode paymentMode : disablePaymentMode) {
                if (EPayMethod.getPayMethodByOldName(paymentMode.getMode()) != null
                        && !(paymentMode.getMode().equals(EPayMethod.COD.getMethod()) || paymentMode.getMode().equals(
                                EPayMethod.PPBL.getOldName()))) {
                    paymentMode.setMode(EPayMethod.getPayMethodByOldName(paymentMode.getMode()).getMethod());
                    newDisablePaymentMode.add(paymentMode);
                } else {
                    newDisablePaymentMode.add(paymentMode);
                }
            }
        }
        if (enablePaymentMode != null && !enablePaymentMode.isEmpty()) {
            for (PaymentMode paymentMode : enablePaymentMode) {
                if (EPayMethod.getPayMethodByOldName(paymentMode.getMode()) != null
                        && !(paymentMode.getMode().equals(EPayMethod.COD.getMethod()) || paymentMode.getMode().equals(
                                EPayMethod.PPBL.getOldName()))) {
                    paymentMode.setMode(EPayMethod.getPayMethodByOldName(paymentMode.getMode()).getMethod());
                    newEnablePaymentMode.add(paymentMode);
                } else {
                    newEnablePaymentMode.add(paymentMode);
                }
            }
        }
        request.getInitiateTxnReq().getBody().setDisablePaymentMode(newDisablePaymentMode);
        request.getInitiateTxnReq().getBody().setEnablePaymentMode(newEnablePaymentMode);

    }

    private void validateLinkBasedPayment(NativeInitiateRequest request) {
        if (request != null && request.getInitiateTxnReq() != null) {
            InitiateTransactionRequestBody initiateTransactionRequestBody = request.getInitiateTxnReq().getBody();
            if (initiateTransactionRequestBody != null && initiateTransactionRequestBody.getLinkDetailsData() != null) {
                LinkDetailResponseBody linkDetailResponseBody = initiateTransactionRequestBody.getLinkDetailsData();
                if (linkDetailResponseBody == null
                        || (ERequestType.LINK_BASED_PAYMENT.getType().equals(
                                initiateTransactionRequestBody.getRequestType()) && StringUtils
                                .isBlank(linkDetailResponseBody.getLinkId()))
                        || (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equals(
                                initiateTransactionRequestBody.getRequestType()) && StringUtils
                                .isBlank(linkDetailResponseBody.getInvoiceId()))
                        || StringUtils.isBlank(linkDetailResponseBody.getLinkName())
                        || StringUtils.isBlank(linkDetailResponseBody.getLinkDescription())
                        || StringUtils.isBlank(linkDetailResponseBody.getShortUrl())
                        || StringUtils.isBlank(linkDetailResponseBody.getLongUrl())
                        || StringUtils.isBlank(linkDetailResponseBody.getSubRequestType())
                        || linkDetailResponseBody.getAmount() == null
                        || Double.parseDouble(initiateTransactionRequestBody.getTxnAmount().getValue()) != linkDetailResponseBody
                                .getAmount()) {
                    throw RequestValidationException.getException(ResultCode.INVALID_LINK_DETAILS_IN_INITIATE_REQUEST);
                }

            }
        }

    }

    private boolean isLinkBasedMutualFundOrStockTradeRequest(
            InitiateTransactionRequestBody initiateTransactionRequestBody) {
        if (initiateTransactionRequestBody.getLinkDetailsData() != null
                && StringUtils.isNotBlank(initiateTransactionRequestBody.getLinkDetailsData().getSubRequestType())
                && (ERequestType.NATIVE_ST.getType().equals(
                        initiateTransactionRequestBody.getLinkDetailsData().getSubRequestType()) || ERequestType.NATIVE_MF
                        .getType().equals(initiateTransactionRequestBody.getLinkDetailsData().getSubRequestType()))) {
            return true;
        }
        return false;
    }

    private void validateSavedCardAutoRefundFlow(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().isAutoRefund()) {
            if (!merchantPreferenceService
                    .isAutoRefundPreferenceEnabled(request.getInitiateTxnReq().getBody().getMid())) {
                LOGGER.error("autoRefund Flag = true, preference disabled");
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
    }

    private void validateAmountForAutoRefundFlow(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().isAutoRefund()) {
            Double requestedTxnAmount = Double.parseDouble(request.getInitiateTxnReq().getBody().getTxnAmount()
                    .getValue());
            Double maxAcceptableTxnAmount = Double.parseDouble(ConfigurationUtil.getProperty(
                    AUTO_REFUND_MAX_ACCEPTABLE_AMOUNT, "50"));
            if (requestedTxnAmount > maxAcceptableTxnAmount) {
                LOGGER.error("Requested Amount {} is greater than Max AcceptableAmount {}", requestedTxnAmount,
                        maxAcceptableTxnAmount);
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
    }

    private void updateSimplifiedDetailsForMinimalPromoMerchant(NativeInitiateRequest request) {

        if (merchantPreferenceService.isMinimalPromoMerchant(request.getInitiateTxnReq().getBody().getMid())
                && request.getInitiateTxnReq().getBody().getSimplifiedPaymentOffers() == null
                && request.getInitiateTxnReq().getBody().getPaymentOffersApplied() == null
                && request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2() == null) {
            SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
            simplifiedPaymentOffers.setApplyAvailablePromo(true);
            request.getInitiateTxnReq().getBody().setSimplifiedPaymentOffers(simplifiedPaymentOffers);
        }
    }

    private void validatePaymentOfferV2request(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2() != null) {
            if (request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2().getVerificationCode() == null
                    || request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2().getPromoContext() == null
                    || request.getInitiateTxnReq().getBody().getPaymentOffersAppliedV2().getPromoCode() == null) {
                LOGGER.error("invalid PaymentOffersAppliedV2 object received in request");
                throw RequestValidationException.getException(ResultCode.PROMO_CODE_VALIDATION_EXCEPTION);
            }
        }
    }

    public String getDiscountedAmount(PaymentOfferV2 paymentOfferV2) {
        String discountAmount = "0";
        if (!CollectionUtils.isEmpty(paymentOfferV2.getSavings())) {
            for (ItemLevelPaymentOffer.PromoSaving saving : paymentOfferV2.getSavings()) {
                if (saving.getSavings() != null) {
                    Optional<RedemptionType> redemptionTypeOptional = RedemptionType.fromString(saving
                            .getRedemptionType());
                    if (redemptionTypeOptional.isPresent() && redemptionTypeOptional.get() == RedemptionType.DISCOUNT) {
                        discountAmount = (saving.getSavings());
                    }
                }
            }
        }
        return discountAmount;
    }

    private void validateProductAmount(NativeInitiateRequest request) {
        if (request.getInitiateTxnReq().getBody().getLinkDetailsData() != null
                && request.getInitiateTxnReq().getBody().getLinkDetailsData().getEdcEmiFields() != null) {
            if (StringUtils.isBlank(request.getInitiateTxnReq().getBody().getLinkDetailsData().getEdcEmiFields()
                    .getProductAmount())) {
                LOGGER.error("Product Amount can't be blank for Edc Link Payment ");
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
    }

    private void setPromoNullIfRequired(NativeInitiateRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.Native.MID, request.getInitiateTxnReq().getBody().getMid());
        if (StringUtils.isNotBlank(request.getInitiateTxnReq().getBody().getPromoCode())
                && iPgpFf4jClient.checkWithdefault(
                        TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT_BY_SETTING_PROMO_VALUE_NULL,
                        context, false)) {
            request.getInitiateTxnReq().getBody().setPromoCode(null);
        }
    }

    private void cacheLinkDetailsForQR(InitiateTransactionRequest request) {
        if (request != null && request.getBody() != null && StringUtils.isNotBlank(request.getBody().getMid())
                && ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(), THEIA_ENABLE_LINK_FLOW_ON_DQR, false)) {
            LinkDetailResponseBody linkDetailResponseBody = request.getBody().getLinkDetailsData();
            if (linkDetailResponseBody != null && StringUtils.isNotBlank(request.getBody().getOrderId())) {
                String midOrderIdToken = nativeSessionUtil.getMidOrderIdToken(request.getBody().getMid(), request
                        .getBody().getOrderId());
                if (StringUtils.isNotBlank(linkDetailResponseBody.getLinkId())) {
                    nativeSessionUtil.setLinkIdForQR(midOrderIdToken, linkDetailResponseBody.getLinkId());
                    EXT_LOGGER.customInfo("Setting linkId :{} in redis against token :{}",
                            linkDetailResponseBody.getLinkId(), midOrderIdToken);
                } else if (StringUtils.isNotBlank(linkDetailResponseBody.getInvoiceId())) {
                    nativeSessionUtil.setInvoiceIdForQR(midOrderIdToken, linkDetailResponseBody.getInvoiceId());
                    EXT_LOGGER.customInfo("Setting invoiceId :{} in redis against token :{}",
                            linkDetailResponseBody.getInvoiceId(), midOrderIdToken);
                }
            }
        }
    }

}