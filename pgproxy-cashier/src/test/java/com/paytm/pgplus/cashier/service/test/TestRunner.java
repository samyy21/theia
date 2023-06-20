package com.paytm.pgplus.cashier.service.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.paytm.pgplus.cashier.service.test.payment.initiate.AddMoneyUPIWorkflowExceptionTest;

/**
 * @author Vivek Kumar
 */

public class TestRunner {

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(AddMoneyUPIWorkflowExceptionTest.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }

}
