package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailV4Request;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeBinDetailRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.helper.SuperGwBinDetailHelper;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.SUPERGW_VERSION;

@Service("superGwBinDetailRequestProcessor")
public class SuperGwBinDetailRequestProcessor
        extends
        AbstractRequestProcessor<NativeBinDetailV4Request, NativeBinDetailResponse, NativeBinDetailRequest, NativeBinDetailResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwBinDetailRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private SuperGwBinDetailHelper superGwBinDetailHelper;

    @Autowired
    private NativeBinDetailRequestProcessor nativeBinDetailRequestProcessor;

    @Override
    protected NativeBinDetailRequest preProcess(NativeBinDetailV4Request request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        superGwBinDetailHelper.validateBin(request);
        superGwBinDetailHelper.validateJwt(request);
        return superGwBinDetailHelper.createBinDetailRequest(request);

    }

    @Override
    protected NativeBinDetailResponse onProcess(NativeBinDetailV4Request request, NativeBinDetailRequest serviceReq)
            throws Exception {

        NativeBinDetailResponse binDetailResponse = null;
        LOGGER.info("Calling Native Fetch bin api from v4 controller");
        binDetailResponse = nativeBinDetailRequestProcessor.process(serviceReq);
        return binDetailResponse;

    }

    @Override
    protected NativeBinDetailResponse postProcess(NativeBinDetailV4Request request, NativeBinDetailRequest serviceReq,
            NativeBinDetailResponse serviceRes) throws Exception {

        if (serviceRes.getHead() != null) {
            serviceRes.getHead().setVersion(SUPERGW_VERSION);
        }
        return serviceRes;
    }

}