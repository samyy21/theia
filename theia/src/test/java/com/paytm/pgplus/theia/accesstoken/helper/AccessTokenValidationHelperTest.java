package com.paytm.pgplus.theia.accesstoken.helper;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.accesstoken.exception.BaseException;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequestBody;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessTokenValidationHelperTest {

    @InjectMocks
    AccessTokenValidationHelper accessTokenValidationHelper;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateChecksum() {
        CreateAccessTokenRequestBody body = new CreateAccessTokenRequestBody();
        String token = "token", reqStr = "reqStr", mid = "mid";

        try {
            accessTokenValidationHelper.validateChecksum(token, body, null, mid);
            fail();
        } catch (BaseException baseException) {

        }
        when(tokenValidationHelper.validateJsonAndGetBodyText(reqStr)).thenReturn("reqStr");
        when(merchantExtendInfoUtils.getMerchantKey(mid)).thenReturn("merchantKey").thenThrow(
                new RequestValidationException(new ResultInfo("resultCode")));
        try {
            accessTokenValidationHelper.validateChecksum(token, body, reqStr, mid);
            fail();
        } catch (RequestValidationException ignored) {

        }
        try {
            accessTokenValidationHelper.validateChecksum(token, body, reqStr, mid);
            fail();
        } catch (RequestValidationException ignored) {

        }

    }
}