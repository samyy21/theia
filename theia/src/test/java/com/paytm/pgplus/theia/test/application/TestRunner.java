package com.paytm.pgplus.theia.test.application;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;

/**
 * @author kartik
 * @date 30-05-2017
 */
public class TestRunner {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { com.paytm.pgplus.theia.test.testflow.SeamlessACSPaymentServiceImplTest.class });
        testng.addListener(tla);
        testng.run();
    }

}
