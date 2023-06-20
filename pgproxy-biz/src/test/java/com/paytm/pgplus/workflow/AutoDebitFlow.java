package com.paytm.pgplus.workflow;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.constants.EApiResponseKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Naman on 06/06/17.
 */
public class AutoDebitFlow extends Base {

    @Autowired
    @Qualifier("autoDebitFlow")
    IWorkFlow autoDebitFlow;

    @Test(enabled = false)
    public void success() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Auto.Debit.Request.Success";

        setALLSuccessResponseKeys();

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test
    public void invalidToken() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Auto.Debit.Request.InvalidToken";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.FETCH_USER_DETAILS);

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test
    public void oAuthException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Auto.Debit.Request.OAuthException";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.FETCH_USER_DETAILS);

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test(enabled = false)
    public void createOrderAndPayFailure() throws IllegalAccessException, NoSuchFieldException, IOException {

        String testCase = "Auto.Debit.Request.CreateOrderAndPayFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CREATE_ORDER_AND_PAY, EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test(enabled = false)
    public void payResultQueryFailure() throws IllegalAccessException, NoSuchFieldException, IOException {

        String testCase = "Auto.Debit.Request.PayResultQueryFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.PAY_RESULT_QUERY);

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test(enabled = false)
    public void payResultQuery_PaymentStatusFailure() throws IllegalAccessException, NoSuchFieldException, IOException {

        String testCase = "Auto.Debit.Request.PayResultQuerySuccessPaymentStatusFailure";

        setALLSuccessResponseKeys();
        setPayResultQuery_FailureTxn();

        executeTestCase(autoDebitFlow, testCase);
    }

    @Test(enabled = false)
    public void queryByAcquirementIdFailure() throws IllegalAccessException, NoSuchFieldException, IOException {

        String testCase = "Auto.Debit.Request.QueryByAcquirementFailure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.QUERY_BY_ACQUIREMENT_ID);

        executeTestCase(autoDebitFlow, testCase);
    }

}