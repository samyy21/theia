package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentservices.models.request.CreateAgreementRQBody;
import com.paytm.pgplus.facade.paymentservices.models.request.CreateAgreementRequest;
import com.paytm.pgplus.facade.paymentservices.models.response.CreateAgreementResponse;
import com.paytm.pgplus.facade.user.models.request.ValidateAuthCodeRequest;
import com.paytm.pgplus.facade.user.models.response.ValidateAuthCodeResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.AgreementEndponts;
import com.paytm.pgplus.theia.models.TheiaCreateAgreementRequest;
import com.paytm.pgplus.theia.models.TheiaCreateAgreementRequestBody;
import com.paytm.pgplus.theia.models.response.AgreementResponse;
import com.paytm.pgplus.theia.models.response.AgreementResponseBody;
import com.paytm.pgplus.theia.models.response.TheiaCreateAgreementResponse;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service
public class AgreementHelper {

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authenticationImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementHelper.class);

    // method for getting the valid token for the authCode received from oauth
    public ValidateAuthCodeResponse getValidateAuthCodeResponse(String authCode) throws FacadeCheckedException {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);

        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);

        return authenticationImpl.validateAuthCode(new ValidateAuthCodeRequest(authCode, clientId, secretKey));
    }

    public CreateAgreementRequest generateRequest(TheiaCreateAgreementRequest theiaCreateAgreementRequest) {

        CreateAgreementRequest createAgreementRequest = new CreateAgreementRequest();
        CreateAgreementRQBody requestbody = new CreateAgreementRQBody();

        requestbody.setMerchantMid(theiaCreateAgreementRequest.getBody().getMerchantMid());
        requestbody.setMerchantAgreementId(theiaCreateAgreementRequest.getBody().getMerchantAgreementId());
        requestbody.setDescription(theiaCreateAgreementRequest.getBody().getDescription());
        requestbody.setNeedImmediateActivation(theiaCreateAgreementRequest.getBody().getNeedImmediateActivation());

        createAgreementRequest.setBody(requestbody);

        LOGGER.debug("Request sent to paymentservices for create_agreement: ", createAgreementRequest);

        return createAgreementRequest;
    }

    public TheiaCreateAgreementResponse generateResponse(CreateAgreementResponse createAgreementResponse) {

        LOGGER.debug("Response received from paymentservices after create_agreement: ", createAgreementResponse);

        TheiaCreateAgreementResponse response = new TheiaCreateAgreementResponse();

        response.setAgreementId(createAgreementResponse.getBody().getAgreementDetails().getAgreementId());
        response.setMerchantName(createAgreementResponse.getBody().getAgreementDetails().getMerchantName());
        response.setMerchantAgreementId(createAgreementResponse.getBody().getAgreementDetails()
                .getMerchantAgreementId());
        response.setCancelTime(createAgreementResponse.getBody().getAgreementDetails().getCancelTime());
        response.setCreatedTime(createAgreementResponse.getBody().getAgreementDetails().getCreatedTime());
        response.setStatus(createAgreementResponse.getBody().getAgreementDetails().getStatus());
        response.setCancelReason(createAgreementResponse.getBody().getAgreementDetails().getCancelReason());
        response.setDescription(createAgreementResponse.getBody().getAgreementDetails().getDescription());
        response.setActivateTime(createAgreementResponse.getBody().getAgreementDetails().getActivateTime());

        return response;
    }

    public String generateResponseURL(String redisKey) {

        StringBuilder url = new StringBuilder();
        StringBuilder redirectUri = new StringBuilder(ConfigurationUtil.getProperty(THEIA_BASE_URL))
                .append(AgreementEndponts.CREATE_AGREEMENT.getEndPoint());
        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);

        url.append(ConfigurationUtil.getProperty(OAUTH_BASE_URL)).append(OAUTH_AUTHORIZE_URL).append("?&scope=")
                .append(SCOPE).append("&redirect_uri=").append(redirectUri).append("/").append(redisKey)
                .append("&client_id=").append(clientId).append("&response_type=").append(RESPONSE_TYPE)
                .append("&theme=").append(THEME).append("&merchant=").append(MERCHANT).append("#/login");

        // url.append("https://accounts-staging.paytm.in/oauth2/authorize?&scope=paytm&redirect_uri=https://securegw-stage.paytm.in/theia/agreement/create/"+redisKey+"&client_id=paytm-pg-client-staging&response_type=code&theme=agreement&merchant=apple#/login");

        LOGGER.debug("Oauth url where the merchant is redirected : ", url);

        return url.toString();
    }

    public String generateRandom() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public AgreementResponse createResponse(TheiaCreateAgreementRequest request, String responseUrl) {
        AgreementResponseBody responseBody = new AgreementResponseBody();
        responseBody.setRedirectUri(responseUrl);
        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        AgreementResponse response = new AgreementResponse(responseHeader, responseBody);
        return response;
    }

    public TheiaCreateAgreementRequest createRequest(HttpServletRequest request) {

        TheiaCreateAgreementRequest theiaCreateAgreementRequest = new TheiaCreateAgreementRequest();
        TheiaCreateAgreementRequestBody body = new TheiaCreateAgreementRequestBody();
        body.setMerchantMid(request.getParameter("merchantMid"));
        body.setMerchantAgreementId(request.getParameter("merchantAgreementId"));
        if (request.getParameter("needImmediateActivation") != null) {
            body.setNeedImmediateActivation(Boolean.valueOf(request.getParameter("needImmediateActivation")));
        }
        // if request doesn't contain the parameter,set it to true by default
        body.setNeedImmediateActivation(true);
        body.setDescription(request.getParameter("description"));
        body.setCallBackURL(request.getParameter("callBackURL"));
        theiaCreateAgreementRequest.setBody(body);

        return theiaCreateAgreementRequest;

    }
}
