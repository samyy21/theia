package com.paytm.pgplus.workflow;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Naman on 14/06/17.
 */
public class BuyerPaysChargeFlow extends Base {

    @Autowired
    @Qualifier("buyerPaysChargeFlow")
    protected IWorkFlow buyerPaysChargeFlow;

    @Test
    public void success() throws IOException, NoSuchFieldException, IllegalAccessException {

        String testCase = "Buyer.Pays.Charge.Post.Login.Request.Success";

        setALLSuccessResponseKeys();

        executeTestCase(buyerPaysChargeFlow, testCase);
    }

}
