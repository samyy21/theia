package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.testng.Assert;

import static org.junit.Assert.*;

public class LocalizationUtilTest extends AOAUtilsTest {

    @InjectMocks
    LocalizationUtil localizationUtil;

    @Test
    public void testGetLocaleAppData() {
        Assert.assertNotNull(localizationUtil.getLocaleAppData("test"));
    }

    @Test
    public void testAddLocaleAppDataWhenInputEnhancedCashierPage() {
        EnhancedCashierPage enhancedCashierPage = new EnhancedCashierPage();
        localizationUtil.addLocaleAppData(enhancedCashierPage);
        Assert.assertNull(enhancedCashierPage.getLanguageCode());
    }

    @Test
    public void testAddLocaleAppDataWhenInputUPITransactionInfo() {
        UPITransactionInfo upiTransactionInfo = new UPITransactionInfo();
        localizationUtil.addLocaleAppData(upiTransactionInfo);
        Assert.assertNull(upiTransactionInfo.getLocalePushAppData());
    }

}