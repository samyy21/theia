package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.theia.enums.CardValidationCardType;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.testng.Assert;

import static org.junit.Assert.*;

public class CardTypeUtilTest extends AOAUtilsTest {

    @InjectMocks
    CardTypeUtil cardTypeUtil;

    @Test
    public void testCheckForCardTypeReturnTrue() {
        BinData binData = new BinData();
        binData.setChannelName("EXPIRED");

        BinDetailResponseBody binDetailResponseBody = new BinDetailResponseBody();
        binDetailResponseBody.setBinDetail(binData);

        BinDetailResponse binDetailResponse = new BinDetailResponse();
        binDetailResponse.setBody(binDetailResponseBody);

        Assert.assertTrue(cardTypeUtil.checkForCardType(binDetailResponse, CardValidationCardType.EXPIRED));

    }

    @Test
    public void testCheckForCardTypeReturnFalse() {
        Assert.assertFalse(cardTypeUtil.checkForCardType(null, CardValidationCardType.EXPIRED));

    }

}