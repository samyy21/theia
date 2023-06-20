package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeRequest;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.core.payment.utils.FeeHelper;
import com.paytm.pgplus.biz.enums.AddMoneySourceEnum;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.SimplifiedSubventionHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.models.SimplifiedPaymentOffers;
import com.paytm.pgplus.models.SimplifiedSubvention;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.PaymentDetails;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequestBody;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequest;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailResponse;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequestBody;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.service.IPaymentOffersService;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.THEIA_ENABLE_PROMOTIONS_IN_PCF;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Service
public class NativeFetchPcfDetailsRequestProcessor
        extends
        AbstractRequestProcessor<FetchPcfDetailRequest, FetchPcfDetailResponse, FetchPcfDetailRequest, FetchPcfDetailResponse> {

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchPcfDetailsRequestProcessor.class);

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private IPaymentOffersService paymentOffersService;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    private ISubventionEmiService subventionEmiService;

    @Autowired
    private SimplifiedSubventionHelper simplifiedSubventionHelper;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Override
    protected FetchPcfDetailRequest preProcess(FetchPcfDetailRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateWithAccessToken(request);
        } else {
            validate(request);
        }
        return request;
    }

    @Override
    protected FetchPcfDetailResponse onProcess(FetchPcfDetailRequest request, FetchPcfDetailRequest serviceRequest) {

        ConsultFeeRequest consultFeeRequest = new ConsultFeeRequest();
        final List<EPayMethod> payMethods = new ArrayList<>();
        final List<String> instId = new ArrayList<>();
        final List<FeeRateFactors> feeRateFactors = new ArrayList<>();
        for (PayChannelOptionView payChannelOptionView : request.getBody().getPayMethods()) {
            FeeRateFactors feeRateFactorsObj = (payChannelOptionView.getFeeRateFactors() != null) ? payChannelOptionView
                    .getFeeRateFactors() : new FeeRateFactors();
            if (PayMethod.PPBL.getOldName().equals(payChannelOptionView.getPayMethod().getOldName())) {
                payMethods.add(EPayMethod.NET_BANKING);
                instId.add(EPayMethod.PPBL.getOldName());
                feeRateFactorsObj.setInstId(EPayMethod.PPBL.getOldName());
            } else {
                payMethods.add(EPayMethod.getPayMethodByMethod(payChannelOptionView.getPayMethod().getMethod()));
                instId.add(payChannelOptionView.getInstId());
                feeRateFactorsObj.setInstId(payChannelOptionView.getInstId());
            }
            feeRateFactors.add(feeRateFactorsObj);
            if (StringUtils.isNotBlank(payChannelOptionView.getAddMoneyAmount())) {
                consultFeeRequest.setAddNPay(true);
                consultFeeRequest.setAddMoneyAmount(new BigDecimal(AmountUtils
                        .getTransactionAmountInPaise(payChannelOptionView.getAddMoneyAmount())));
            }
        }
        final BigDecimal txnAmount = new BigDecimal(AmountUtils.getTransactionAmountInPaise(request.getBody()
                .getTxnAmount()));

        consultFeeRequest.setMerchantId(request.getBody().getMerchantId());
        consultFeeRequest.setPayMethods(payMethods);
        consultFeeRequest.setTransactionAmount(txnAmount);
        consultFeeRequest.setSlabBasedMDR(request.getBody().isSlabBasedMDR());
        consultFeeRequest.setFeeRateFactors(feeRateFactors);
        consultFeeRequest.setDynamicFeeMerchant(request.getBody().isDynamicFeeMerchant());
        consultFeeRequest.setUserId(request.getBody().getUserId());
        consultFeeRequest.setTxnType(request.getBody().getTxnType());

        // support on specific product codes for mdr/pcf edc flow
        if (request.getBody().isDynamicFeeMerchant() && StringUtils.isNotBlank(request.getHead().getToken())) {
            consultFeeRequest.setProductCode(getProductCodeFromCacheForDynamicCharge(request));
        }

        setConsultRequestForAddMoneyAndAddnPay(request, consultFeeRequest);

        if (merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(request.getBody().getMid()))
            consultFeeRequest.setTransactionType(ERequestType.DYNAMIC_QR_2FA);
        consultFeeRequest.setInstId(instId);

        consultFeeRequest.setRoute(Routes.PG2);
        consultFeeRequest.setMerchantId(request.getBody().getMid());
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = bizPaymentService
                .consultBulkConvenienceFees(consultFeeRequest);
        if (!consultFeeResponse.isSuccessfullyProcessed()) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isRetryAllowed(false).isRedirectEnhanceFlow(true)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }

        FetchPcfDetailResponseBody responseBody = new FetchPcfDetailResponseBody();
        responseBody.setConsultDetails(nativePaymentUtil.convertConsultFeeResponse(consultFeeResponse.getResponse()
                .getConsultDetails()));
        return new FetchPcfDetailResponse(new ResponseHeader(), responseBody);
    }

    @Override
    protected FetchPcfDetailResponse postProcess(FetchPcfDetailRequest request, FetchPcfDetailRequest serviceRequest,
            FetchPcfDetailResponse serviceResponse) {

        serviceResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        return serviceResponse;
    }

    private void validate(FetchPcfDetailRequest request) {

        validateRequest(request);
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request.getHead().getTxnToken());
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();

        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        if (!isEmpty(orderDetail.getPaytmSsoToken())) {
            /*
             * This is the case when ssoToken has already been validated in
             * /initiateTxn API, so not validating again
             */
            if (nativeInitiateRequest.getNativePersistData() == null
                    || nativeInitiateRequest.getNativePersistData().getUserDetails() == null) {
                UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(
                        orderDetail.getPaytmSsoToken(), orderDetail.getMid());
                request.getBody().setUserId(userDetailsBiz.getUserId());
            } else {
                request.getBody().setUserId(nativeInitiateRequest.getNativePersistData().getUserDetails().getUserId());
            }
            LOGGER.info("userId :{}", request.getBody().getUserId());
            request.getBody().setSsoToken(orderDetail.getPaytmSsoToken());
        }
        if (!(isAddMoneyPcfEnabled(orderDetail.getMid(), request.getBody().getUserId())
                || merchantPreferenceService.isPostConvenienceFeesEnabled(orderDetail.getMid()) || merchantPreferenceService
                    .isDynamicFeeMerchant(orderDetail.getMid()))) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        if (BooleanUtils.isTrue(orderDetail.isNativeAddMoney())) {
            request.getBody().setTxnType(TheiaConstant.RequestParams.Native.ADDMONEY);
        }
        if (!request.getBody().isUseAmount()) {
            // calling promotions to fetch amount Details
            Map<String, Object> context = new HashMap<>();
            context.put(TheiaConstant.RequestParams.Native.MID, request.getBody().getMid());
            double instantDiscount = 0.0;
            if (iPgpFf4jClient.checkWithdefault(THEIA_ENABLE_PROMOTIONS_IN_PCF, context, false)) {
                instantDiscount = getInstantDiscount(request, nativeInitiateRequest, orderDetail);
            }
            final double txnAmount = Double.parseDouble(orderDetail.getTxnAmount().getValue()) - instantDiscount;
            request.getBody().setTxnAmount(String.valueOf(txnAmount));
        }
        enrich(request, orderDetail.getMid());
    }

    private double getInstantDiscount(FetchPcfDetailRequest pcfDetailRequest,
            NativeInitiateRequest nativeInitiateRequest, InitiateTransactionRequestBody orderDetail) {
        String bin = pcfDetailRequest.getBody().getBin();
        if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null
                && nativeInitiateRequest.getInitiateTxnReq().getBody() != null) {
            double promoDiscount = applyPromo(pcfDetailRequest, nativeInitiateRequest, orderDetail);
            double subventionDiscount = 0D;
            if (StringUtils.isNotBlank(bin) && bin.length() >= 6)
                applySubvention(pcfDetailRequest, nativeInitiateRequest,
                        Double.parseDouble(orderDetail.getTxnAmount().getValue()) - promoDiscount);
            return promoDiscount + subventionDiscount;
        }
        return 0;
    }

    private double applySubvention(FetchPcfDetailRequest pcfDetailRequest, NativeInitiateRequest nativeInitiateRequest,
            double txnAmountInRs) {
        SimplifiedSubvention simplifiedSubvention = null;

        if (nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedSubvention() != null) {
            simplifiedSubvention = nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedSubvention();
        }

        if (null != simplifiedSubvention) {
            ValidateEmiRequestBody validateEmiRequestBody = SubventionEmiServiceHelper.transform(simplifiedSubvention);
            PaymentDetails paymentDetails = validateEmiRequestBody.getPaymentDetails();
            paymentDetails.setCardBin6(StringUtils.substring(pcfDetailRequest.getBody().getBin(), 0, 6));
            ValidateRequest validateRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(
                    validateEmiRequestBody, null, null);
            simplifiedSubventionHelper.updateItemPrices(validateRequest, AmountUtils.getAmountInPaise(txnAmountInRs));
            try {
                GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse = subventionEmiService
                        .validateSubventionEmi(validateRequest);
                ValidateResponse validateResponse = validateEmiServiceResponse.getData();
                return validateResponse.getGratifications().stream()
                        .filter(gratification -> GratificationType.DISCOUNT.equals(gratification.getType()))
                        .mapToDouble(Gratification::getValue).sum();
            } catch (FacadeCheckedException e) {
                throw new RuntimeException("error in validating subvention");
            }
        }
        return 0;
    }

    private double applyPromo(FetchPcfDetailRequest pcfDetailRequest, NativeInitiateRequest nativeInitiateRequest,
            InitiateTransactionRequestBody orderDetail) {
        SimplifiedPaymentOffers simplifiedPaymentOffers = null;

        if (nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedPaymentOffers() != null) {
            simplifiedPaymentOffers = nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedPaymentOffers();
        }

        if (null != simplifiedPaymentOffers) {
            ApplyPromoRequest applyPromoRequest = new ApplyPromoRequest();
            applyPromoRequest.setBody(new ApplyPromoRequestBody());
            applyPromoRequest.setHead(new TokenRequestHeader());

            applyPromoRequest.getHead().setTokenType(TokenType.TXN_TOKEN);
            applyPromoRequest.getHead().setToken(pcfDetailRequest.getHead().getTxnToken());
            applyPromoRequest.getHead().setChannelId(pcfDetailRequest.getHead().getChannelId());
            applyPromoRequest.getBody().setMid(orderDetail.getMid());
            applyPromoRequest.getBody().setOrderId(orderDetail.getOrderId());
            applyPromoRequest.getBody().setPromocode(simplifiedPaymentOffers.getPromoCode());
            applyPromoRequest.getBody().setTotalTransactionAmount(getTxnAmount(orderDetail));
            applyPromoRequest.getBody().setPromoForPCFMerchant(true);
            PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
            String pfcDetailRequestPaymethod = pcfDetailRequest.getBody().getPayMethods().get(0).getPayMethod().name();
            promoPaymentOption.setPayMethod(com.paytm.pgplus.enums.PayMethod.valueOf(pfcDetailRequestPaymethod));
            promoPaymentOption.setTransactionAmount(applyPromoRequest.getBody().getTotalTransactionAmount());
            applyPromoRequest.getBody().setPaymentOptions(Collections.singletonList(promoPaymentOption));

            ApplyPromoResponse response = paymentOffersService.applyPromo(applyPromoRequest, "V1", null);
            if (response != null && response.getBody() != null && response.getBody().getPaymentOffer() != null
                    && StringUtils.isNotBlank(response.getBody().getPaymentOffer().getTotalInstantDiscount())) {
                return Double.parseDouble(response.getBody().getPaymentOffer().getTotalInstantDiscount());
            }
        }
        return 0;
    }

    private void enrich(FetchPcfDetailRequest request, String mid) {
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(mid);
        boolean isDynamicFeeMerchant = merchantPreferenceService.isDynamicFeeMerchant(mid);
        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(mid);
        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            request.getBody().setMerchantId(merchantMappingResponse.getResponse().getAlipayId());
        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        if (StringUtils.isBlank(request.getBody().getMid())) {
            request.getBody().setMid(mid);
        }
        request.getBody().setSlabBasedMDR(isSlabBasedMdr);
        request.getBody().setDynamicFeeMerchant(isDynamicFeeMerchant);

    }

    private void validateWithSsoToken(FetchPcfDetailRequest request) {
        validateRequest(request);
        String mid = request.getBody().getMid();
        nativeValidationService.validateMid(mid);
        UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(request.getHead().getToken(), mid);
        request.getBody().setUserId(userDetailsBiz.getUserId());

        if ((!isAddMoneyPcfEnabled(mid, request.getBody().getUserId()))
                && (!merchantPreferenceService.isPostConvenienceFeesEnabled(mid) && !merchantPreferenceService
                        .isDynamicFeeMerchant(mid))) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);

        }
        enrich(request, mid);

    }

    private void validateWithAccessToken(FetchPcfDetailRequest request) {
        validateRequest(request);
        String mid = request.getBody().getMid();
        nativeValidationService.validateMid(mid);
        accessTokenUtils.validateAccessToken(mid, request.getBody().getReferenceId(), request.getHead().getToken());

        if ((merchantPreferenceService.isAddMoneyPcfDisabled(mid, false))
                && (!merchantPreferenceService.isPostConvenienceFeesEnabled(mid) && !merchantPreferenceService
                        .isDynamicFeeMerchant(mid))) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);

        }
        enrich(request, mid);

    }

    private void validateRequest(FetchPcfDetailRequest request) {
        if (request.getBody() == null || request.getHead() == null) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        if (request.getBody().getPayMethods() == null) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        for (PayChannelOptionView payChannelOptionView : request.getBody().getPayMethods()) {
            if (EPayMethod.getPayMethodByMethod(payChannelOptionView.getPayMethod().getMethod()) == null) {
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }

    }

    private ProductCodes getProductCodeFromCacheForDynamicCharge(FetchPcfDetailRequest request) {

        String token = nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody()
                .getMid());
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(token);
        if (cashierInfoResponse != null && cashierInfoResponse.getBody() != null
                && StringUtils.isNotBlank(cashierInfoResponse.getBody().getProductCode())) {
            String productCode = cashierInfoResponse.getBody().getProductCode();
            List<String> productCodeList = FeeHelper.getProductCodesForDynamicChargeSupport();
            if (CollectionUtils.isNotEmpty(productCodeList) && productCodeList.contains(productCode)) {
                try {
                    return ProductCodes.getProductById(productCode);
                } catch (FacadeInvalidParameterException e) {
                    LOGGER.error("Could not set product code because : ", e);
                }
            }
        }
        return null;
    }

    private String getTxnAmount(InitiateTransactionRequestBody orderDetail) {
        SimplifiedPaymentOffers simplifiedPaymentOffers = orderDetail.getSimplifiedPaymentOffers();
        String txnAmount = (simplifiedPaymentOffers != null && StringUtils.isNotBlank(simplifiedPaymentOffers
                .getPromoAmount())) ? AmountUtils.getTransactionAmountInPaise(simplifiedPaymentOffers.getPromoAmount())
                : orderDetail.getTxnAmount().getValue();
        return AmountUtils.getTransactionAmountInRupee(txnAmount);
    }

    private void setConsultRequestForAddMoneyAndAddnPay(FetchPcfDetailRequest request,
            ConsultFeeRequest consultFeeRequest) {
        if (isAddMoneyPcfEnabled(request.getBody().getMid(), consultFeeRequest.getUserId())) {
            LOGGER.info("Inside consult fee request");
            checkAndSetIfAppNewVersion(request, consultFeeRequest);
            final ConsultWalletLimitsRequest walletConsultRequest;
            if (TheiaConstant.RequestParams.Native.ADDANDPAY.equals(consultFeeRequest.getTxnType())
                    || consultFeeRequest.isAddNPay()
                    || TheiaConstant.RequestParams.Native.ADDMONEY.equals(consultFeeRequest.getTxnType())) {
                String addMoneyPayMethod = null;
                consultFeeRequest.setAddMoneyPcfEnabled(true);
                for (EPayMethod payMethod : consultFeeRequest.getPayMethods()) {
                    if (!EPayMethod.BALANCE.equals(payMethod)) {
                        addMoneyPayMethod = payMethod.getMethod();
                        break;
                    }
                }
                if (TheiaConstant.RequestParams.Native.ADDMONEY.equals(consultFeeRequest.getTxnType())) {
                    walletConsultRequest = new ConsultWalletLimitsRequest(consultFeeRequest.getTransactionAmount()
                            .doubleValue(), consultFeeRequest.getUserId(), null);
                } else {
                    consultFeeRequest.setAddNPayProductCode(request.getBody().getProductCode());
                    walletConsultRequest = new ConsultWalletLimitsRequest(consultFeeRequest.getAddMoneyAmount()
                            .doubleValue(), consultFeeRequest.getUserId(), null);
                    walletConsultRequest.setAddAndPay(true);
                }
                walletConsultRequest.setTotalTxnAmount(consultFeeRequest.getTransactionAmount().doubleValue());
                walletConsultRequest.setPaymentMode(addMoneyPayMethod);
                walletConsultRequest.setSource(AddMoneySourceEnum.THIRD_PARTY.getValue());
                setWalletConsultRequest(walletConsultRequest, consultFeeRequest);
                BizWalletConsultResponse bizWalletConsultResponse = bizPaymentService
                        .walletLimitsConsultV2(walletConsultRequest);
                if (bizWalletConsultResponse != null
                        && CollectionUtils.isNotEmpty(bizWalletConsultResponse.getFeeDetails())) {
                    List<Map<String, Object>> feeDetails = bizWalletConsultResponse.getFeeDetails();
                    String feeRateCode = getConsultFeeRateCode(feeDetails, addMoneyPayMethod);
                    if (StringUtils.isNotEmpty(feeRateCode)) {
                        consultFeeRequest.setFeeRateCode(feeRateCode);
                    }
                }
            }
        }
    }

    private String getConsultFeeRateCode(List<Map<String, Object>> feeDetails, String addMoneyPayMethod) {
        for (Map<String, Object> feeItem : feeDetails) {
            if (MapUtils.isNotEmpty(feeItem)) {
                String payMethod = (String) feeItem.get(BizConstant.PAYMENT_METHOD);
                if (StringUtils.isNotBlank(payMethod) && payMethod.equalsIgnoreCase(addMoneyPayMethod)
                        && StringUtils.isNotBlank((String) feeItem.get(BizConstant.FEE_RATE_CODE))) {

                    String feeRateCode = (String) feeItem.get(BizConstant.FEE_RATE_CODE);
                    return feeRateCode;
                }
            }
        }
        return null;
    }

    private void setWalletConsultRequest(ConsultWalletLimitsRequest walletConsultRequest,
            ConsultFeeRequest consultFeeRequest) {
        if (CollectionUtils.isNotEmpty(consultFeeRequest.getFeeRateFactors())) {
            for (FeeRateFactors feeRateFactors : consultFeeRequest.getFeeRateFactors()) {
                if (StringUtils.isNotBlank(feeRateFactors.getInstId())) {
                    walletConsultRequest.setBankId(feeRateFactors.getInstId());
                }
                if (feeRateFactors.isPrepaidCard()) {
                    walletConsultRequest.setPrepaidCard(true);
                }
                if (feeRateFactors.isCorporateCard()) {
                    walletConsultRequest.setCorporateCard(true);
                }
            }
        }
    }

    private void checkAndSetIfAppNewVersion(FetchPcfDetailRequest request, ConsultFeeRequest consultFeeRequest) {
        if (TheiaConstant.RequestParams.Native.ADDANDPAY.equals(consultFeeRequest.getTxnType())
                || consultFeeRequest.isAddNPay()) {
            if (!(StringUtils.equals(request.getHead().getWorkFlow(), BizConstant.ENHANCED_CASHIER_FLOW) || StringUtils
                    .equals(request.getHead().getWorkFlow(), BizConstant.CHECKOUT))) {
                String token = request.getHead().getTxnToken();
                if (StringUtils.isNotEmpty(token)) {
                    theiaSessionRedisUtil.hsetIfExist(token, BizConstant.APP_NEW_VERSION_FLOW, true);
                }
            }
        }
    }

    private boolean isAddMoneyPcfEnabled(String mid, String userId) {
        if ((!merchantPreferenceService.isAddMoneyPcfDisabled(mid, false))
                && StringUtils.isNotBlank(userId)
                && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_ADD_MONEY_SURCHARGE, mid, null, userId,
                        false)) {
            return true;
        }
        return false;
    }
}
