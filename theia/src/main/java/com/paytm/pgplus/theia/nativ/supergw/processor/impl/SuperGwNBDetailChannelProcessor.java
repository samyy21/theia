package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeFetchNetBankingPayChannelRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.helper.SuperGwNbDetailHelper;
import com.paytm.pgplus.theia.nativ.supergw.util.PaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theiacommon.supergw.payview.nb.NativeFetchNBPayChannelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.SUPERGW_VERSION;

@Service("superGwNbDetailChannelRequestProcessor")
public class SuperGwNBDetailChannelProcessor
        extends
        AbstractRequestProcessor<NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse, com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwNBDetailChannelProcessor.class);

    @Autowired
    private SuperGwNbDetailHelper superGwNbDetailHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private PaymentUtil paymentUtil;

    @Autowired
    private NativeFetchNetBankingPayChannelRequestProcessor NativeFetchNetBankingPayChannelRequestProcessor;

    @Override
    protected com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest preProcess(
            NativeFetchNBPayChannelRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        superGwNbDetailHelper.validateRequestParam(request);
        superGwNbDetailHelper.validateJwt(request);
        return superGwNbDetailHelper.createFetchNBRequest(request);

    }

    @Override
    protected NativeFetchNBPayChannelResponse onProcess(NativeFetchNBPayChannelRequest request,
            com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest serviceReq) throws Exception {

        NativeFetchNBPayChannelResponse fetchNBPayChannelResponse = null;
        LOGGER.info("Calling Native FetchNB api from v4 controller");
        fetchNBPayChannelResponse = NativeFetchNetBankingPayChannelRequestProcessor.process(serviceReq);
        return fetchNBPayChannelResponse;
    }

    @Override
    protected NativeFetchNBPayChannelResponse postProcess(NativeFetchNBPayChannelRequest request,
            com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest serviceReq,
            NativeFetchNBPayChannelResponse serviceRes) throws Exception {
        if (serviceRes.getHead() != null) {
            serviceRes.getHead().setVersion(SUPERGW_VERSION);
        }
        return serviceRes;
    }

}
