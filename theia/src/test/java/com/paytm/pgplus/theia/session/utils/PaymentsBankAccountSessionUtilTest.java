package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.SavingsAccountInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

public class PaymentsBankAccountSessionUtilTest extends AOAUtilsTest {

    @InjectMocks
    PaymentsBankAccountSessionUtil paymentsBankAccountSessionUtil;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    SavingsAccountInfo savingsAccountInfo;

    @Mock
    UserDetailsBiz userDetailsBiz;

    @Mock
    AccountBalanceResponse accountBalanceResponse;

    @Mock
    TransactionInfo txnInfo;

    @Mock
    WalletInfo walletInfo;

    @Test
    public void testSetPaymentsBankAccountInfoInSessionWhenEpayModeNone() {
        when(theiaSessionDataService.getSavingsAccountInfoFromSession(any(), anyBoolean())).thenReturn(
                savingsAccountInfo);
        when(workFlowResponseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(workFlowResponseBean.getAccountBalanceResponse()).thenReturn(accountBalanceResponse);
        when(accountBalanceResponse.getAccountNumber()).thenReturn("1234567890");
        when(accountBalanceResponse.getEffectiveBalance()).thenReturn("100");
        when(accountBalanceResponse.getSlfdBalance()).thenReturn("10");
        when(requestBean.getAccount_ref_id()).thenReturn("test");
        when(workFlowResponseBean.getAllowedPayMode()).thenReturn(EPayMode.NONE);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(txnInfo.getTxnAmount()).thenReturn("50");
        when(txnInfo.getChargeFeeAmountNetBanking()).thenReturn(80L);
        when(savingsAccountInfo.getEffectiveBalance()).thenReturn("40");
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        verify(theiaSessionDataService, atMost(1)).getTxnInfoFromSession(any());
        paymentsBankAccountSessionUtil.setPaymentsBankAccountInfoInSession(requestBean, workFlowResponseBean);
    }

    @Test
    public void testSetPaymentsBankAccountInfoInSessionWhenEpayModeHybrid() {
        when(theiaSessionDataService.getSavingsAccountInfoFromSession(any(), anyBoolean())).thenReturn(
                savingsAccountInfo);
        when(workFlowResponseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(workFlowResponseBean.getAccountBalanceResponse()).thenReturn(accountBalanceResponse);
        when(accountBalanceResponse.getAccountNumber()).thenReturn("1234567890");
        when(accountBalanceResponse.getEffectiveBalance()).thenReturn("100");
        when(accountBalanceResponse.getSlfdBalance()).thenReturn("10");
        when(requestBean.getAccount_ref_id()).thenReturn("test");
        when(workFlowResponseBean.getAllowedPayMode()).thenReturn(EPayMode.HYBRID);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(txnInfo.getTxnAmount()).thenReturn("50");
        when(txnInfo.getChargeFeeAmountNetBanking()).thenReturn(80L);
        when(savingsAccountInfo.getEffectiveBalance()).thenReturn("40");
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        verify(theiaSessionDataService, atMost(1)).getTxnInfoFromSession(any());
        paymentsBankAccountSessionUtil.setPaymentsBankAccountInfoInSession(requestBean, workFlowResponseBean);
    }

}