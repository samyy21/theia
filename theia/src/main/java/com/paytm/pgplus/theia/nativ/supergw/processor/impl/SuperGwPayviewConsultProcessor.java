package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoV4Request;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativePayviewConsultRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.helper.SuperGwPayviewConsultHelper;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.SUPERGW_VERSION;

@Service("superGwPayviewConsultProcessor")
public class SuperGwPayviewConsultProcessor
        extends
        AbstractRequestProcessor<NativeCashierInfoV4Request, NativeCashierInfoResponse, NativeCashierInfoContainerRequest, NativeCashierInfoResponse> {

    @Autowired
    private SuperGwPayviewConsultHelper payviewConsultHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    private SuperGwPayviewConsultHelper superGwPayviewConsultHelper;

    @Override
    protected NativeCashierInfoContainerRequest preProcess(NativeCashierInfoV4Request request) throws Exception {

        nativeValidationService.validateMid(request.getBody().getMid());
        superGwPayviewConsultHelper.validateJwt(request);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = payviewConsultHelper
                .createFetchPayOptionsRequest(request, request.getBody().getMid());
        nativeCashierInfoContainerRequest.setSuperGwApiHit(true);
        return nativeCashierInfoContainerRequest;

    }

    @Override
    protected NativeCashierInfoResponse onProcess(NativeCashierInfoV4Request request,
            NativeCashierInfoContainerRequest serviceReq) throws Exception {
        NativeCashierInfoResponse nativeCashierInfoResponse;
        nativeCashierInfoResponse = nativePayviewConsultRequestProcessor.process(serviceReq);
        return nativeCashierInfoResponse;

    }

    @Override
    protected NativeCashierInfoResponse postProcess(NativeCashierInfoV4Request request,
            NativeCashierInfoContainerRequest serviceReq, NativeCashierInfoResponse serviceRes) throws Exception {

        if (serviceRes.getHead() != null) {
            serviceRes.getHead().setVersion(SUPERGW_VERSION);
            MDC.put(VERSION, SUPERGW_VERSION);
        }
        return serviceRes;

    }

}
