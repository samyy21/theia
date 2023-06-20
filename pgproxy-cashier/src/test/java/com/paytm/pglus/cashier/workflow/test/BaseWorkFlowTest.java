/**
 * 
 */
package com.paytm.pglus.cashier.workflow.test;

import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;

/**
 * @author amit.dubey
 *
 */
@ContextConfiguration(locations = { "classpath:/LooperClientContext.xml", "classpath:/cashier-context-test.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseWorkFlowTest implements ApplicationContextAware {
    @Autowired
    protected ApplicationContext applicationContext;

    protected PaymentServiceImpl paymentServiceImpl;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        paymentServiceImpl = (PaymentServiceImpl) applicationContext.getBean("paymentServiceImpl");
    }

}
