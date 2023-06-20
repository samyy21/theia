package com.paytm.pgplus.biz.core.risk;

import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoViewRequest;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoViewRequestBody;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponse;
import com.paytm.pgplus.facade.risk.services.IRiskVerify;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RiskUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(RiskUtil.class);

    @Autowired
    @Qualifier("riskVerificationService")
    private IRiskVerify riskVerificationService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    public RiskVerifierCacheProcessingPayload setRiskDoViewResponseInCache(String mid, String orderId, String transId,
            String verifyId, String method, String txnToken) {
        RiskVerifierCacheProcessingPayload payload = new RiskVerifierCacheProcessingPayload();
        if (StringUtils.isBlank(transId)) {
            return payload;
        }
        try {
            RiskVerifierDoViewResponse doViewResponse = getDoViewResponse(verifyId, method);
            RiskVerifierPayload riskVerifierPayload = setRiskDoViewInCache(doViewResponse, mid, orderId, transId,
                    txnToken);
            if (riskVerifierPayload != null) {
                payload.setSuccessful(true);
                if (RiskConstants.RiskVerifyMethod.PASSWORD.equals(riskVerifierPayload
                        .getRiskVerifierDoViewResponseBody().getMethod())) {
                    populateRiskPasswordVerifierErrorMessage(riskVerifierPayload, payload);
                }
                return payload;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while calling doView.", e);
        }
        return payload;
    }

    private void populateRiskPasswordVerifierErrorMessage(RiskVerifierPayload riskVerifierPayload,
            RiskVerifierCacheProcessingPayload payload) {
        if (MapUtils.isNotEmpty(riskVerifierPayload.getRiskVerifierDoViewResponseBody().getRenderData())) {
            if (riskVerifierPayload.getRiskVerifierDoViewResponseBody().getRenderData()
                    .containsKey(RiskConstants.RISK_STATE_PARAM)
                    && StringUtils.isEmpty(riskVerifierPayload.getRiskVerifierDoViewResponseBody().getRenderData()
                            .get(RiskConstants.RISK_STATE_PARAM))) {
                payload.setSuccessful(false);
                switch (riskVerifierPayload.getRiskVerifierDoViewResponseBody().getRenderData()
                        .get(RiskConstants.RISK_RESPONSE_CODE_PARAM)) {
                case RiskConstants.USER_BLOCKED_CODE:
                    payload.setMessage(RiskConstants.USER_BLOCKED_MESSAGE);
                    break;
                case RiskConstants.PASSWORD_DOES_NOT_EXIST_CODE:
                    payload.setMessage(RiskConstants.PASSWORD_DOES_NOT_EXIST_MESSAGE);
                    break;
                case RiskConstants.PASSWORD_LIMIT_BREACHED_CODE:
                    payload.setMessage(RiskConstants.PASSWORD_LIMIT_BREACHED_MESSAGE);
                    break;
                default:
                    break;
                }
            }
        }
    }

    public RiskVerifierDoViewResponse getDoViewResponse(String verifyId, String method) throws FacadeCheckedException {
        AlipayExternalRequestHeader header = RequestHeaderGenerator.getHeader(ApiFunctions.RISK_VERIFIER_DO_VIEW);
        RiskVerifierDoViewRequestBody doViewRequestBody = new RiskVerifierDoViewRequestBody(verifyId, method);
        RiskVerifierDoViewRequest request = new RiskVerifierDoViewRequest(header, doViewRequestBody);
        RiskVerifierDoViewResponse doViewResponse = riskVerificationService.doView(request);
        return doViewResponse;
    }

    public RiskVerifierPayload setRiskDoViewInCache(RiskVerifierDoViewResponse doViewResponse, String mid,
            String orderId, String transId, String txnToken) {
        RiskVerifierPayload riskVerifierPayload = null;
        if (ResultCode.SUCCESS.getCode().equals(doViewResponse.getBody().getResultInfo().getResultCode())) {
            riskVerifierPayload = new RiskVerifierPayload(transId, mid, orderId, doViewResponse.getBody());
            riskVerifierPayload.setTxnToken(txnToken);
            String key = RiskConstants.DO_VIEW_CACHE_KEY_PREFIX + transId;
            theiaSessionRedisUtil.set(key, riskVerifierPayload, 900);
        }
        return riskVerifierPayload;
    }
}
