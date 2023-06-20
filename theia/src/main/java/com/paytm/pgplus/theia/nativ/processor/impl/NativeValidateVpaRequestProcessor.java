package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.bankrequest.IValidateVpaService;
import com.paytm.pgplus.facade.bankrequest.Service.BankValidateService;
import com.paytm.pgplus.facade.bankrequest.model.ValidateVpaAndPspRequest;
import com.paytm.pgplus.facade.bankrequest.model.ValidateVpaAndPspResponse;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetailsV4;
import com.paytm.pgplus.facade.user.models.VpaDetailV4;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.httpclient.exception.HttpConnectTimeoutException;
import com.paytm.pgplus.httpclient.exception.HttpReadTimeoutException;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequestBody;
import com.paytm.pgplus.theia.nativ.enums.VpaValidateResponseMapping;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.response.*;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.NativeValidateVpaRequest;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.services.upiAccount.helper.CheckUPIAccountServiceHelper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.facade.bankrequest.enums.AlipayRequestBodyParams.mobile_constant;
import static com.paytm.pgplus.facade.bankrequest.enums.BankRequestParams.DEVICE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.PAYER_NAME;
import static com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.UPI_USER_NOT_FOUND;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;

@Service("nativeValidateVpaRequestProcessor")
public class NativeValidateVpaRequestProcessor
        extends
        AbstractRequestProcessor<VpaValidateRequest, ValidateVpaResponse, NativeValidateVpaRequest, NativeValidateVpaResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidateVpaRequestProcessor.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeValidateVpaRequestProcessor.class);

    @Autowired
    BankValidateService bankValidateService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    IMerchantMappingService merchantMappingService;

    @Autowired
    ChecksumService checksumService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("validateVpaService")
    private IValidateVpaService validateVpaService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public static final String RECURRING = "RECURRING";
    public static final String OTM = "OTM";
    public static final String OTHER = "OTHER";
    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    private CheckUPIAccountServiceHelper checkUPIAccountServiceHelper;

    @Override
    protected NativeValidateVpaRequest preProcess(VpaValidateRequest request) {
        NativeValidateVpaRequest serviceRequest = null;
        validateRequest(request);
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
            if (StringUtils.isNotBlank(request.getBody().getNumericId())) {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), null,
                        MDC.get(TheiaConstant.RequestParams.ORDER_ID), null, false, request.getBody().getNumericId());
            } else {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), request.getBody().getVpa(),
                        MDC.get(TheiaConstant.RequestParams.ORDER_ID));
            }

        } else if (TokenType.GUEST.equals(request.getHead().getTokenType())) {
            validateWithGuestToken(request);
            if (StringUtils.isNotBlank(request.getBody().getNumericId())) {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), null, null, null, false,
                        request.getBody().getNumericId());
            } else {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), request.getBody().getVpa());
            }
        } else if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())) {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
            if (StringUtils.isNotBlank(request.getBody().getNumericId())) {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), null, null, null, false,
                        request.getBody().getNumericId());
            } else {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), request.getBody().getVpa());
            }
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateWithAccessToken(request);
            if (StringUtils.isNotBlank(request.getBody().getNumericId())) {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), null, null, null, false,
                        request.getBody().getNumericId());
            } else {
                serviceRequest = new NativeValidateVpaRequest(request.getBody().getMid(), request.getBody().getVpa());
            }
        } else {
            String txnToken = request.getHead().getTxnToken();
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
            if (!TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equals(request.getHead().getWorkFlow())
                    && nativeSessionUtil.checkVPAValidationLimit(txnToken)) {
                // LOGGER.error("Rate Limiting on API: /vpa/validate, not proceeding with the request");
                EXT_LOGGER.customInfo("Rate Limiting on API: /vpa/validate, not proceeding with the request");
                throw SessionExpiredException.getException(ResultCode.RATE_LIMITING_EXCEPTION);
            }
            InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
            /* skip validation based on AoaSubsOnPgMid */
            if (!orderDetail.isAoaSubsOnPgMid()) {
                try {
                    nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
                } catch (Exception ignored) {
                    try {
                        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();
                        nativeValidationService.validateMidOrderIdinRequest(orderDetail.getMid(),
                                orderDetail.getOrderId(),
                                httpServletRequest.getParameter(TheiaConstant.RequestParams.Native.MID),
                                httpServletRequest.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID));
                    } catch (MidDoesnotMatchException | OrderIdDoesnotMatchException midDoesnotMatchException) {
                        throw midDoesnotMatchException;
                    } catch (Exception ignored1) {
                        throw MidDoesnotMatchException.getException();
                    }
                }
            }
            boolean preAuth = TxnType.AUTH.equals(orderDetail.getTxnType())
                    || TxnType.ESCROW.equals(orderDetail.getTxnType());
            if (StringUtils.isNotBlank(request.getBody().getNumericId())) {
                serviceRequest = new NativeValidateVpaRequest(orderDetail.getMid(), null, orderDetail.getOrderId(),
                        orderDetail.getOrderId(), preAuth, request.getBody().getNumericId());
            } else {
                serviceRequest = new NativeValidateVpaRequest(orderDetail.getMid(), request.getBody().getVpa(),
                        orderDetail.getOrderId(), orderDetail.getRequestType(), preAuth);
            }
        }
        serviceRequest.setQueryParams(request.getBody().getQueryParams());
        return serviceRequest;
    }

    @Override
    protected NativeValidateVpaResponse onProcess(VpaValidateRequest request,
            NativeValidateVpaRequest nativeValidateVpaRequest) throws Exception {
        String mid = nativeValidateVpaRequest.getMid();
        String vpa = nativeValidateVpaRequest.getVpaAddress();
        String numericId = nativeValidateVpaRequest.getNumericId();

        NativeValidateVpaResponse serviceResponse = new NativeValidateVpaResponse(vpa);
        ResultInfo resultInfo = serviceResponse.getResultInfo();
        ResponseConstants responseConstant;
        if (StringUtils.isBlank(request.getBody().getVpa()) && StringUtils.isNotBlank(request.getBody().getPhoneNo())) {
            NativeValidateVpaResponse nativeValidateVpaResponse = getVpaFromPhoneNumber(request);
            return nativeValidateVpaResponse;
        }
        if (StringUtils.isBlank(vpa) && StringUtils.isNotBlank(numericId)
                && !numericIdValidation(numericId, resultInfo)) {
            responseConstant = ResponseConstants.INVALID_UPI_NUMBER;
            resultInfo.setResultCode(responseConstant.getCode());
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            return serviceResponse;
        }
        if (StringUtils.isBlank(mid) || (StringUtils.isBlank(vpa) && StringUtils.isBlank(numericId))) {
            return getInvalidParamResponse();
        } else {
            try {
                final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                        .fetchMerchanData(mid);
                if (merchantMappingResponse == null || !merchantMappingResponse.isSuccessfullyProcessed()
                        || StringUtils.isBlank(merchantMappingResponse.getResponse().getOfficialName())) {
                    throw new InvalidRequestException("Merchant details not found");
                }
                String seqNo = generateRandomString();
                String merchantName = getTrimmedMerchantName(merchantMappingResponse.getResponse().getOfficialName());
                if (ConfigurationUtil.getProperty("validate.vpa.v2", "false").equals("true")) {
                    ValidateVpaAndPspRequest validateVpaAndPspRequest = new ValidateVpaAndPspRequest();
                    validateVpaAndPspRequest.setSeqNo(seqNo);
                    validateVpaAndPspRequest.setPayeeName(merchantName);
                    validateVpaAndPspRequest.setMobile(mobile_constant.getName());
                    if (StringUtils.isNotBlank(vpa) && StringUtils.isBlank(numericId)) {
                        validateVpaAndPspRequest.setVirtualAddress(vpa);
                    } else if (StringUtils.isNotBlank(numericId) && StringUtils.isBlank(vpa)) {
                        validateVpaAndPspRequest.setVirtualAddress(numericId);
                    }
                    validateVpaAndPspRequest.setDeviceId(DEVICE.getName());
                    if (ERequestType.isSubscriptionCreationRequest(nativeValidateVpaRequest.getRequestType()))
                        validateVpaAndPspRequest.setAdditionalInfoType(RECURRING);
                    else if (nativeValidateVpaRequest.isPreAuth())
                        validateVpaAndPspRequest.setAdditionalInfoType(OTM);
                    else
                        validateVpaAndPspRequest.setAdditionalInfoType(OTHER);

                    ValidateVpaAndPspResponse validateVpaAndPspResponse = validateVpaService
                            .fetchValidatedVpa(validateVpaAndPspRequest);
                    try {
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("RESPONSE_STATUS", validateVpaAndPspResponse.getStatus());
                        responseMap.put("RESPONSE_MESSAGE", validateVpaAndPspResponse.getRespMessage());
                        statsDUtils.pushResponse("VALIDATE_VPA", responseMap);
                    } catch (Exception exception) {
                        LOGGER.error("Error in pushing response message " + "VALIDATE_VPA" + "to grafana", exception);
                    }
                    processResponsev2(request, serviceResponse, validateVpaAndPspResponse, validateVpaAndPspRequest);
                } else {
                    // this is a old code written
                    JSONObject payResponse = bankValidateService.getResponse(seqNo, vpa, merchantName,
                            nativeValidateVpaRequest.getQueryParams());
                    LOGGER.info("Response received from bank : {}", payResponse);
                    processResponse(serviceResponse, payResponse);
                }
            } catch (HttpConnectTimeoutException | HttpReadTimeoutException networkException) {
                LOGGER.error("Network error in validating VPA in native : {}", networkException);
                serviceResponse.setValid(true);
            } catch (Exception e) {
                LOGGER.error("Issue in validating VPA in native : {}", e);
                responseConstant = ResponseConstants.SYSTEM_ERROR;
                resultInfo.setResultCode(responseConstant.getCode());
                if (StringUtils.isNotBlank(vpa) && StringUtils.isBlank(numericId)) {
                    resultInfo.setResultMsg("Sorry! We could not verify the VPA. Please try again.");
                } else if (StringUtils.isNotBlank(numericId) && StringUtils.isBlank(vpa)) {
                    resultInfo.setResultMsg("Sorry! We could not verify the UPI Number. Please try again.");
                }
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
                localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                        .getRequestURI());
            }
        }
        LOGGER.info("Response returning to merchant : {}", serviceResponse);
        return serviceResponse;

    }

    @Override
    protected ValidateVpaResponse postProcess(VpaValidateRequest request,
            NativeValidateVpaRequest nativeValidateVpaRequest, NativeValidateVpaResponse nativeValidateVpaResponse)
            throws Exception {
        ResponseHeader responseHeader = new ResponseHeader();
        loggingSuccessOrFailureOfVpaResponse(nativeValidateVpaRequest, nativeValidateVpaResponse);
        return new ValidateVpaResponse(responseHeader, nativeValidateVpaResponse);
    }

    private String getTrimmedMerchantName(String merchantName) {
        return merchantName != null ? merchantName.replace(" ", "") : merchantName;
    }

    private String generateRandomString() {
        String randStr = RandomStringUtils.random(10, true, true);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        sb.append("PTM").append(randStr).append(dateFormat.format(new Date()));
        return sb.toString();
    }

    private void processResponse(NativeValidateVpaResponse serviceResponse, JSONObject payResponse) throws Exception {
        try {
            ValidateVpaResponseFromBank vpaResponse = JsonMapper.mapJsonToObject(payResponse.toString(),
                    ValidateVpaResponseFromBank.class);
            if (StringUtils.isNotBlank(vpaResponse.getResponse())) {
                if (StringUtils.equals(vpaResponse.getResponse(), "0")) {
                    serviceResponse.setValid(true);
                    return;
                }
            }
        } catch (Exception ex) {
            throw new Exception("Json Parsing Exception in ValidateVpaResponseFromBank, reason:: ", ex);
        }
        serviceResponse.setValid(false);
    }

    private void processResponsev2(VpaValidateRequest request, NativeValidateVpaResponse nativeValidateVpaResponse,
            ValidateVpaAndPspResponse validateVpaAndPspResponse, ValidateVpaAndPspRequest vpaAndPspRequest) {

        if ("SUCCESS".equals(validateVpaAndPspResponse.getStatus())) {
            nativeValidateVpaResponse.setValid(true);
            nativeValidateVpaResponse.setCustId(validateVpaAndPspResponse.getCustId());
            if (null != validateVpaAndPspResponse.getCmid()) {
                nativeValidateVpaResponse.setCmId(validateVpaAndPspResponse.getCmid());
            }
            if (null != validateVpaAndPspResponse.getVpa()) {
                nativeValidateVpaResponse.setVpa(validateVpaAndPspResponse.getVpa());
            }
            // set recurring details
            if (RECURRING.equals(vpaAndPspRequest.getAdditionalInfoType())
                    && validateVpaAndPspResponse.getRecurringDetails() != null) {
                RecurringDetails recurringDetails = new RecurringDetails();
                recurringDetails.setPspSupportedRecurring("Y".equals(validateVpaAndPspResponse.getFeatureDetails()
                        .getPspSupported()));
                recurringDetails.setBankSupportedRecurring("Y".equals(validateVpaAndPspResponse.getFeatureDetails()
                        .getAnyBankSupported()));
                nativeValidateVpaResponse.setRecurringDetails(recurringDetails);
            } else if (OTM.equals(vpaAndPspRequest.getAdditionalInfoType())
                    && validateVpaAndPspResponse.getFeatureDetails() != null) {
                FeatureDetails featureDetails = new FeatureDetails();
                featureDetails.setBankSupported("Y".equals(validateVpaAndPspResponse.getFeatureDetails()
                        .getAnyBankSupported()));
                featureDetails.setPspSupported("Y".equals(validateVpaAndPspResponse.getFeatureDetails()
                        .getPspSupported()));
                nativeValidateVpaResponse.setFeatureDetails(featureDetails);
            }

            // set PAYER_VPA in txnToken
            if (request.getHead().getTokenType() == null) {
                String txnToken = request.getHead().getTxnToken();
                theiaSessionRedisUtil.hsetIfExist(txnToken, PAYER_NAME, validateVpaAndPspResponse.getName());
            }
        } else {
            prepareFailureResponseForVpa(nativeValidateVpaResponse, validateVpaAndPspResponse, request);
        }
    }

    private void validateWithSsoToken(VpaValidateRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        nativeValidationService.validateSSOToken(request.getHead().getToken(), request.getBody().getMid());
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));
    }

    private void validateWithGuestToken(VpaValidateRequest request) {
        nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
        String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                TheiaConstant.RequestParams.Native.MID);
        nativeValidationService.validateMid(mid);
        request.getHead().setTxnToken(request.getHead().getToken());
    }

    private void validateWithAccessToken(VpaValidateRequest request) {
        String mid = request.getBody().getMid();
        String referenceId = request.getBody().getReferenceId();
        String token = request.getHead().getToken();
        nativeValidationService.validateMid(mid);
        accessTokenUtils.validateAccessToken(mid, referenceId, token);
        request.getHead().setTxnToken(request.getHead().getToken());
    }

    private NativeValidateVpaResponse getInvalidParamResponse() {
        NativeValidateVpaResponse nativeValidateVpaResponse = new NativeValidateVpaResponse();
        ResponseConstants responseConstant = ResponseConstants.INVALID_PARAM;
        ResultInfo resultInfo = nativeValidateVpaResponse.getResultInfo();
        resultInfo.setResultCode(responseConstant.getCode());
        resultInfo.setResultMsg(responseConstant.getMessage());
        resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                .getRequestURI());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        nativeValidateVpaResponse.setValid(false);
        return nativeValidateVpaResponse;
    }

    private NativeValidateVpaResponse validateRequest(VpaValidateRequest request) {
        if (StringUtils.isBlank(request.getBody().getVpa())
                && (StringUtils.isBlank(request.getBody().getPhoneNo()) && StringUtils.isBlank(request.getBody()
                        .getNumericId()))) {
            return getInvalidParamResponse();
        }
        return null;
    }

    private NativeValidateVpaResponse getVpaFromPhoneNumber(VpaValidateRequest request) {

        String vpa = null;
        try {
            CheckUPIAccountRequest checkUPIAccountRequest = new CheckUPIAccountRequest();
            checkUPIAccountRequest.setBody(new CheckUPIAccountRequestBody(request.getBody().getMid(), request.getBody()
                    .getPhoneNo()));
            String userIdOAuth = checkUPIAccountServiceHelper.getUserIdFromOAuth(checkUPIAccountRequest);
            FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest();
            fetchUserPaytmVpaRequest.setUserId(userIdOAuth);
            fetchUserPaytmVpaRequest.setRequestId(RequestIdGenerator.generateRequestId());
            if (null == userIdOAuth) {
                return prepareFailureOauthResponse();
            }
            GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserPaytmVpaResponse = sarvatraVpaDetails
                    .fetchUserProfileVpaV4(fetchUserPaytmVpaRequest);
            PaytmVpaDetailsV4 paytmVpaDetailsV4 = null;
            if (fetchUserPaytmVpaResponse != null && fetchUserPaytmVpaResponse.isSuccessfullyProcessed()
                    && SUCCESS.equalsIgnoreCase(fetchUserPaytmVpaResponse.getResponse().getStatus())) {
                if (null != fetchUserPaytmVpaResponse.getResponse()
                        && null != fetchUserPaytmVpaResponse.getResponse().getRespDetails()) {
                    paytmVpaDetailsV4 = fetchUserPaytmVpaResponse.getResponse().getRespDetails();
                    if (null != paytmVpaDetailsV4 && null != paytmVpaDetailsV4.getProfileDetail()
                            && null != paytmVpaDetailsV4.getProfileDetail().getVpaDetails()) {
                        // List<VpaDetailV4> vpaDetails =
                        // paytmVpaDetailsV4.getProfileDetail().getVpaDetails();
                        for (VpaDetailV4 vpaDetails : paytmVpaDetailsV4.getProfileDetail().getVpaDetails())
                            if (null != vpaDetails && vpaDetails.isPrimary()) {
                                vpa = vpaDetails.getName();
                            }
                    }
                }
                if (null == vpa) {
                    return prepareResponseWhereVpaDoesnotExists();
                }
                return prepareResponseWhereVpaExists(vpa);
            }
            if (fetchUserPaytmVpaResponse.getResponse().getRespCode().equals(UPI_USER_NOT_FOUND.getCode())) {
                return prepareResponseWhereVpaDoesnotExists();
            }

        } catch (Exception e) {
            LOGGER.info("Exception occured while fetching Vpa through phone number : {}", e);
            ResultInfo resultInfo = new ResultInfo();
            ResponseConstants responseConstant = ResponseConstants.SYSTEM_ERROR;
            resultInfo.setResultCode(responseConstant.getCode());
            resultInfo.setResultMsg("Sorry! We could not fetch the UPI ID. Please try again.");
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                    .getRequestURI());
        }
        NativeValidateVpaResponse nativeValidateVpaResponse = new NativeValidateVpaResponse();
        return prepareFailureResponseForPhone(nativeValidateVpaResponse, null);
    }

    private NativeValidateVpaResponse prepareResponseWhereVpaExists(String vpa) {
        NativeValidateVpaResponse nativeValidateVpaResponse = new NativeValidateVpaResponse();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.SUCCESS.getCode());
        resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
        resultInfo.setResultMsg(ResultCode.SUCCESS.getResultMsg());
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                .getRequestURI());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        nativeValidateVpaResponse.setValid(true);
        nativeValidateVpaResponse.setVpa(vpa);
        return nativeValidateVpaResponse;
    }

    private NativeValidateVpaResponse prepareResponseWhereVpaDoesnotExists() {
        NativeValidateVpaResponse nativeValidateVpaResponse = new NativeValidateVpaResponse();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.SUCCESS.getCode());
        resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
        resultInfo.setResultMsg("Paytm phone-number not linked to VPA, Try Again");
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                .getRequestURI());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        nativeValidateVpaResponse.setValid(false);
        return nativeValidateVpaResponse;
    }

    private NativeValidateVpaResponse prepareFailureResponseForPhone(
            NativeValidateVpaResponse nativeValidateVpaResponse, ValidateVpaAndPspResponse validateVpaAndPspResponse) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
        if (null != validateVpaAndPspResponse && null != validateVpaAndPspResponse.getRespMessage()) {
            resultInfo.setResultMsg(validateVpaAndPspResponse.getRespMessage());
        } else {
            resultInfo.setResultMsg("Verification failed, Try Again");
        }
        resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        nativeValidateVpaResponse.setValid(false);
        return nativeValidateVpaResponse;
    }

    private NativeValidateVpaResponse prepareFailureResponseForVpa(NativeValidateVpaResponse nativeValidateVpaResponse,
            ValidateVpaAndPspResponse validateVpaAndPspResponse, VpaValidateRequest request) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
        boolean isNumericIdCase = StringUtils.isNotBlank(request.getBody().getNumericId());
        if (validateVpaAndPspResponse != null && validateVpaAndPspResponse.getRespCode() != null) {
            VpaValidateResponseMapping responseEnum = VpaValidateResponseMapping.get(validateVpaAndPspResponse
                    .getRespCode());
            if (responseEnum != null) {
                if (isNumericIdCase)
                    resultInfo.setResultMsg(responseEnum.getNumericIdErrorMsg());
                else
                    resultInfo.setResultMsg(responseEnum.getVpaErrorMsg());
            }
        }
        if (StringUtils.isBlank(resultInfo.getResultMsg())) {
            if (isNumericIdCase)
                resultInfo.setResultMsg(VpaValidateResponseMapping.DEFAULT_ERROR.getNumericIdErrorMsg());
            else
                resultInfo.setResultMsg(VpaValidateResponseMapping.DEFAULT_ERROR.getVpaErrorMsg());
        }
        resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        nativeValidateVpaResponse.setValid(false);
        return nativeValidateVpaResponse;
    }

    private NativeValidateVpaResponse prepareFailureOauthResponse() {
        NativeValidateVpaResponse nativeValidateVpaResponse = new NativeValidateVpaResponse();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.FAILED.getCode());
        resultInfo.setResultMsg("Phone number does not exist in Paytm, Try Again");
        resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
        nativeValidateVpaResponse.setResultInfo(resultInfo);
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, OfflinePaymentUtils.gethttpServletRequest()
                .getRequestURI());
        nativeValidateVpaResponse.setValid(false);
        return nativeValidateVpaResponse;
    }

    private boolean numericIdValidation(String numericId, ResultInfo resultInfo) {
        if (!numericId.matches("\\d+")) {
            resultInfo.setResultMsg("Only numbers to be entered for UPI Number");
            return false;
        }
        if (numericId.length() < 8 || numericId.length() > 10) {
            resultInfo.setResultMsg("UPI Number can be 8 to 10 digit length only");
            return false;
        }
        return true;
    }

    private void loggingSuccessOrFailureOfVpaResponse(NativeValidateVpaRequest nativeValidateVpaRequest,
            NativeValidateVpaResponse nativeValidateVpaResponse) {
        if (nativeValidateVpaRequest != null || nativeValidateVpaResponse != null) {
            Map<String, String> metaData = new HashMap<>();
            metaData.put("isUpiNumberRequest",
                    String.valueOf(StringUtils.isNotBlank(nativeValidateVpaRequest.getNumericId())));
            metaData.put("isVpaRequest",
                    String.valueOf(StringUtils.isNotBlank(nativeValidateVpaRequest.getVpaAddress())));
            metaData.put("isValidValue", String.valueOf(nativeValidateVpaResponse.isValid()));
            EventUtils.pushTheiaEvents(EventNameEnum.VALIDATE_UPI, metaData);
        }
    }

}
