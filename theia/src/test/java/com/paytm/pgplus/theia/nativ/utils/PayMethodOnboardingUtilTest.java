package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.enums.EPayMethod;
import org.junit.Test;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PayMethodOnboardingUtilTest extends AOAUtilsTest {

    @Mock
    UserDetailsBiz userDetails;

    @Test
    public void testGetOnboardingWhenUserPostPaidStatusBlank() {
        when(userDetails.getPostpaidStatus()).thenReturn("");
        Assert.assertFalse(PayMethodOnboardingUtil.getOnboarding(EPayMethod.ATM, userDetails));
    }

    @Test
    public void testGetOnboardingWhenReturnTrue() {
        when(userDetails.getPostpaidStatus()).thenReturn("WHITELISTED");
        Assert.assertTrue(PayMethodOnboardingUtil.getOnboarding(EPayMethod.PAYTM_DIGITAL_CREDIT, userDetails));
    }

    @Test
    public void testGetOnboardingWhenReturnFalse() {
        when(userDetails.getPostpaidStatus()).thenReturn("test");
        Assert.assertFalse(PayMethodOnboardingUtil.getOnboarding(EPayMethod.ATM, userDetails));
    }

}