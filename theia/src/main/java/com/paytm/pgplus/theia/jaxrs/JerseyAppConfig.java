package com.paytm.pgplus.theia.jaxrs;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.paytm.pgplus.theia.controllers.async.AsyncTransactionStatusController;
import com.paytm.pgplus.theia.controllers.async.TransactionStatusFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JerseyAppConfig extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyAppConfig.class);

    static {
        LOGGER.info("JerseyAppConfig loading");
    }

    public JerseyAppConfig() {
        register(TransactionStatusFilter.class).register(JacksonJaxbJsonProvider.class)
                .register(AsyncTestController.class).register(AsyncTransactionStatusController.class);

    }

}
