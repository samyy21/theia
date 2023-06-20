package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.payview.StatusInfo;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class CardValidationHelperTest {

    @InjectMocks
    CardValidationHelper cardValidationHelper;

    @Mock
    BinDetailResponse binDetailResponse;

    @Mock
    BinData binDetail;

    @Mock
    BinDetailResponseBody binDetailResponseBody;

    @Mock
    BinDetailService binDetailService;

    @Mock
    StatusInfo statusInfo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrepareValidateCardResponseCoveringIf() {
        List<String> testList = new ArrayList<>();
        when(binDetailResponse.getBody()).thenReturn(binDetailResponseBody);
        when(binDetailResponseBody.getBinDetail()).thenReturn(binDetail);
        when(binDetail.getIssuingBank()).thenReturn("SBI");
        when(binDetailResponseBody.getAuthModes()).thenReturn(testList);
        when(binDetailResponseBody.getIconUrl()).thenReturn("test");
        when(binDetailResponseBody.getErrorMsg()).thenReturn("test");
        when(binDetail.getChannelCode()).thenReturn("test");
        when(binDetail.getIsIndian()).thenReturn("false");
        Assert.assertNotNull(cardValidationHelper.prepareValidateCardResponse(binDetailResponse));
    }

    @Test
    public void testPrepareValidateCardResponseCoveringElseIf() {
        List<String> testList = new ArrayList<>();
        when(binDetailResponse.getBody()).thenReturn(binDetailResponseBody);
        when(binDetailResponseBody.getBinDetail()).thenReturn(binDetail);
        when(binDetail.getIssuingBank()).thenReturn("SBI");
        when(binDetailResponseBody.getAuthModes()).thenReturn(testList);
        when(binDetailResponseBody.getIconUrl()).thenReturn("test");
        when(binDetailResponseBody.getErrorMsg()).thenReturn("test");
        when(binDetail.getChannelCode()).thenReturn("test");
        when(binDetail.getIsIndian()).thenReturn("true");
        when(binDetailResponseBody.getHasLowSuccessRate()).thenReturn(statusInfo);
        when(statusInfo.getMsg()).thenReturn("test");
        when(statusInfo.getStatus()).thenReturn("true");
        Assert.assertNotNull(cardValidationHelper.prepareValidateCardResponse(binDetailResponse));
    }

    @Test
    public void testPrepareValidateCardResponseCoveringSecondElseIf() {
        List<String> testList = new ArrayList<>();
        when(binDetailResponse.getBody()).thenReturn(binDetailResponseBody);
        when(binDetailResponseBody.getBinDetail()).thenReturn(binDetail);
        when(binDetail.getIssuingBank()).thenReturn("SBI");
        when(binDetailResponseBody.getAuthModes()).thenReturn(testList);
        when(binDetailResponseBody.getIconUrl()).thenReturn("test");
        when(binDetailResponseBody.getErrorMsg()).thenReturn("test");
        when(binDetail.getChannelCode()).thenReturn("test");
        when(binDetail.getIsIndian()).thenReturn("true");
        when(binDetailResponseBody.getHasLowSuccessRate()).thenReturn(statusInfo);
        when(statusInfo.getMsg()).thenReturn("test");
        when(statusInfo.getStatus()).thenReturn("false");
        Assert.assertNotNull(cardValidationHelper.prepareValidateCardResponse(binDetailResponse));
    }

    @Test
    public void testPrepareBinDetailsRequest() {
        Assert.assertNotNull(cardValidationHelper.prepareBinDetailsRequest("test", EChannelId.APP));
    }

}