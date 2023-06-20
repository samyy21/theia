package com.paytm.pgplus.workflow;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.constants.EApiResponseKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Naman on 05/06/17.
 */
public class DefaultFlowUserLoggedIn extends Base {

    @Autowired
    @Qualifier("defaultLoggedInFlow")
    protected IWorkFlow defaultLoggedInFlow;

    @Test(enabled = false)
    public void success() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.Success";

        setALLSuccessResponseKeys();

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test
    public void testInvalidToken() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.InvalidToken";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.FETCH_USER_DETAILS);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test
    public void createOrderFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.CreateOrderFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CREATE_ORDER);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test
    public void createOrderException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.CreateOrderException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CREATE_ORDER);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test
    public void consultPayViewFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.ConsultPayViewFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CONSULT_PAYVIEW);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test
    public void consultPayViewException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.ConsultPayViewException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CONSULT_PAYVIEW);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test(enabled = false)
    public void addAndPayViewConsultPayViewFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.AddAndPay.ConsultPayViewFailure";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

    @Test(enabled = false)
    public void addAndPayViewConsultPayViewException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Default.User.Logged.In.Request.AddAndPay.ConsultPayViewException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(defaultLoggedInFlow, testCase);
    }

}
