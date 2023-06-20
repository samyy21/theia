package com.paytm.pgplus.theia.nativ.utils;

//import com.paytm.pgplus.theia.controllers.ExpressPaymentController;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
import com.paytm.pgplus.theia.services.impl.ExpressPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

public class NativeFetchCardIndexNumberTask implements Callable<ExpressCardTokenResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchCardIndexNumberTask.class);
    private ExpressCardTokenRequest expressCardTokenRequest;

    public NativeFetchCardIndexNumberTask(ExpressCardTokenRequest expressCardTokenRequest) {
        this.expressCardTokenRequest = expressCardTokenRequest;
    }

    @Autowired
    private ExpressPaymentService expressPaymentService;

    @Override
    public ExpressCardTokenResponse call() throws Exception {
        ExpressCardTokenResponse expressCardTokenResponse = expressPaymentService.getCardToken(expressCardTokenRequest,
                true);
        return expressCardTokenResponse;
    }
}
