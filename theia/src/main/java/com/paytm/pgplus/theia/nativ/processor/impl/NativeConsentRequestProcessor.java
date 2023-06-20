package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.facade.consent.model.ConsentServiceRequest;
import com.paytm.pgplus.facade.consent.model.ConsentServiceRequestBody;
import com.paytm.pgplus.facade.consent.model.ConsentServiceResponse;
import com.paytm.pgplus.facade.consent.service.IConsentService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponse;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponseBody;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponseHead;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Created by: satyamsinghrajput at 31/10/19
 */
@Service("nativeConsentRequestProcessor")
public class NativeConsentRequestProcessor
        extends
        AbstractRequestProcessor<ConsentServiceRequest, NativeConsentResponse, ConsentServiceRequest, ConsentServiceResponse> {

    @Autowired
    IConsentService consentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeConsentRequestProcessor.class);

    @Override
    protected ConsentServiceRequest preProcess(ConsentServiceRequest request) {
        if (!validateRequest(request)) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        return request;
    }

    @Override
    protected ConsentServiceResponse onProcess(ConsentServiceRequest request,
            ConsentServiceRequest consentServiceRequest) throws Exception {

        ConsentServiceResponse response = new ConsentServiceResponse();
        ConsentServiceRequestBody requestBody = consentServiceRequest.getBody();
        response = consentService.setUserConsent(requestBody);

        return response;
    }

    @Override
    protected NativeConsentResponse postProcess(ConsentServiceRequest request,
            ConsentServiceRequest consentServiceRequest, ConsentServiceResponse consentServiceResponse)
            throws Exception {
        NativeConsentResponse response = new NativeConsentResponse();
        NativeConsentResponseBody body = new NativeConsentResponseBody();
        response.setHead(generateHead());

        if (consentServiceResponse.getStatus().equals(ResultCode.SUCCESS.getCode())) {
            body.setSeqNo(consentServiceResponse.getSeqNo());
            body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
            response.setBody(body);
        } else {
            body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.FAILED));
            String respCode = consentServiceResponse.getRespCode();
            if ("1006".equals(respCode)) {
                LOGGER.info("Invalid Device id");
                body.getResultInfo().setResultMsg("Invalid Device id");
            } else if ("514".equals(respCode)) {
                LOGGER.info("Upi user not found");
                body.getResultInfo().setResultMsg("Upi user not found");
            } else {
                if ("INT-1051".equals(respCode)) {
                    LOGGER.info("invalid JWT Token");
                }
                body.getResultInfo().setResultMsg(com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultMsg());
            }
            response.setBody(body);
        }
        return response;

    }

    private boolean validateRequest(ConsentServiceRequest consentServiceRequest) {
        ConsentServiceRequestBody body = consentServiceRequest.getBody();
        if (StringUtils.isNotBlank(body.getQueryParam().getDeviceId())
                && StringUtils.isNotBlank(body.getQueryParam().getSeqNo())
                && StringUtils.isNotBlank(body.getRequestParam().getCustId())
                && !CollectionUtils.isEmpty(body.getRequestParam().getConsentList())) {
            return true;
        }
        return false;
    }

    private NativeConsentResponseHead generateHead() {
        NativeConsentResponseHead head = new NativeConsentResponseHead();
        head.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        head.setVersion("v2");
        return head;
    }

}
