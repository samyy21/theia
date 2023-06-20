package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentservices.models.request.CreateAgreementRequest;
import com.paytm.pgplus.facade.paymentservices.models.response.AgreementDetails;
import com.paytm.pgplus.facade.paymentservices.models.response.CreateAgreementRSBody;
import com.paytm.pgplus.facade.paymentservices.models.response.CreateAgreementResponse;
import com.paytm.pgplus.facade.user.models.response.ValidateAuthCodeResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.TheiaCreateAgreementRequest;
import com.paytm.pgplus.theia.models.TheiaCreateAgreementRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenSecureRequestHeader;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @createdOn 1-July-2021
 * @author kalluru nanda kishore
 */
public class AgreementHelperTest {

    @InjectMocks
    private AgreementHelper agreementHelper;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    private IAuthentication authenticationImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getValidateAuthCodeResponse() throws FacadeCheckedException {
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID)).thenReturn(
                "client");
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY))
                .thenReturn("secretKey");
        when(authenticationImpl.validateAuthCode(any())).thenReturn(new ValidateAuthCodeResponse("response"));
        agreementHelper.getValidateAuthCodeResponse("authCode");
        verify(authenticationImpl, times(1)).validateAuthCode(any());

    }

    @Test
    public void generateRequest() {
        TheiaCreateAgreementRequest request = new TheiaCreateAgreementRequest();
        request.setBody(new TheiaCreateAgreementRequestBody());
        assertNotNull(agreementHelper.generateRequest(request));

    }

    @Test
    public void generateResponse() {
        CreateAgreementResponse createAgreementResponse = new CreateAgreementResponse();
        AgreementDetails agreementDetails = new AgreementDetails();
        createAgreementResponse.setBody(new CreateAgreementRSBody());
        createAgreementResponse.getBody().setAgreementDetails(agreementDetails);
        assertNotNull(agreementHelper.generateResponse(createAgreementResponse));
    }

    @Test
    public void generateResponseURL() {
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID)).thenReturn(
                "clientid");
        assertNotNull(agreementHelper.generateResponseURL("rediskey"));
    }

    @Test
    public void generateRandom() {
        assertNotNull(agreementHelper.generateRandom());
    }

    @Test
    public void createResponse() {

        TheiaCreateAgreementRequest request = new TheiaCreateAgreementRequest();
        request.setHead(new TokenSecureRequestHeader());
        request.getHead().setClientId("client");
        assertNotNull(agreementHelper.createResponse(request, "url"));
    }

    @Test
    public void createRequest() {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("merchantMid", "merchant");
        request.setAttribute("merchantAgreementId", "ma1");
        request.setAttribute("needImmediateActivation", "true");
        request.setAttribute("description", "descritpion");
        request.setAttribute("callBackURL", "url");
        assertNotNull(agreementHelper.createRequest(request));
    }
}