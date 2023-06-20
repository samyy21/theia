package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.enums.ResultStatus;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoVerifyRequest;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoVerifyRequestBody;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoViewRequest;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoViewRequestBody;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoVerifyResponse;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoVerifyResponseBody;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponse;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponseBody;
import com.paytm.pgplus.facade.risk.services.IRiskVerify;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequest;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponse;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponseBody;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequest;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponse;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponseBody;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("riskVerifierService")
public class RiskVerifierServiceImpl implements IRiskVerifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskVerifierServiceImpl.class);

    @Autowired
    private IRiskVerify riskVerificationService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private RiskVerificationUtil riskVerificationUtil;

    @Override
    public DoViewResponse doView(DoViewRequest request) {
        nativeValidationService.validateRiskDoViewRequest(request.getHead().getToken());
        final AlipayExternalRequestHeader header;
        RiskVerifierDoViewResponse riskVerifierDoViewResponse = null;
        try {
            header = RequestHeaderGenerator.getHeader(ApiFunctions.RISK_VERIFIER_DO_VIEW);
            RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.getRiskVerificationData(request.getHead()
                    .getToken());
            String verifyId = riskVerifierPayload.getRiskVerifierDoViewResponseBody().getVerifyId();
            RiskVerifierDoViewRequestBody riskVerifierDoViewRequestBody = new RiskVerifierDoViewRequestBody(verifyId,
                    request.getBody().getMethod());
            RiskVerifierDoViewRequest riskVerifierDoViewRequest = new RiskVerifierDoViewRequest(header,
                    riskVerifierDoViewRequestBody);
            riskVerifierDoViewResponse = riskVerificationService.doView(riskVerifierDoViewRequest);
            if (com.paytm.pgplus.common.enums.ResultCode.SUCCESS.getCode().equals(
                    riskVerifierDoViewResponse.getBody().getResultInfo().getResultCode())) {
                RiskVerifierDoViewResponseBody riskVerifierDoViewResponseBody = riskVerifierDoViewResponse.getBody();
                DoViewResponseBody doViewResponseBody = new DoViewResponseBody(
                        riskVerifierDoViewResponseBody.getMethod(), riskVerifierDoViewResponseBody.getRenderData());
                DoViewResponse doViewResponse = new DoViewResponse(new ResponseHeader(), doViewResponseBody);
                return doViewResponse;
            }

        } catch (Exception e) {
            LOGGER.error("Exception occured while calling risk doView ", e);
        }
        return getDoViewErrorResponse(riskVerifierDoViewResponse, request.getHead().getTxnToken());
    }

    @Override
    public DoVerifyResponse doVerify(DoVerifyRequest request) {
        nativeValidationService.validateRiskDoViewRequest(request.getHead().getToken());
        final AlipayExternalRequestHeader header;
        RiskVerifierDoVerifyResponse riskVerifierDoVerifyResponse = null;
        try {
            header = RequestHeaderGenerator.getHeader(ApiFunctions.RISK_VERIFIER_DO_VERIFY);
            RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.getRiskVerificationData(request.getHead()
                    .getToken());
            String verifyId = riskVerifierPayload.getRiskVerifierDoViewResponseBody().getVerifyId();

            RiskVerifierDoVerifyRequestBody riskVerifierDoVerifyRequestBody = new RiskVerifierDoVerifyRequestBody(
                    verifyId, request.getBody().getMethod(), request.getBody().getValidateData());
            RiskVerifierDoVerifyRequest riskVerifierDoVerifyRequest = new RiskVerifierDoVerifyRequest(header,
                    riskVerifierDoVerifyRequestBody);
            riskVerifierDoVerifyResponse = riskVerificationService.doVerify(riskVerifierDoVerifyRequest);
            if (com.paytm.pgplus.common.enums.ResultCode.SUCCESS.getCode().equals(
                    riskVerifierDoVerifyResponse.getBody().getResultInfo().getResultCode())) {
                RiskVerifierDoVerifyResponseBody riskVerifierDoVerifyResponseBody = riskVerifierDoVerifyResponse
                        .getBody();
                if (Boolean.valueOf(riskVerifierDoVerifyResponseBody.getIsFinish())) {
                    String key = RiskConstants.IS_RISK_VERIFIED_PREFIX + request.getHead().getToken();
                    nativeSessionUtil.setKey(key, verifyId, 900);
                }
                // TODO do we need to set retry flag to false for pwd
                // irrespective of risk response
                DoVerifyResponseBody doVerifyResponseBody = new DoVerifyResponseBody(
                        riskVerifierDoVerifyResponseBody.getIsFinish(), riskVerifierDoVerifyResponseBody.getCanRetry(),
                        riskVerifierDoVerifyResponseBody.getNextMethod(),
                        riskVerifierDoVerifyResponseBody.getRenderData());
                DoVerifyResponse doVerifyResponse = new DoVerifyResponse(new ResponseHeader(), doVerifyResponseBody);
                return doVerifyResponse;
            } else if (com.paytm.pgplus.common.enums.ResultCode.SECURITY_VERIFY_FAIL.getCode().equals(
                    riskVerifierDoVerifyResponse.getBody().getResultInfo().getResultCode())) {
                return getVerificationFailedResponse(riskVerifierDoVerifyResponse);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while calling risk doVerify ", e);
        }
        return getDoVerifyErrorResponse(request, request.getHead().getTxnToken());
    }

    private DoViewResponse getDoViewErrorResponse(RiskVerifierDoViewResponse riskVerifierDoViewResponse, String txnToken) {
        String resultCode = null;
        if (riskVerifierDoViewResponse != null && riskVerifierDoViewResponse.getBody() != null) {
            resultCode = riskVerifierDoViewResponse.getBody().getResultInfo().getResultCode();
        }
        DoViewResponseBody doViewResponseBody = new DoViewResponseBody();
        DoViewResponse doViewResponse = new DoViewResponse(new ResponseHeader(), doViewResponseBody);
        ResultInfo resultInfo = new ResultInfo();
        if (riskVerificationUtil.isDoViewRetryAllowed(txnToken, resultCode)) {
            resultInfo.setRetry(Boolean.TRUE);
            resultInfo.setResultMsg(RiskConstants.RETRY_ERROR_MSG);
        } else {
            resultInfo.setRetry(Boolean.FALSE);
            resultInfo.setResultMsg(RiskConstants.ERROR_MSG);
        }

        resultInfo.setResultStatus(ResultStatus.FAIL.getValue());
        resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
        doViewResponse.getBody().setResultInfo(resultInfo);
        return doViewResponse;
    }

    private DoVerifyResponse getDoVerifyErrorResponse(DoVerifyRequest request, String txnToken) {
        DoVerifyResponseBody doVerifyResponseBody = new DoVerifyResponseBody();
        DoVerifyResponse doVerifyResponse = new DoVerifyResponse(new ResponseHeader(), doVerifyResponseBody);
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(ResultStatus.FAIL.getValue());
        if (!(request.getBody() != null && RiskConstants.RiskVerifyMethod.PASSWORD
                .equals(request.getBody().getMethod())) && riskVerificationUtil.isDoVerifyRetryAllowed(txnToken)) {
            resultInfo.setRetry(Boolean.TRUE);
            resultInfo.setResultMsg(RiskConstants.RETRY_ERROR_MSG);
        } else {
            resultInfo.setRetry(Boolean.FALSE);
            resultInfo.setResultMsg(RiskConstants.ERROR_MSG);
        }
        resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
        doVerifyResponse.getBody().setResultInfo(resultInfo);
        return doVerifyResponse;

    }

    private DoVerifyResponse getVerificationFailedResponse(RiskVerifierDoVerifyResponse riskVerifierDoVerifyResponse) {
        RiskVerifierDoVerifyResponseBody riskVerifierDoVerifyResponseBody = riskVerifierDoVerifyResponse.getBody();
        DoVerifyResponseBody doVerifyResponseBody = new DoVerifyResponseBody();
        DoVerifyResponse doVerifyResponse = new DoVerifyResponse(new ResponseHeader(), doVerifyResponseBody);
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(ResultStatus.FAIL.getValue());
        resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
        resultInfo.setRetry(Boolean.valueOf(riskVerifierDoVerifyResponseBody.getCanRetry()));
        if (!Boolean.TRUE.toString().equals(riskVerifierDoVerifyResponseBody.getCanRetry())) {
            resultInfo.setResultMsg(RiskConstants.VERIFICATION_FAILED);
        } else {
            resultInfo.setResultMsg(riskVerifierDoVerifyResponseBody.getResultInfo().getResultMsg());
        }
        doVerifyResponse.getBody().setResultInfo(resultInfo);
        return doVerifyResponse;
    }

}
