/**
 *
 */
package com.paytm.pgplus.cashier.facade.service.impl;

import java.math.BigDecimal;
import java.util.*;

import com.paytm.pgplus.cashier.exception.RiskVerificationRequiredException;
import com.paytm.pgplus.cashier.util.RouteUtil;
import com.paytm.pgplus.facade.common.model.*;
import com.paytm.pgplus.facade.merchantlimit.utils.MerchantLimitUtil;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequestBody;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.exception.RouterException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.RiskRejectionErrorMessage;
import com.paytm.pgplus.pgproxycommon.utils.RiskRejectInfoCodesAndMessages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.IMPSCardRequest;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.models.CashierFacadeMapper;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.looper.model.CashierLopperMapper;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.payoption.IPayOptionBillGenerator;
import com.paytm.pgplus.cashier.payoption.PayOptionBillGeneratorFactory;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequest;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequestBody;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.boss.models.ConsultDetail;
import com.paytm.pgplus.facade.boss.models.PayMethodDetail;
import com.paytm.pgplus.facade.boss.models.request.ChargeFeeConsultRequest;
import com.paytm.pgplus.facade.boss.models.request.ChargeFeeConsultRequestBody;
import com.paytm.pgplus.facade.boss.models.response.ChargeFeeConsultResponse;
import com.paytm.pgplus.facade.boss.services.ICharge;
import com.paytm.pgplus.facade.common.model.EnvInfo.EnvInfoBuilder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.EnumCurrency;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequest;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequestBody;
import com.paytm.pgplus.facade.fund.models.request.QueryByFundOrderIdRequest;
import com.paytm.pgplus.facade.fund.models.request.QueryByFundOrderIdRequestBody;
import com.paytm.pgplus.facade.fund.models.response.QueryByFundOrderIdResponse;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.payment.models.PayOptionBill;
import com.paytm.pgplus.facade.payment.models.request.PayRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResponse;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.risk.enums.BizSceneEnum;
import com.paytm.pgplus.facade.risk.enums.RiskResultEnum;
import com.paytm.pgplus.facade.risk.models.request.RiskRequest;
import com.paytm.pgplus.facade.risk.models.request.RiskRequestBody;
import com.paytm.pgplus.facade.risk.models.response.RiskResponse;
import com.paytm.pgplus.facade.risk.services.IRisk;
import com.paytm.pgplus.facade.user.enums.CardType;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody.CacheCardRequestBodyBuilder;
import com.paytm.pgplus.facade.user.models.request.ValidatePaymentOtpRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.facade.user.models.response.ValidatePaymentOtpResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import com.paytm.pgplus.facade.utils.EventUtil;

@Component("facadeServiceImpl")
public class FacadeServiceImpl implements IFacadeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacadeServiceImpl.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String USER_ID = "userId";
    private static final String CARD_CACHE_TOKEN = "cardCacheToken";
    private static String RISK_REJECT_PROPERTY_PREFIX = "risk.reject";
    private static String USER_MESSAGE = "user.message";
    private static String PRIORITY = "priority";

    @Autowired
    IAsset assetImpl;

    @Autowired
    ICashier cashierImpl;

    @Autowired
    IAuthentication authenticationImpl;

    @Autowired
    IAcquiringOrder acquiringOrderImpl;

    @Autowired
    IRisk riskImpl;

    @Autowired
    ICharge chargeImpl;

    @Autowired
    IAcquiringOrder acquiringOrder;

    @Autowired
    ITopup topupImpl;

    @Autowired
    RouteUtil routeUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    public static String ENABLE_STORE_IN_CACHE_ONLY = "theia.enableStoreInCacheOnly";
    public static String DISABLE_CARD_INDEX_NUMBER = "theia.disableCardIndexNumber";

    /*
     * Method send the cache card request to facade
     * 
     * @return cache card token id
     */
    @Override
    public CacheCardResponseBody getCacheCardTokenId(final CompleteCardRequest completeCardReqest)
            throws CashierCheckedException, PaytmValidationException {

        validateRequest(completeCardReqest);
        InstNetworkType instNetworkType = completeCardReqest.getInstNetworkType();

        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);

            CacheCardRequestBody body;

            switch (instNetworkType) {
            case IMPS:
                body = prepareBodyForIMPSPayRequest(completeCardReqest, instNetworkType);
                break;
            case ISOCARD:
                body = prepareBodyForCardPayRequest(completeCardReqest, instNetworkType);
                break;
            case IFSC:
            case UPI:
            default:
                throw new CashierCheckedException("Process failed : instNetworkType not supported ");
            }

            CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            if (null != cacheCardRequest) {
                CacheCardRequestBody cacheCardRequestBody = cacheCardRequest.getBody();
                if ((cacheCardRequestBody.getInstNetworkType() == InstNetworkType.COFT)
                        || cacheCardRequestBody.getInstNetworkType() == InstNetworkType.ISOCARD) {
                    if (iPgpFf4jClient.checkWithdefault(ENABLE_STORE_IN_CACHE_ONLY, null, false)) {
                        cacheCardRequestBody.setStoreInCacheOnly(true);
                    }
                    if (iPgpFf4jClient.checkWithdefault(DISABLE_CARD_INDEX_NUMBER, null, false)) {
                        cacheCardRequestBody.setCardIndexNo(null);
                    }
                }
            }
            CacheCardResponse cacheCardResponse = assetImpl.cacheCard(cacheCardRequest);
            validateCacheCardResponse(cacheCardResponse);

            ResultInfo resultInfo = cacheCardResponse.getBody().getResultInfo();

            if (ResultCode.SUCCESS.getResultStatus().equalsIgnoreCase(resultInfo.getResultStatus())) {
                return cacheCardResponse.getBody();
            } else if (ResultCode.PARAM_ILLEGAL.getCode().equalsIgnoreCase(resultInfo.getResultCode())) {
                LOGGER.error("Result Info received : {}", resultInfo);
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PARAM_CACHE_CARD);
            } else {
                throw new CashierCheckedException("Result Info received : " + mapper.writeValueAsString(resultInfo));
            }
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Cashier Internal system error", e);
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException("Facde Internal system error", e);
        }

    }

    /**
     * @param completeCardReqest
     * @param instNetworkType
     * @return
     * @throws FacadeCheckedException
     * @throws FacadeInvalidParameterException
     */
    private CacheCardRequestBody prepareBodyForIMPSPayRequest(final CompleteCardRequest completeCardReqest,
            InstNetworkType instNetworkType) throws FacadeCheckedException {
        CacheCardRequestBody body;
        IMPSCardRequest impsCardRequest = completeCardReqest.getImpsCardRequest();

        body = new CacheCardRequestBodyBuilder().cardNo(impsCardRequest.getMmid())
                .holderMobileNo(impsCardRequest.getHolderMobileNo()).otp(impsCardRequest.getOtp())
                .instNetworkType(instNetworkType).instNetworkCode(impsCardRequest.getInstNetworkCode()).build();
        return body;
    }

    /**
     * @param completeCardReqest
     * @param instNetworkType
     * @return
     * @throws FacadeInvalidParameterException
     * @throws FacadeCheckedException
     */
    private CacheCardRequestBody prepareBodyForCardPayRequest(final CompleteCardRequest completeCardReqest,
            InstNetworkType instNetworkType) throws FacadeCheckedException {
        CacheCardRequestBody body;
        BankCardRequest bankCardRequest = completeCardReqest.getBankCardRequest();
        BinCardRequest binCardRequest = completeCardReqest.getBinCardRequest();
        String cardSchema = binCardRequest.getCardScheme().getScheme();
        String cardType = binCardRequest.getCardType().getValue();

        CacheCardRequestBodyBuilder builder = new CacheCardRequestBodyBuilder();
        builder.cardNo(bankCardRequest.getCardNo()).cardScheme(cardSchema)
                .cardType(CardType.getCardTypebyValue(cardType)).cvv2(bankCardRequest.getCvv())
                .instNetworkType(instNetworkType).instNetworkCode(instNetworkType.getNetworkType())
                .instId(binCardRequest.getInstId());

        if (!StringUtils.isEmpty(bankCardRequest.getExpiryMonth())) {
            builder.expiryMonth(Short.valueOf(bankCardRequest.getExpiryMonth()));
        }

        if (!StringUtils.isEmpty(bankCardRequest.getExpiryYear())) {
            builder.expiryYear(Short.valueOf(bankCardRequest.getExpiryYear()));
        }

        body = builder.build();
        return body;
    }

    @Override
    public String getCashierRequestId(PaymentRequest paymentRequest, List<CashierPayOptionBill> cashierPayOptionBills)
            throws CashierCheckedException, PaytmValidationException {
        if ((null == paymentRequest) || cashierPayOptionBills.isEmpty()) {
            throw new CashierCheckedException("process failed : payment request can not be null");
        }

        try {
            PayRequest payRequest = CashierFacadeMapper.buildPayRequest(paymentRequest, cashierPayOptionBills);
            PayResponse payResponse = cashierImpl.pay(payRequest);
            validatePayResponse(payResponse);
            ResultInfo resultInfo = payResponse.getBody().getResultInfo();
            EventUtil.payLog(payResponse, payRequest);
            if (ResultCode.ACCEPTED_SUCCESS.getCode().equalsIgnoreCase(resultInfo.getResultCode())) {
                LOGGER.info("Cashier Request Id ::{}", payResponse.getBody().getCashierRequestId());
                return payResponse.getBody().getCashierRequestId();
            } else if (ResultCode.RISK_REJECT.getResultCodeId().equalsIgnoreCase(resultInfo.getResultCodeId())) {

                if (payResponse.getBody().getSecurityPolicyResult() != null
                        && payResponse.getBody().getSecurityPolicyResult().getRiskResult() != null) {

                    Map<String, String> infoCodeMessageMap = RiskRejectInfoCodesAndMessages
                            .fetchUserMessageAccToPriorityFromInfoCodeListWithCode(payResponse.getBody()
                                    .getSecurityPolicyResult().getRiskResult().getRiskInfo());
                    String riskMessage = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.MESSAGE);
                    String internalErrorCode = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.INFOCODE);

                    if (StringUtils.isBlank(riskMessage)) {
                        riskMessage = PaytmValidationExceptionType.INVALID_RISK_PARAM.getValidationFailedMsg();
                    }

                    PaytmValidationExceptionType paytmValidationExceptionType = PaytmValidationExceptionType
                            .getTypeForMessage(riskMessage);
                    if (paytmValidationExceptionType != null) {
                        throw new PaytmValidationException(paytmValidationExceptionType);
                    }

                    throw new PaytmValidationException(internalErrorCode, riskMessage);
                } else {
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_RISK_PARAM);
                }
            } else if (ResultCode.TRANSACTION_CLOSED.getCode().equals(resultInfo.getResultCode())) {
                throw new PaytmValidationException(PaytmValidationExceptionType.TRANSACTION_CLOSED);
            } else if (ResponseConstants.RISK_VERIFICATION.toString().equals(resultInfo.getResultCode())) {
                throw new RiskVerificationRequiredException(payResponse.getBody().getSecurityPolicyResult()
                        .getSecurityId(), payResponse.getBody().getSecurityPolicyResult().getRiskResult()
                        .getVerificationMethod().getMethod());
            } else if (ResultCode.PARAM_ILLEGAL.getResultStatus().equalsIgnoreCase(resultInfo.getResultStatus())) {
                MerchantLimitUtil.checkIfMerchantLimitBreached(resultInfo.getResultCodeId());
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PARAM_CASHIER_PAY);
            } else {
                throw new CashierCheckedException("Result Info received : " + mapper.writeValueAsString(resultInfo));
            }
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException("Facade checked exception : ", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Json processing exception : ", e);
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_TRANSACTION_STATUS)
    public CashierTransactionStatus queryByAcquirementId(String merchantId, String acquirementId, boolean needFullInfo,
            boolean fromAoaMerchant) throws CashierCheckedException {
        try {

            ApiFunctions apiFunction = ApiFunctions.QUERY_BY_ACQUIREMENTID;
            if (fromAoaMerchant) {
                apiFunction = ApiFunctions.AOA_QUERY_BY_ACQUIREMENTID;
            }

            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);

            QueryByAcquirementIdRequestBody body = new QueryByAcquirementIdRequestBody(merchantId, acquirementId,
                    needFullInfo, fromAoaMerchant);

            QueryByAcquirementIdRequest queryByAcquirementIdRequest = new QueryByAcquirementIdRequest(head, body);

            QueryByAcquirementIdResponse queryByAcquirementIdResponse = acquiringOrderImpl
                    .queryByAcquirementId(queryByAcquirementIdRequest);

            validateQueryByAcquirementIdResponse(queryByAcquirementIdResponse);

            ResultInfo resultInfo = queryByAcquirementIdResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                return CashierLopperMapper.buildCashierTrasactionStatus(queryByAcquirementIdResponse.getBody());
            }
            throw new CashierCheckedException("Unable to get response, Result Info received : "
                    + mapper.writeValueAsString(resultInfo));

        } catch (FacadeCheckedException ex) {
            throw new CashierCheckedException("Process failed : unable to get response for query by acquriment id", ex);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Internal system error", e);
        }
    }

    @Override
    public boolean validatePaymentOTP(final String code, final String token, final Long otp)
            throws CashierCheckedException {
        ValidatePaymentOtpRequest validatePaymentOtpRequest = new ValidatePaymentOtpRequest(otp, code, token);

        try {
            ValidatePaymentOtpResponse validatePaymentOtpResponse = authenticationImpl
                    .validatePaymentOtp(validatePaymentOtpRequest);
            return validatePaymentOtpResponse.isOtpVerified();
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException("Internal server error for payment OTP validation", e);
        }
    }

    /**
     * @param completeCardRequest
     * @throws CashierCheckedException
     */
    private void validateRequest(final CompleteCardRequest completeCardRequest) throws CashierCheckedException {
        if (null == completeCardRequest) {
            throw new CashierCheckedException("Process failed : card request can not be null");
        }
    }

    /**
     * @param cacheCardResponse
     * @throws CashierCheckedException
     */
    private void validateCacheCardResponse(CacheCardResponse cacheCardResponse) throws CashierCheckedException {
        if (null == cacheCardResponse) {
            throw new CashierCheckedException("Process failed : cacheCardResponse received as null");
        }

        if (null == cacheCardResponse.getBody()) {
            throw new CashierCheckedException("Process failed : cacheCardResponse body received as null");
        }

        if (null == cacheCardResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException("Process failed : cacheCardResponse body result Info received as null");
        }
    }

    /**
     * @param payResponse
     * @throws CashierCheckedException
     */
    private void validatePayResponse(PayResponse payResponse) throws CashierCheckedException {
        if (null == payResponse) {
            throw new CashierCheckedException("Process failed : payResponse received as null");
        }

        if (null == payResponse.getBody()) {
            throw new CashierCheckedException("Process failed : payResponse body received as null");
        }

        if (null == payResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException("Process failed : payResponse : body : Result Info : received as null");
        }
    }

    /**
     * @param queryByAcquirementIdResponse
     * @throws CashierCheckedException
     */
    private void validateQueryByAcquirementIdResponse(QueryByAcquirementIdResponse queryByAcquirementIdResponse)
            throws CashierCheckedException {
        if (null == queryByAcquirementIdResponse) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse received as null");
        }

        if (null == queryByAcquirementIdResponse.getBody()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body received as null");
        }

        if (null == queryByAcquirementIdResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body Result Info received as null");
        }
    }

    @Override
    public boolean riskConsultResponse(CashierRequest cashierRequest, String cacheCardToken, String userId)
            throws FacadeCheckedException {
        boolean riskApplied;
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.RISK_POLICY_CONSULT);
        RiskRequestBody riskRequestBody = buildRiskRequestBodyForCard(cashierRequest, cacheCardToken);
        RiskRequest riskRequest = new RiskRequest(head, riskRequestBody);

        // Check only for the card number
        RiskResponse riskResponse = riskImpl.riskPolicyConsult(riskRequest);

        riskApplied = validateRiskResponse(riskResponse);

        if (riskApplied) {
            return riskApplied;
        }

        if (!StringUtils.isBlank(userId)
                && cashierRequest.getPaymentRequest().getPayBillOptions().getPayOptions()
                        .containsKey(PayMethod.CREDIT_CARD)) {
            riskRequestBody = buildRiskRequestBodyForUser(cashierRequest, userId);
            riskRequest = new RiskRequest(head, riskRequestBody);

            // Check only for the user id
            riskResponse = riskImpl.riskPolicyConsult(riskRequest);

            riskApplied = validateRiskResponse(riskResponse);
        }

        return riskApplied;
    }

    /**
     * @param riskApplied
     * @param riskResponse
     * @return
     */
    private boolean validateRiskResponse(RiskResponse riskResponse) {
        if ((riskResponse == null) || (riskResponse.getBody() == null)) {
            LOGGER.error("No response received from RISK_POLICY_CONSULT");
            return false;
        }

        if (RiskResultEnum.REJECT == riskResponse.getBody().getRiskResult()) {
            LOGGER.info("Risk applied on the provided field");
            return true;
        }
        return false;
    }

    /**
     * @param cashierRequest
     * @param cacheCardToken
     * @param userId
     * @return
     * @throws FacadeInvalidParameterException
     */
    private RiskRequestBody buildRiskRequestBodyForCard(CashierRequest cashierRequest, String cacheCardToken)
            throws FacadeInvalidParameterException {
        Date date = new Date();
        CashierEnvInfo cashierEnvInfo = cashierRequest.getPaymentRequest().getCashierEnvInfo();

        EnvInfo envInfo = buildEnvInfoForRiskRequest(cashierEnvInfo);

        Map<String, String> extendInfoMap = new HashMap<>();
        extendInfoMap.put(CARD_CACHE_TOKEN, cacheCardToken);

        String extendInfo = "";
        try {
            extendInfo = mapper.writeValueAsString(extendInfoMap);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to parse the object", e);
        }

        return new RiskRequestBody(date, BizSceneEnum.NAMELIST_QUERY, envInfo, extendInfo);
    }

    private RiskRequestBody buildRiskRequestBodyForUser(CashierRequest cashierRequest, String userId)
            throws FacadeInvalidParameterException {
        Date date = new Date();
        CashierEnvInfo cashierEnvInfo = cashierRequest.getPaymentRequest().getCashierEnvInfo();

        EnvInfo envInfo = buildEnvInfoForRiskRequest(cashierEnvInfo);

        Map<String, String> extendInfoMap = new HashMap<>();
        extendInfoMap.put(USER_ID, userId);

        String extendInfo = "";
        try {
            extendInfo = mapper.writeValueAsString(extendInfoMap);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to parse the object", e);
        }

        return new RiskRequestBody(date, userId, BizSceneEnum.NAMELIST_QUERY, envInfo, extendInfo);
    }

    /**
     * @param cashierEnvInfo
     * @return
     * @throws FacadeInvalidParameterException
     */
    private EnvInfo buildEnvInfoForRiskRequest(CashierEnvInfo cashierEnvInfo) throws FacadeInvalidParameterException {
        EnvInfo envInfo = new EnvInfoBuilder(cashierEnvInfo.getClientIp(), cashierEnvInfo.getTerminalType()
                .getTerminal()).clientKey(cashierEnvInfo.getClientKey()).tokenId(cashierEnvInfo.getTokenId())
                .sessionId(cashierEnvInfo.getSessionId()).sdkVersion(cashierEnvInfo.getSdkVersion())
                .appVersion(cashierEnvInfo.getAppVersion()).websiteLanguage(cashierEnvInfo.getWebsiteLanguage())
                .osType(cashierEnvInfo.getOsType()).orderOsType(cashierEnvInfo.getOrderOsType())
                .orderTerminalType(cashierEnvInfo.getOrderTerminalType())
                .merchantAppVersion(cashierEnvInfo.getAppVersion()).extendInfo(cashierEnvInfo.getExtentInfo()).build();
        return envInfo;
    }

    @Override
    public ConsultDetails totalChargeFeeAmount(CashierRequest cashierRequest, long additionalChargeFee)
            throws FacadeCheckedException, CashierCheckedException {
        String merchantId = cashierRequest.getCashierMerchant().getInternalMerchantId();
        PaymentRequest paymentRequest = cashierRequest.getPaymentRequest();

        IPayOptionBillGenerator payOptionBillGenerator = generatePayOptionBill(cashierRequest);
        List<CashierPayOptionBill> cashierPayOptionBills = payOptionBillGenerator.generatePayOptionBill(paymentRequest
                .getPayBillOptions());
        PayRequest payRequest = CashierFacadeMapper.buildPayRequest(paymentRequest, cashierPayOptionBills);

        long totalServiceAmt = paymentRequest.getPayBillOptions().getServiceAmount();

        Money serviceMoney = new Money(EnumCurrency.INR, String.valueOf(totalServiceAmt));
        List<PayMethodDetail> payMethodDetails = new ArrayList<>();

        for (PayOptionBill pob : payRequest.getBody().getPayOptionBills()) {
            PayMethodDetail payMethodDetail;
            if (PayMethod.BALANCE == pob.getPayMethod()) {
                payMethodDetail = new PayMethodDetail(pob.getPayMethod(), pob.getTransAmount());
            } else {
                long transMoneyAmt = Long.parseLong(pob.getTransAmount().getAmount());

                Money transMoney = new Money(pob.getTransAmount().getCurrency(), String.valueOf(transMoneyAmt));
                Money feeMoney = new Money(pob.getTransAmount().getCurrency(), String.valueOf(additionalChargeFee));
                payMethodDetail = new PayMethodDetail(pob.getPayMethod(), transMoney, feeMoney);
            }
            payMethodDetails.add(payMethodDetail);
        }

        ChargeFeeConsultRequestBody body = new ChargeFeeConsultRequestBody(merchantId, cashierRequest.getProductCode(),
                serviceMoney, payMethodDetails);
        body.setTransCreatedTime(cashierRequest.getTransCreatedtime());
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CHARGE_FEE_CONSULT);

        ChargeFeeConsultRequest chargeFeeConsultRequest = new ChargeFeeConsultRequest(body, head);
        if (routeUtil.getRoute(null, null, cashierRequest.getAcquirementId(), null, "submit_pay") == Routes.PG2) {
            chargeFeeConsultRequest.getBody().setRoute(Routes.PG2.getName());
            chargeFeeConsultRequest.getBody().setMerchantId(cashierRequest.getCashierMerchant().getMerchantId());
            LOGGER.info("Fee consult request will be routed to PG2 for mid : {}", cashierRequest.getCashierMerchant()
                    .getMerchantId());
        }
        ChargeFeeConsultResponse chargeFeeConsultResponse = chargeImpl.feeConsult(chargeFeeConsultRequest);

        if ((chargeFeeConsultResponse == null) || (chargeFeeConsultResponse.getBody() == null)) {
            throw new CashierCheckedException("No response received for CHARGE_FEE_CONSULT");
        }

        List<ConsultDetail> consultDetailList = chargeFeeConsultResponse.getBody().getConsultDetailsList();
        final ConsultDetails consultDetails = new ConsultDetails();

        if (payRequest.getBody().getPayOptionBills().size() > 1) {
            consultDetails.setPayMethod(EPayMethod.HYBRID_PAYMENT);
        } else {
            EPayMethod payMethod = EPayMethod.getPayMethodByMethod(payRequest.getBody().getPayOptionBills().get(0)
                    .getPayMethod().getMethod());
            consultDetails.setPayMethod(payMethod);
        }
        BigDecimal feeAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (consultDetailList != null && consultDetailList.size() > 0) {
            for (ConsultDetail consultDetail : consultDetailList) {
                feeAmount = feeAmount.add(new BigDecimal(AmountUtils.getTransactionAmountInRupee(consultDetail
                        .getBaseFeeAmount().getAmount())));
                taxAmount = taxAmount.add(new BigDecimal(AmountUtils.getTransactionAmountInRupee(consultDetail
                        .getTaxAmount().getAmount())));
            }
        }
        consultDetails.setBaseTransactionAmount(new BigDecimal(AmountUtils.getTransactionAmountInRupee(cashierRequest
                .getPaymentRequest().getPayBillOptions().getServiceAmount().toString())));
        consultDetails.setFeeAmount(feeAmount);
        consultDetails.setTaxAmount(taxAmount);
        consultDetails.setTotalConvenienceCharges(consultDetails.getFeeAmount().add(consultDetails.getTaxAmount()));
        consultDetails.setTotalTransactionAmount(consultDetails.getBaseTransactionAmount()
                .add(consultDetails.getFeeAmount()).add(consultDetails.getTaxAmount()));
        StringBuilder text = new StringBuilder().append("Rs. ").append(consultDetails.getFeeAmount().toPlainString())
                .append(" + GST as applicable.");
        consultDetails.setText(text.toString());

        return consultDetails;
    }

    private IPayOptionBillGenerator generatePayOptionBill(CashierRequest cashierRequest) {
        PayOptionBillGeneratorFactory factory = new PayOptionBillGeneratorFactory();
        return factory.getPayOptionBillGenerator(cashierRequest.getPaymentRequest().getPaymentType());
    }

    public String fetchUserMessageForRiskReject(String alipayInfoCode) {

        String[] infoCodes = StringUtils.split(alipayInfoCode, ";");

        if (infoCodes != null && infoCodes.length > 0 && infoCodes[0].contains("&&")) {
            String[] infoCode = StringUtils.split(infoCodes[0], "&&");
            alipayInfoCode = infoCode[infoCode.length - 1];
        }
        return RiskRejectionErrorMessage.fetchUserMessageByInfoCode(alipayInfoCode);
    }

    public PayResultQueryResponse fetchPayResultQueryResponse(PayResultQueryRequest payResultQueryRequest)
            throws FacadeCheckedException {
        ApiFunctions apiFunction = ApiFunctions.PAY_RESULT_QUERY;
        if (payResultQueryRequest.getBody().isFromAoaMerchant()) {
            apiFunction = ApiFunctions.AOA_PAY_RESULT_QUERY;
        }
        return cashierImpl.payResultQuery(new PayResultQueryRequest(RequestHeaderGenerator.getHeader(apiFunction),
                payResultQueryRequest.getBody()));
    }

    public PayResultQueryResponse fetchPayResultQueryResponse(final CashierRequest cashierRequest)
            throws FacadeCheckedException {
        ApiFunctions apiFunction = ApiFunctions.PAY_RESULT_QUERY;
        if (cashierRequest.isFromAoaMerchant()) {
            apiFunction = ApiFunctions.AOA_PAY_RESULT_QUERY;
        }
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
        PayResultQueryRequestBody body = new PayResultQueryRequestBody(cashierRequest.getLooperRequest()
                .getCashierRequestId(), cashierRequest.isFromAoaMerchant(), cashierRequest.getRoute());
        return cashierImpl.payResultQuery(new PayResultQueryRequest(head, body));
    }

    public void closeOrder(final String merchantId, final String acquirementId, final boolean fromAoaMerchant)
            throws FacadeCheckedException {
        String closeReason = String.format("Payment failed for the acquirementId : {1}", acquirementId);
        ApiFunctions apiFunction = ApiFunctions.CLOSE_ORDER;
        if (fromAoaMerchant) {
            apiFunction = ApiFunctions.AOA_CLOSE_ORDER;
        }
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
        CloseRequestBody body = new CloseRequestBody(acquirementId, merchantId, closeReason, fromAoaMerchant);
        body.setRoute(Routes.PG2);
        body.setPaytmMerchantId(merchantId);
        CloseRequest closeRequest = new CloseRequest(head, body);

        acquiringOrder.closeOrder(closeRequest);
    }

    public void closeFundOrder(final String fundOrderId, final EnvInfoRequestBean envInfo)
            throws FacadeCheckedException {
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CLOSE_FUND_ORDER);
        CloseFundRequestBody body = new CloseFundRequestBody(fundOrderId, envInfo);
        body.setRoute(Routes.PG2);
        CloseFundRequest closeRequest = new CloseFundRequest(head, body);
        topupImpl.closeFundOrder(closeRequest);
    }

    @Override
    public QueryByFundOrderIdResponse queryByFundOrderId(String fundOrderId, String paytmMerchantId, Routes route)
            throws FacadeCheckedException {
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_FUND_ORDER_ID);
        QueryByFundOrderIdRequestBody body = new QueryByFundOrderIdRequestBody(fundOrderId);
        body.setRoute(Routes.PG2);
        body.setPaytmMerchantId(paytmMerchantId);
        QueryByFundOrderIdRequest requestData = new QueryByFundOrderIdRequest(body, head);
        QueryByFundOrderIdResponse fundOrderIdResponse = topupImpl.queryByFundOrderId(requestData);
        return fundOrderIdResponse;
    }

    @Override
    public CashierTransactionStatus queryByAcquirementId(String merchantId, String acquirementId, boolean needFullInfo,
            boolean isFromAOAMerchant, String paytmMerchantId, Routes route) throws CashierCheckedException {
        if (StringUtils.isBlank(paytmMerchantId)) {
            return queryByAcquirementId(merchantId, acquirementId, needFullInfo, isFromAOAMerchant);
        }
        try {

            ApiFunctions apiFunction = ApiFunctions.QUERY_BY_ACQUIREMENTID;
            if (isFromAOAMerchant) {
                apiFunction = ApiFunctions.AOA_QUERY_BY_ACQUIREMENTID;
            }

            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);

            QueryByAcquirementIdRequestBody body = new QueryByAcquirementIdRequestBody(merchantId, acquirementId,
                    needFullInfo, isFromAOAMerchant, route, paytmMerchantId);

            QueryByAcquirementIdRequest queryByAcquirementIdRequest = new QueryByAcquirementIdRequest(head, body);

            QueryByAcquirementIdResponse queryByAcquirementIdResponse = acquiringOrderImpl
                    .queryByAcquirementId(queryByAcquirementIdRequest);

            validateQueryByAcquirementIdResponse(queryByAcquirementIdResponse);

            ResultInfo resultInfo = queryByAcquirementIdResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                return CashierLopperMapper.buildCashierTrasactionStatus(queryByAcquirementIdResponse.getBody());
            }
            throw new CashierCheckedException("Unable to get response, Result Info received : "
                    + mapper.writeValueAsString(resultInfo));

        } catch (FacadeCheckedException ex) {
            throw new CashierCheckedException("Process failed : unable to get response for query by acquriment id", ex);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Internal system error", e);
        } catch (RouterException e) {
            throw new CashierCheckedException("Process failed: unable to get route for query by acqurirement id ", e);
        }
    }
}
