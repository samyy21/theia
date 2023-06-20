package com.paytm.pgplus.theia.aoa.processor.factory;

import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class AoARequestProcessorFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public enum RequestType {
        UNI_PAY, NATIVE_SUBSCRIPTION;
    }

    @SuppressWarnings("rawtypes")
    public IRequestProcessor getRequestProcessor(AoARequestProcessorFactory.RequestType type) {
        if (type == AoARequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION) {
            // TODO: Absolute or consolidate? not sure about the name of
            // processor and requestType.
            return (IRequestProcessor) applicationContext
                    .getBean("nativeAbsoluteSubscriptionTransactionRequestProcessor");
        } else if (type == AoARequestProcessorFactory.RequestType.UNI_PAY) {
            // TODO: Absolute or consolidate? not sure about the name of
            // processor and requestType.
            return (IRequestProcessor) applicationContext.getBean("nativeAbsoluteSeamlessTransactionRequestProcessor");
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
