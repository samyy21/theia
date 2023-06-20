package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.utils.AOAGatewayScoreCalculator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.user.models.CredsAllowed;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetails;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.models.MerchantDetails;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.SarvatraVPAMapInfo;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class SarvatraVPASessionUtilTest extends AOAUtilsTest {

    @InjectMocks
    SarvatraVPASessionUtil sessionUtil;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    SarvatraVPAMapInfo sarvatraVPAMapInfo;

    @Mock
    UserProfileSarvatra userProfileSarvatra;

    @Mock
    PaytmVpaDetails paytmVpaDetails;

    @Mock
    SarvatraVpaDetails sarvatraVpaDetails;

    @Mock
    PaytmBanksVpaDefaultDebitCredit paytmBanksVpaDefaultDebitCredit;

    @Mock
    CredsAllowed credsAllowed;

    @Mock
    MerchantDataUtil merchantDataUtil;

    @Test
    public void testSetSarvatraVpaInfoInSessionWhenMerchantPushExpressUpiEnabled() {
        List<SarvatraVpaDetails> testList = new ArrayList<>();
        testList.add(sarvatraVpaDetails);

        List<CredsAllowed> testCredsList = new ArrayList<>();
        testCredsList.add(credsAllowed);

        when(theiaSessionDataService.getSarvatraVPAInfoFromSession(any(), anyBoolean())).thenReturn(sarvatraVPAMapInfo);
        when(workFlowResponseBean.getSarvatraUserProfile()).thenReturn(userProfileSarvatra);
        when(userProfileSarvatra.getResponse()).thenReturn(paytmVpaDetails);
        when(paytmVpaDetails.getVpaDetails()).thenReturn(testList);
        when(sarvatraVpaDetails.getDefaultDebit()).thenReturn(paytmBanksVpaDefaultDebitCredit);
        when(paytmBanksVpaDefaultDebitCredit.isInvalidVpa()).thenReturn(false);
        when(sarvatraVpaDetails.getName()).thenReturn("test");
        when(paytmBanksVpaDefaultDebitCredit.getCredsAllowed()).thenReturn(testCredsList);
        when(paytmBanksVpaDefaultDebitCredit.getMbeba()).thenReturn("test");
        when(paytmBanksVpaDefaultDebitCredit.getAccount()).thenReturn("1234567890");
        when(paytmBanksVpaDefaultDebitCredit.getBank()).thenReturn("SBI");
        when(sarvatraVpaDetails.isPrimary()).thenReturn(true);
        when(workFlowResponseBean.isMerchantUpiPushExpressEnabled()).thenReturn(true);
        when(workFlowResponseBean.isAddUpiPushExpressEnabled()).thenReturn(true);
        when(merchantDataUtil.getMerchantDetails(anyString(), anyBoolean())).thenReturn(new MerchantDetails());
        sessionUtil.setSarvatraVpaInfoInSession(requestBean, workFlowResponseBean);
        verify(merchantDataUtil, atMost(2)).getMerchantDetails(anyString(), anyBoolean());

    }

    @Test
    public void testSetSarvatraVpaInfoInSessionWhenMerchantPushExpressUpiDisabled() {
        List<SarvatraVpaDetails> testList = new ArrayList<>();
        testList.add(sarvatraVpaDetails);

        List<CredsAllowed> testCredsList = new ArrayList<>();
        testCredsList.add(credsAllowed);

        when(theiaSessionDataService.getSarvatraVPAInfoFromSession(any(), anyBoolean())).thenReturn(sarvatraVPAMapInfo);
        when(workFlowResponseBean.getSarvatraUserProfile()).thenReturn(userProfileSarvatra);
        when(userProfileSarvatra.getResponse()).thenReturn(paytmVpaDetails);
        when(paytmVpaDetails.getVpaDetails()).thenReturn(testList);
        when(sarvatraVpaDetails.getDefaultDebit()).thenReturn(null);
        when(sarvatraVpaDetails.getName()).thenReturn("test");

        when(sarvatraVpaDetails.isPrimary()).thenReturn(true);
        when(workFlowResponseBean.isMerchantUpiPushExpressEnabled()).thenReturn(false);
        when(workFlowResponseBean.isAddUpiPushExpressEnabled()).thenReturn(false);
        when(workFlowResponseBean.isMerchantUpiPushEnabled()).thenReturn(true);
        when(workFlowResponseBean.isAddUpiPushEnabled()).thenReturn(true);
        when(merchantDataUtil.getMerchantDetails(anyString(), anyBoolean())).thenReturn(new MerchantDetails());
        sessionUtil.setSarvatraVpaInfoInSession(requestBean, workFlowResponseBean);
        verify(merchantDataUtil, atMost(2)).getMerchantDetails(anyString(), anyBoolean());

    }

}