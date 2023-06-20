package com.paytm.pgplus.theia.nativ;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class OAuthHelperTest {

    @InjectMocks
    private OAuthHelper oAuthHelper = new OAuthHelper();

    @Mock
    private IAuthentication authFacade;

    @Mock
    IUserMapping userMapping;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    private MappingUtil mappingUtil;

    @Mock
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidateSSOToken() throws FacadeCheckedException {
        InitiateTransactionRequestBody body = new InitiateTransactionRequestBody();
        body.setMid("mid");
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("value");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(true);
        when(authFacade.fetchUserDetailsV2(any())).thenReturn(new FetchUserDetailsResponse(new UserDetails()));
        oAuthHelper.validateSSOToken("token", body);

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("userId");
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
        when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse(userDetails));
        body.setCorporateCustId("custId");
        oAuthHelper.validateSSOToken("token", body);

        try {
            when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
            when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse("message"));
            oAuthHelper.validateSSOToken("token", body);
        } catch (PaymentRequestProcessingException e) {
        }

        try {
            oAuthHelper.validateSSOToken("", body);
        } catch (PaymentRequestProcessingException e) {
        }

    }

    @Test
    public void testValidateSSOTokenException() {
        InitiateTransactionRequestBody body = new InitiateTransactionRequestBody();
        body.setMid("mid");
        body.setPaytmSsoToken("ssoToken");
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("value");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        try {
            oAuthHelper.validateSSOToken("token", body);
        } catch (Exception e) {
        }
        try {
            oAuthHelper.validateSSOToken(body);
        } catch (Exception e) {
        }
        try {
            oAuthHelper.validateSSOToken("token");
        } catch (Exception e) {
        }
        try {
            oAuthHelper.validateSSOToken("token", "mid");
        } catch (Exception e) {
        }
    }

    @Test
    public void testValidateSSOToken1() throws FacadeCheckedException {
        InitiateTransactionRequestBody body = new InitiateTransactionRequestBody();
        body.setMid("mid");
        body.setPaytmSsoToken("ssoToken");
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("value");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(true);
        when(authFacade.fetchUserDetailsV2(any())).thenReturn(new FetchUserDetailsResponse(new UserDetails()));
        oAuthHelper.validateSSOToken(body);

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("userId");
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
        when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse(userDetails));
        body.setCorporateCustId("custId");
        oAuthHelper.validateSSOToken(body);

        try {
            when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
            when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse("message"));
            oAuthHelper.validateSSOToken(body);
        } catch (PaymentRequestProcessingException e) {
        }

        try {
            body.setPaytmSsoToken(null);
            oAuthHelper.validateSSOToken(body);
        } catch (PaymentRequestProcessingException e) {
        }
    }

    @Test
    public void testValidateSSOToken2() throws FacadeCheckedException {
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("value");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("userId");
        when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse(userDetails));
        oAuthHelper.validateSSOToken("token");

        try {
            when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse("message"));
            oAuthHelper.validateSSOToken("token");
        } catch (PaymentRequestProcessingException e) {
        }

        try {
            oAuthHelper.validateSSOToken("");
        } catch (PaymentRequestProcessingException e) {
        }
    }

    @Test
    public void testValidateSSOToken3() throws FacadeCheckedException {
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("value");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(true);
        when(authFacade.fetchUserDetailsV2(any())).thenReturn(new FetchUserDetailsResponse(new UserDetails()));
        oAuthHelper.validateSSOToken("token", "mid");

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("userId");
        when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
        when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse(userDetails));
        oAuthHelper.validateSSOToken("token", "mid");

        try {
            when(ff4jUtils.isFeatureEnabledOnMid(any())).thenReturn(false);
            when(authFacade.fetchUserDetails(any())).thenReturn(new FetchUserDetailsResponse("message"));
            oAuthHelper.validateSSOToken("token", "mid");
        } catch (PaymentRequestProcessingException e) {
        }

        try {
            oAuthHelper.validateSSOToken("", "mid");
        } catch (PaymentRequestProcessingException e) {
        }
    }
}