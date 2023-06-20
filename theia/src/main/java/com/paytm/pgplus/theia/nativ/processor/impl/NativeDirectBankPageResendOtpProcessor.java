package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.common.bankForm.model.ResponseDetail;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service(value = "nativeDirectBankPageResendOtpProcessor")
public class NativeDirectBankPageResendOtpProcessor
        extends
        AbstractRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse, NativeDirectBankPageServiceRequest, NativeJsonResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageResendOtpProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("nativeDirectBankPageService")
    INativeDirectBankPageService nativeDirectBankPageService;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Override
    protected NativeDirectBankPageServiceRequest preProcess(NativeDirectBankPageRequest request) {

        nativeDirectBankPageHelper.validateRequest(request);

        String txnToken = request.getHead().getTxnToken();

        InitiateTransactionRequestBody orderDetail = null;
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();

        try {
            orderDetail = nativeDirectBankPageHelper.getOrderDetail(request);

            if (!nativeDirectBankPageHelper.addMoneyMerchant()) {
                nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
            }

            NativeDirectBankPageCacheData cachedBankFormData = nativeDirectBankPageHelper
                    .getCachedBankFormData(request);
            serviceRequest.setCachedBankFormData(cachedBankFormData);

            boolean validRequest = nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount(txnToken,
                    serviceRequest, request);

            if (!validRequest) {
                serviceRequest.setTotalAllowedDirectBankPageResendRetryCount(0);
            }

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

        if (serviceRequest.getTotalAllowedDirectBankPageResendRetryCount() == 0) {
            return new NativeJsonResponse();
        }

        try {

            NativeDirectBankPageServiceResponse serviceResponse = nativeDirectBankPageService.callInstaProxyResendOtp(
                    request, serviceRequest);
            serviceRequest.setInstaProxyResponse(serviceResponse.getDirectAPIResponse());

            /*
             * This method has been crafted to create a new payment request if
             * there is a case of ForceResendOtp on directBankPage
             */
            if (nativeDirectBankPageHelper.isForceResendOtp(request, serviceResponse.getFormDetail())) {
                nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);
            }

        } catch (Exception e) {
            // LOGGER.error("Exception in direct bank page resendOtp {}",
            // ExceptionUtils.getStackTrace(e));
            LOGGER.error("Exception in direct bank page resendOtp {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
            throwException(ResultCode.FAILED, serviceRequest.getOrderDetail());
        }
        return new NativeJsonResponse();
    }

    @Override
    protected NativeJsonResponse postProcess(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest, NativeJsonResponse response) throws Exception {

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);

        if (serviceRequest.getTotalAllowedDirectBankPageResendRetryCount() == 0) {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            resultInfo.setResultMsg("Your OTP resend limit has exhausted.");
            body.setResendRetry(false);
            body.setResultInfo(resultInfo);

            response.setHead(new ResponseHeader());
            response.setBody(body);
            return response;
        }

        DirectAPIResponse instaProxyResp = serviceRequest.getInstaProxyResponse();
        ResponseDetail responseDetail = instaProxyResp.getResultInfo();

        /*
         * 0001 is for fail case at instaProxy
         */
        if (StringUtils.equals(ResultCode.FAILED.getResultCodeId(), responseDetail.getResultCodeId())) {
            LOGGER.info("0001 Response code from instaProxy");
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            resultInfo.setResultMsg("Error occurred in communication with bank. Kindly resend the OTP again");

        } else if (request.getBody().isForceResendOtp()) {
            /*
             * This is the case when isForceResendOtp=true and creation of new
             * payment-leg fails, theia creates resultMsg in this
             */
            if (serviceRequest.getWorkFlowResponseBean() == null) {
                LOGGER.info("isForceResendOtp=true and creation of new payment-leg failed");
                resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
                resultInfo.setResultMsg("Error occurred in communication with bank. Kindly resend the OTP again");

            } else {
                resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
                LOGGER.info("isForceResendOtp=true and creation of new payment-leg is success");
                resultInfo.setResultMsg("OTP has been sent to your registered mobile number");
                /*
                 * resultInfo.setResultMsg("OTP has been resent successfully" +
                 * nativeDirectBankPageHelper.getLastRetryMsg(serviceRequest));
                 */
            }
        } else {
            resultInfo.setResultMsg("OTP has been sent to your registered mobile number");
            /*
             * resultInfo.setResultMsg("OTP has been resent successfully" +
             * nativeDirectBankPageHelper.getLastRetryMsg(serviceRequest));
             */
        }

        body.setResendRetry(nativeDirectBankPageHelper.isResendOtpAllowed(serviceRequest));

        nativeDirectBankPageHelper.incrementDirectBankPageResendOtpRetryCount(request.getHead().getTxnToken(),
                serviceRequest.getCurrentDirectBankPageResendOtpRetryCount());

        body.setResultInfo(resultInfo);
        response.setHead(new ResponseHeader());
        response.setBody(body);

        return response;
    }

    private void throwException(ResultCode resultCode, InitiateTransactionRequestBody orderDetail) {
        throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false).isNativeJsonRequest(true)
                .setOrderDetail(orderDetail).build();
    }
}
