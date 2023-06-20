package com.paytm.pgplus.workflow;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.constants.EApiResponseKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Naman on 13/06/17.
 */
public class DefaultFlowUserNotLogged extends Base {

    @Autowired
    @Qualifier("defaultUserNotLoggedFlow")
    protected IWorkFlow defaultUserNotLoggedFlow;

    @Test
    public void success() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Not.Logged.Request.Success";

        setALLSuccessResponseKeys();

        executeTestCase(defaultUserNotLoggedFlow, testCase);
    }

    @Test
    public void createOrderFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Not.Logged.Request.CreateOrderFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CREATE_ORDER);

        executeTestCase(defaultUserNotLoggedFlow, testCase);
    }

    @Test
    public void createOrderException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Not.Logged.Request.CreateOrderException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CREATE_ORDER);

        executeTestCase(defaultUserNotLoggedFlow, testCase);
    }

    @Test
    public void consultPayViewFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Not.Logged.Request.ConsultPayViewFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CONSULT_PAYVIEW, EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(defaultUserNotLoggedFlow, testCase);
    }

    @Test
    public void consultPayViewException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Not.Logged.Request.ConsultPayViewException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CONSULT_PAYVIEW, EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(defaultUserNotLoggedFlow, testCase);
    }
}
