package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageCacheData;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.INativeDirectBankPageService;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.ONUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRANSACTION_RESPONSE_OBJECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_ENCRYPTED_RESPONSE_TO_JSON;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.OFFLINE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.PAYMENT_RETRY_INFO;

@Service("nativeDirectBankPageSubmitProcessor")
public class NativeDirectBankPageSubmitProcessor
        extends
        AbstractRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse, NativeDirectBankPageServiceRequest, NativeJsonResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageSubmitProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    @Qualifier("nativeDirectBankPageService")
    INativeDirectBankPageService nativeDirectBankPageService;

    @Autowired
    @Qualifier("merchantResponseService")
    MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    protected NativeDirectBankPageServiceRequest preProcess(NativeDirectBankPageRequest request) {

        nativeDirectBankPageHelper.validateRequest(request);

        if (StringUtils.isBlank(request.getBody().getOtp())) {
            LOGGER.error("OTP is null or empty");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isNativeJsonRequest(true).isRetryAllowed(true).setRetryMsg("OTP entered is empty.").build();
        }
        checkIfOtpNotNumeric(request.getBody().getOtp());

        String txnToken = request.getHead().getTxnToken();

        InitiateTransactionRequestBody orderDetail = null;

        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();

        try {

            orderDetail = nativeDirectBankPageHelper.getOrderDetail(request);
            serviceRequest.setOrderDetail(orderDetail);
            /*
             * Hack for add and pay where mid comes as scwpa
             */

            if (!nativeDirectBankPageHelper.addMoneyMerchant()) {
                nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
            }

            NativeDirectBankPageCacheData cachedBankFormData = nativeDirectBankPageHelper
                    .getCachedBankFormData(request);

            serviceRequest.setCachedBankFormData(cachedBankFormData);

            nativeDirectBankPageHelper.validateDirectBankPageSubmitRetryCount(txnToken, serviceRequest, request);

        } catch (SessionExpiredException see) {
            throwException(ResultCode.SESSION_EXPIRED_EXCEPTION, orderDetail);
        } catch (MidDoesnotMatchException mme) {
            throwException(ResultCode.MID_DOES_NOT_MATCH, orderDetail);
        } catch (OrderIdDoesnotMatchException orderIdException) {
            throwException(ResultCode.ORDER_ID_DOES_NOT_MATCH, orderDetail);
        }

        return serviceRequest;
    }

    @Override
    protected NativeJsonResponse onProcess(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception {

        try {
            /*
             * This calls instaProxy API
             */
            NativeDirectBankPageServiceResponse serviceResponse = null;
            try {

                serviceResponse = nativeDirectBankPageService.callInstaProxySubmit(request, serviceRequest);
                serviceRequest.setInstaProxyResponse(serviceResponse.getDirectAPIResponse());
            } catch (Exception e) {
                LOGGER.error("Failed in Fetching response from Instaproxy {}", ExceptionUtils.getStackTrace(e));
                serviceResponse = new NativeDirectBankPageServiceResponse(
                        nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                                NativeDirectBankPageRequestType.submit));

            }

            /*
             * We set params in HttpRequest because it is needed for creating
             * transactionResponse
             */
            if (serviceRequest.getInstaProxyResponse() == null
                    || (serviceRequest.getInstaProxyResponse() != null && !serviceRequest.getInstaProxyResponse()
                            .getResultInfo().isRetry())) {
                nativeDirectBankPageHelper.setParamsForCashierResponseInHttpRequest(request, serviceRequest,
                        serviceResponse);

                /*
                 * "data" is transactionResponse we get when theia gets callback
                 * on /v1/transactionStatus
                 */

                Map<String, String> data = transactionStatusServiceImpl.getCashierResponse(request
                        .getHttpServletRequest());
                TransactionResponse transactionResponse = JsonMapper.mapJsonToObject(
                        data.get(TRANSACTION_RESPONSE_OBJECT), TransactionResponse.class);
                serviceRequest.setTransactionResponse(transactionResponse);
                serviceRequest.setTransactionStatusData(data);

                /*
                 * This method has been crafted to create a new payment request
                 * if there is a case of invalid otp on directBankPage
                 */
                if (nativeDirectBankPageHelper.checkIfInvalidOtpCase(data)) {
                    nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in direct bank page submit {}", ExceptionUtils.getStackTrace(e));
            throwException(ResultCode.FAILED, serviceRequest.getOrderDetail());
        }

        return new NativeJsonResponse();
    }

    @Override
    protected NativeJsonResponse postProcess(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest, NativeJsonResponse response) throws Exception {

        response.setHead(new ResponseHeader());
        NativeJsonResponseBody body = new NativeJsonResponseBody();

        /*
         * By default setting resultCode as success, later we update it
         * depending on the payment status
         */
        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);

        /*
         * If WorkFlowResponseBean is not null, it means a another(new)
         * paymentRequest has been created. Also, sets bankRetry=true if the
         * directBankPage retryCount is within limits
         */
        if (serviceRequest.getWorkFlowResponseBean() != null
                || (serviceRequest.getInstaProxyResponse() != null && serviceRequest.getInstaProxyResponse()
                        .getResultInfo().isRetry())) {
            LOGGER.info("Returning response with bankRetry=true");

            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            resultInfo.setResultMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
            resultInfo.setBankRetry(true);

            nativeDirectBankPageHelper.incrementDirectBankPageSubmitRetryCount(request.getHead().getTxnToken(),
                    serviceRequest.getCurrentDirectBankPageSubmitRetryCount());

            body.setResultInfo(resultInfo);
            response.setBody(body);
            return response;
        }

        Map<String, String> txnInfo = new TreeMap<>();
        String mid = "";
        if (serviceRequest.getOrderDetail() != null) {
            mid = serviceRequest.getOrderDetail().getMid();
        }
        if (StringUtils.isBlank(mid) && serviceRequest.getTransactionResponse() != null) {
            mid = serviceRequest.getTransactionResponse().getMid();
        } else if (StringUtils.isBlank(mid)) {
            LOGGER.error("OrderDetail = {} and TransactionResponse = {} are null", serviceRequest.getOrderDetail(),
                    serviceRequest.getTransactionResponse());
        }

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);
        boolean encRequestEnabled = merchantPreferenceService.isEncRequestEnabled(mid);
        if ((isAES256Encrypted || encRequestEnabled)
                && ff4jUtils.isFeatureEnabledOnMid(mid, THEIA_ENCRYPTED_RESPONSE_TO_JSON, false)) {
            LOGGER.info("Feature encryptedResponseToJson is enabled");
            merchantResponseService.encryptedResponseJson(mid, txnInfo, serviceRequest.getTransactionResponse(),
                    isAES256Encrypted, encRequestEnabled);
        } else {
            merchantResponseService.makeReponseToMerchantEnhancedNative(serviceRequest.getTransactionResponse(),
                    txnInfo);
        }

        if (!StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(), serviceRequest.getTransactionResponse()
                .getTransactionStatus())) {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            resultInfo.setResultMsg(serviceRequest.getTransactionResponse().getResponseMsg());
        }

        /*
         * This checks if retry is allowed on merchant or not to do more
         * paymentRetry on the order
         */
        String callBackUrl = serviceRequest.getTransactionResponse().getCallbackUrl();

        if (isRetryAllowedInCallBackUrl(callBackUrl)) {
            resultInfo.setRetry(true);
        } else {
            resultInfo.setRetry(false);
        }

        if (!retryServiceHelper.checkNativePaymentRetry(serviceRequest.getTransactionResponse().getMid(),
                serviceRequest.getTransactionResponse().getOrderId())) {
            resultInfo.setRetry(false);
        }
        resultInfo.setResultMsg(serviceRequest.getTransactionResponse().getResponseMsg());

        body.setResultInfo(resultInfo);

        /*
         * This handles case of retry in enhancedNativeFlow
         */
        if (checkForEnhanceNativeFlow(serviceRequest.getTransactionStatusData()) && resultInfo.getRetry()) {
            nativeDirectBankPageHelper.makeResponseForEnhancedNativeFlow(request,
                    serviceRequest.getTransactionResponse(), txnInfo);
            callBackUrl = nativeDirectBankPageHelper.getEnhanceNativeCallBackUrl(serviceRequest
                    .getTransactionResponse().getMid(), serviceRequest.getTransactionResponse().getOrderId());
        }

        body.setTxnInfo(txnInfo);
        body.setCallBackUrl(callBackUrl);
        body.setDeclineReason(serviceRequest.getTransactionResponse().getDeclineReason());
        nativeDirectBankPageHelper.resetCounts(request.getHead().getTxnToken());

        // For Propagating retryInfo in response
        setRetryInfo(body, serviceRequest);

        response.setBody(body);

        return response;
    }

    private void setRetryInfo(NativeJsonResponseBody body, NativeDirectBankPageServiceRequest serviceRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", serviceRequest.getTransactionResponse().getMid());
        if (serviceRequest.getTransactionResponse().isOfflineRequest()) {
            context.put("merchantType", OFFLINE);
        } else if (merchantExtendInfoUtils.isMerchantOnPaytm(serviceRequest.getTransactionResponse().getMid())) {
            context.put("merchantType", ONUS);
        }
        context.put("custId", serviceRequest.getTransactionResponse().getCustId());
        if (iPgpFf4jClient.checkWithdefault(PAYMENT_RETRY_INFO, context, false)
                && retryServiceHelper.checkNativePaymentRetry(serviceRequest.getTransactionResponse().getMid(),
                        serviceRequest.getTransactionResponse().getOrderId())) {
            LOGGER.info("Setting Retry Info in native json response");
            body.setRetryInfo(serviceRequest.getTransactionResponse().getRetryInfo());

        }
    }

    private void throwException(ResultCode resultCode, InitiateTransactionRequestBody orderDetail) {
        throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false).isNativeJsonRequest(true)
                .setOrderDetail(orderDetail).build();
    }

    private boolean isRetryAllowedInCallBackUrl(String callBackUrl) {
        return StringUtils.contains(callBackUrl, "retryAllowed=true");
    }

    private void checkIfOtpNotNumeric(String otp) {
        if (!StringUtils.isNumeric(otp)) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isNativeJsonRequest(true).isRetryAllowed(true)
                    .setRetryMsg("Invalid OTP, alphabets and special characters are not allowed in OTP.").build();
        }
    }

    private boolean checkForEnhanceNativeFlow(Map<String, String> data) {
        /*
         * Here, we're checking if it is EnhancedNativeFlow, and if it is a
         * retry case(go back to cashierPage)
         */
        if (StringUtils.equals(TheiaConstant.RetryConstants.YES,
                data.get(TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW))) {
            return true;
        }
        return false;
    }

}
