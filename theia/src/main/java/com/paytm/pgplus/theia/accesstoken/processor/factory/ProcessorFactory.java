package com.paytm.pgplus.theia.accesstoken.processor.factory;

import com.paytm.pgplus.theia.accesstoken.processor.IRequestProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ProcessorFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SuppressWarnings("rawtypes")
    public IRequestProcessor getRequestProcessor(RequestType type) {
        if (type == RequestType.CREATE_ACCESS_TOKEN_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("accessTokenRequestProcessor");
        }
        return null;
    }

    public enum RequestType {
        CREATE_ACCESS_TOKEN_REQUEST;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
