package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.testng.Assert;

import static org.junit.Assert.*;

public class EcomTokenHelperTest extends AOAUtilsTest {

    @InjectMocks
    EcomTokenHelper ecomTokenHelper;

    @Test(expected = TheiaDataMappingException.class)
    public void testValidateAndCheckIfEcomTokenTransactionAllowed() throws TheiaDataMappingException {
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();

        flowRequestBean.setEcomTokenTxn(true);
        flowRequestBean.setNativeAddMoney(false);
        flowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);
        flowRequestBean.setPaymentTypeId("EMI");
        ecomTokenHelper.validateAndCheckIfEcomTokenTransactionAllowed(flowRequestBean);
    }

}