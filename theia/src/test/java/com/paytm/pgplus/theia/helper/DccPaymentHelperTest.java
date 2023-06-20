package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class DccPaymentHelperTest {

    @InjectMocks
    DccPaymentHelper dccPaymentHelper;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    private WorkFlowRequestBean workFlowRequestBean;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setTxnToken("token");
        workFlowRequestBean.setPaytmMID("pMid");
        workFlowRequestBean.setOrderID("orderId");
    }

    @Ignore
    @Test
    public void getNativePlusJsonDccBankform() {

        assertNotNull(dccPaymentHelper.getNativePlusJsonDccBankform(workFlowRequestBean));

    }

    @Ignore
    @Test
    public void getNativeEnhanceJsonDccBankform() {

        assertNotNull(dccPaymentHelper.getNativeEnhanceJsonDccBankform(workFlowRequestBean));
    }

    @Test
    public void dccEnabledAcquirersOnMerchantInLpv() {

        PaymentRequestBean requestData = new PaymentRequestBean();
        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("cc");
        requestData.setTxnToken("txnToken");
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        response.setBody(new NativeCashierInfoResponseBody());
        NativeCashierInfoResponseBody body = response.getBody();
        body.setMerchantPayOption(new PayOption());
        body.getMerchantPayOption().setPayMethods(Collections.singletonList(new PayMethod()));
        PayMethod payMethod = body.getMerchantPayOption().getPayMethods().get(0);
        payMethod.setPayMethod(binDetail.getCardType());
        payMethod.setPayChannelOptions(Collections.singletonList(new BankCard()));
        PayChannelBase payChannelBase = payMethod.getPayChannelOptions().get(0);
        payChannelBase.setIsDisabled(new StatusInfo());
        payChannelBase.getIsDisabled().setStatus(TheiaConstant.ExtraConstants.TRUE);
        payChannelBase.getIsDisabled().setMsg(ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason());
        when(nativeSessionUtil.getCashierInfoResponse(requestData.getTxnToken())).thenReturn(response);
        assertNull(dccPaymentHelper.dccEnabledAcquirersOnMerchantInLpv(requestData, workFlowRequestBean, binDetail));
        payChannelBase.setIsDisabled(null);
        ((BankCard) payChannelBase).setDccServiceInstIds(Collections.singletonList("id"));
        binDetail.setCardName("creditcard");
        payChannelBase.setPayChannelOption(binDetail.getCardType() + "_" + binDetail.getCardName());
        assertEquals(((BankCard) payChannelBase).getDccServiceInstIds(),
                dccPaymentHelper.dccEnabledAcquirersOnMerchantInLpv(requestData, workFlowRequestBean, binDetail));
    }

    @Test
    public void dccPageTobeRendered() {

        workFlowRequestBean.setDccSupported(true);
        workFlowRequestBean.setPaymentCallFromDccPage(true);
        assertFalse(dccPaymentHelper.dccPageTobeRendered(workFlowRequestBean));
    }

    @Test
    public void dccPaymentAllowed() {
        workFlowRequestBean.setDccSupported(true);
        workFlowRequestBean.setPaymentCallFromDccPage(true);
        assertTrue(dccPaymentHelper.dccPaymentAllowed(workFlowRequestBean));
    }
}