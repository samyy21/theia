package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pglus.cashier.workflow.test.BaseWorkFlowTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderUPI;
import com.paytm.pgplus.cashier.workflow.UPIWorkFlow;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

public class AddMoneyUPIWorkflowExceptionTest extends BaseCashierWorkFlowTest {

    @Autowired
    @Qualifier("UPIWorkflow")
    UPIWorkFlow UPIWorkflow;

    @Autowired
    AddMoneyCashierRequestBuilderUPI cashierUpiRequest;

    @Test(expected = CashierCheckedException.class)
    public void testNullCashierRequest() throws PaytmValidationException, CashierCheckedException {
        UPIWorkflow.initiatePayment(null);
    }

    @Test(expected = CashierCheckedException.class)
    public void testNullPaymentRequest() throws CashierCheckedException, PaytmValidationException {
        CashierRequest cashierRequest = cashierUpiRequest.getUPIRequest();
        cashierRequest.setPaymentRequest(null);
        UPIWorkflow.initiatePayment(cashierRequest);
    }

    /*
     * @Test public void testCloseOrder() throws NoSuchMethodException,
     * SecurityException, IllegalAccessException, IllegalArgumentException,
     * InvocationTargetException {
     * 
     * UPIWorkFlow upi = new UPIWorkFlow(); Method privateMethod =
     * UPIWorkFlow.class.getDeclaredMethod("closeOrder", String.class,
     * String.class); privateMethod.setAccessible(true);
     * privateMethod.invoke(upi, "master86636472935906",
     * "20170531111212800110166467300009542");
     * 
     * }
     */
}
