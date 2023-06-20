package com.paytm.pgplus.workflow;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.constants.EApiResponseKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Naman on 19/05/17.
 */

public class AddMoneyFlow extends Base {

    @Autowired
    @Qualifier("addMoneyFlow")
    protected IWorkFlow addMoneyFlow;

    @Test(enabled = false)
    public void success() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.Success";

        setALLSuccessResponseKeys();

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test
    public void invalidToken() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.InvalidToken";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.FETCH_USER_DETAILS);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test(enabled = false)
    public void walletLimitsFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.WalletLimits.FAIL";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.WALLET_CONSULT);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test(enabled = false)
    public void walletLimitsException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.WalletLimits.Exception";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.WALLET_CONSULT);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test
    public void topUpFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.TopUp.Failure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CREATE_TOPUP);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test
    public void topUpException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.TopUp.Exception";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CREATE_TOPUP);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test
    public void consultFailure() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.Consult.Failure";

        setALLSuccessResponseKeys();
        setFailureResponseKey(EApiResponseKeys.CONSULT_PAYVIEW, EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(addMoneyFlow, testCase);
    }

    @Test
    public void consultException() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Add.Money.Request.Consult.Exception";

        setALLSuccessResponseKeys();
        setExceptionResponseKey(EApiResponseKeys.CONSULT_PAYVIEW, EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW);

        executeTestCase(addMoneyFlow, testCase);
    }
}
