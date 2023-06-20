package com.paytm.pgplus.theia.accesstoken.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.accesstoken.helper.AccessTokenValidationHelper;
import com.paytm.pgplus.theia.accesstoken.model.AccessTokenBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequest;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequestBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.model.response.CreateAccessTokenServiceResponse;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.sun.org.apache.xerces.internal.util.HTTPInputSource;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AccessTokenRequestProcessorTest {

    @InjectMocks
    AccessTokenRequestProcessor accessTokenRequestProcessor;

    @Mock
    private AccessTokenValidationHelper accessTokenValidationHelper;

    @Mock
    private INativeValidationService nativeValidationService;

    @Mock
    private AccessTokenUtils accessTokenUtils;

    @Mock
    FF4JUtil ff4JUtil;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void preProcess() throws IOException {

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setBody(new CreateAccessTokenRequestBody());
        request.getBody().setMid("");
        request.getBody().setPaytmSsoToken("ssoTokenn");
        request.getBody().setCustId("custId");
        request.getBody().setUserInfo(new UserInfo());
        request.setHead(new TokenRequestHeader());
        when(nativeValidationService.validateSSOToken(any(), any())).thenReturn(new UserDetailsBiz())
                .thenThrow(new PaymentRequestProcessingException()).thenReturn(new UserDetailsBiz());
        when(ff4JUtil.isFeatureEnabled(TheiaConstant.ExtraConstants.ACCESS_TOKEN_INVALID_SSO_RESPONSE_CODE, "mid"))
                .thenReturn(true);
        try {
            accessTokenRequestProcessor.preProcess(request);
            fail();
        } catch (RequestValidationException e) {

        }
        request.getBody().setMid("mid");
        request.getBody().setReferenceId("reference");
        try {
            accessTokenRequestProcessor.preProcess(request);
            fail();
        } catch (RequestValidationException e) {

        }
        try {
            accessTokenRequestProcessor.preProcess(request);
            fail();
        } catch (RequestValidationException e) {

        }
        request.getBody().setReferenceId("referenceId");
        request.getHead().setTokenType(TokenType.CHECKSUM);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(mock(ServletInputStream.class));
        RequestAttributes requestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes, false);
        when(accessTokenValidationHelper.validateChecksum(any(), any(), any(), any())).thenReturn(new Object());
        accessTokenRequestProcessor.preProcess(request);

    }

    @Test
    public void testPreProcess() throws Exception {

        AccessTokenBody accessTokenBody = new AccessTokenBody();
        accessTokenBody.setIdempotent(false);
        when(accessTokenUtils.createAccessToken(any(CreateAccessTokenServiceRequest.class)))
                .thenReturn(accessTokenBody);
        assertNotNull(accessTokenRequestProcessor.onProcess(new CreateAccessTokenRequest(),
                new CreateAccessTokenServiceRequest()));
    }

    @Test
    public void testPreProcess1() throws Exception {

        new MockUp<AccessTokenUtils>() {
            @mockit.Mock
            public ResponseHeader createResponseHeader() {
                return new ResponseHeader();
            }
        };
        assertNotNull(accessTokenRequestProcessor.postProcess(new CreateAccessTokenRequest(),
                new CreateAccessTokenServiceRequest(), new CreateAccessTokenServiceResponse()));

    }
}