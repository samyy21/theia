package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.enums.ResultStatus;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.impl.LinkService;
import com.paytm.pgplus.facade.user.models.request.LinkOTPConsultResponseBody;
import com.paytm.pgplus.facade.user.models.request.SendOTPRequestV1;
import com.paytm.pgplus.facade.user.models.request.SendOtpRequest;
import com.paytm.pgplus.facade.user.models.request.ValidateOtpRequest;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import sun.awt.image.ImageWatched;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FEATURE_SEND_OTP_V5;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MOBILE_NO;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class LinkBasedPaymentHelperTest {

    @InjectMocks
    private LinkBasedPaymentHelper linkBasedPaymentHelper;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    private LinkService linkService;

    @Mock
    private Ff4jUtils ff4jUtils;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(configurationDataService.getPaytmPropertyValue(any())).thenReturn("clientId");
        when(configurationDataService.getPaytmPropertyValue(any())).thenReturn("secretkey");
    }

    @Test
    public void getLoginOTPRequest() {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(MOBILE_NO, "loginId");
        assertNotNull(linkBasedPaymentHelper.getLoginOTPRequest(request));
    }

    @Test
    public void buildSendOtpRequest() {

        HttpServletRequest request = new MockHttpServletRequest();
        assertNotNull(linkBasedPaymentHelper.buildSendOtpRequest(request));
    }

    @Test
    public void validateRequest() {
        SendOtpRequest request = new SendOtpRequest();
        request.setMid("mid");
        request.setMerchantName("merchant");
        request.setMobileNumber("99");
        request.setResendCount("3");
        request.setLinkId("link");
        linkBasedPaymentHelper.validateRequest(request, true);

    }

    @Test
    public void testValidateRequest() {

        SendOTPRequestV1 request = new SendOTPRequestV1();
        request.setHead(new TokenRequestHeader());
        request.getHead().setRequestTimestamp("3");
        request.getHead().setVersion("v2");
        SendOtpRequest body = new SendOtpRequest();
        body.setMid("mid");
        body.setMerchantName("merchant");
        body.setMobileNumber("99");
        body.setResendCount("3");
        body.setLinkId("link");
        request.setBody(body);
        request.getHead().setTokenType(TokenType.JWT);
        expectedException.expect(BaseException.class);
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return false;
            }
        };
        linkBasedPaymentHelper.validateRequest(request);
    }

    @Test
    public void testValidateRequest1() {

        expectedException.expect(BaseException.class);
        linkBasedPaymentHelper.validateRequest(new ValidateOtpRequest());
    }

    @Test
    public void generateSendOTPRequest() {

        SendOtpRequest request = new SendOtpRequest();
        request.setMobileNumber("9");
        request.setUniqueId("unique");
        request.setMid("mid");
        request.setMerchantName("merchant");
        when(ff4jUtils.isFeatureEnabledOnMid(request.getMid(), FEATURE_SEND_OTP_V5, false)).thenReturn(true);
        linkBasedPaymentHelper.generateSendOTPRequest(request);

    }

    @Test
    public void getSignInOtpRequest() {
        assertNotNull(linkBasedPaymentHelper.getSignInOtpRequest(new MockHttpServletRequest()));
    }

    @Test
    public void buildValidateOtpRequest() {

        assertNotNull(linkBasedPaymentHelper.buildValidateOtpRequest(new MockHttpServletRequest()));
    }

    @Test
    public void getValidateLoginOTPRequest() {

        assertNotNull(linkBasedPaymentHelper.getValidateLoginOTPRequest(new ValidateOtpRequest()));
    }

    @Test
    public void getValidateSignInOtpRequest() {

        assertNotNull(linkBasedPaymentHelper.getValidateSignInOtpRequest(new MockHttpServletRequest()));
    }

    @Test
    public void getLinkPaymentStatusDate() {
        assertNotNull(LinkBasedPaymentHelper.getLinkPaymentStatusDate(new Date()));
    }

    @Test
    public void createSendOTPRequestFromLink() throws FacadeCheckedException {
        SendOtpRequest request = new SendOtpRequest();
        ResultInfo resultInfo = new ResultInfo("success", "id", FacadeConstants.LINK_CONSULT_SUCCESS_RESULT_CODE, "msg");
        LinkOTPConsultResponseBody linkOTPConsultResponseBody = new LinkOTPConsultResponseBody();
        linkOTPConsultResponseBody.setResultInfo(resultInfo);
        when(linkService.getLinkOTPConsult(any(), any(), any(), any())).thenReturn(linkOTPConsultResponseBody);
        assertNotNull(linkBasedPaymentHelper.createSendOTPRequestFromLink(request));
    }
}