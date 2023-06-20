package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetails;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class VPAHelperTest extends AOAUtilsTest {

    @InjectMocks
    VPAHelper vpaHelper;

    @Mock
    UserProfileSarvatra userProfileSarvatra;

    @Mock
    PaytmVpaDetails paytmVpaDetails;

    @Test
    public void testPopulateVPALinkedBankAccounts() {
        PaytmBanksVpaDefaultDebitCredit defaultDebitCredit1 = new PaytmBanksVpaDefaultDebitCredit();
        PaytmBanksVpaDefaultDebitCredit defaultDebitCredit2 = new PaytmBanksVpaDefaultDebitCredit();

        defaultDebitCredit1.setAccount("SBI");
        defaultDebitCredit2.setAccount("HDFC");
        SarvatraVpaDetails sarvatraVpaDetails = new SarvatraVpaDetails();
        sarvatraVpaDetails.setPrimary(true);
        sarvatraVpaDetails.setDefaultDebit(defaultDebitCredit1);
        List<SarvatraVpaDetails> testVpaDetails = new ArrayList<>();
        testVpaDetails.add(sarvatraVpaDetails);
        List<PaytmBanksVpaDefaultDebitCredit> testBankAccounts = new ArrayList<>();
        testBankAccounts.add(defaultDebitCredit2);
        when(userProfileSarvatra.getResponse()).thenReturn(paytmVpaDetails);
        when(paytmVpaDetails.getVpaDetails()).thenReturn(testVpaDetails);
        when(paytmVpaDetails.getBankAccounts()).thenReturn(testBankAccounts);
        vpaHelper.populateVPALinkedBankAccounts(userProfileSarvatra);
        Assert.assertEquals(2, testVpaDetails.size());
    }

    @Test
    public void testSetMaskedMerchantVpa() {
        Assert.assertNotNull(VPAHelper.setMaskedMerchantVpa("test", "test"));
    }

}