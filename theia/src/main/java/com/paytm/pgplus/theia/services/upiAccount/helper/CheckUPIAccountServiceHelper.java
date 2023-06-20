package com.paytm.pgplus.theia.services.upiAccount.helper;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.VpaDetailV4;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponse;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponseBody;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.facade.constants.FacadeConstants.AUTHORIZATION;
import static com.paytm.pgplus.facade.constants.FacadeConstants.HTTP_SUCCESS_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service
public class CheckUPIAccountServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckUPIAccountServiceHelper.class);

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Deprecated
    public void validateRequest(CheckUPIAccountRequest request) {

        if (request.getHead() == null
                || request.getBody() == null
                || (StringUtils.isBlank(request.getBody().getMobileNumber()) || StringUtils.isBlank(request.getBody()
                        .getMid()))) {
            throw RequestValidationException.getException("invalid request params");
        }
        boolean isCheckUPIAccountSupported = merchantPreferenceService.isCheckUPIAccountSupported(request.getBody()
                .getMid());
        if (!isCheckUPIAccountSupported) {
            throw RequestValidationException.getException("check upi account preference not enabled");
        }
        if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    @Deprecated
    public String getUserIdFromOAuth(CheckUPIAccountRequest checkUPIAccountRequest) {

        HttpRequestPayload<String> payload = generatePayloadForV2User(checkUPIAccountRequest.getBody()
                .getMobileNumber());
        Response response = null;
        try {
            response = JerseyHttpClient.sendHttpGetRequest(payload);
        } catch (Exception e) {
            LOGGER.error("Failed to get custId from OAuth");
        }
        return validateAndProcessSuccessResponse(response);
    }

    @Deprecated
    public GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserProfileVpaV4(
            CheckUPIAccountRequest checkUPIAccountRequest, String userId) {

        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest();

        if (StringUtils.isNotBlank(userId) && checkUPIAccountRequest.getBody() != null) {
            fetchUserPaytmVpaRequest.setUserId(userId);
            fetchUserPaytmVpaRequest.setRequestId(RequestIdGenerator.generateRequestId());
            fetchUserPaytmVpaRequest.setDeviceId(checkUPIAccountRequest.getBody().getDeviceId());
        }

        return sarvatraVpaDetails.fetchUserProfileVpaV4(fetchUserPaytmVpaRequest);
    }

    public String validateAndProcessSuccessResponse(Response response) {
        String userId = null;
        if (HTTP_SUCCESS_CODE.equals(response.getStatus())) {
            String entityString = (String) response.getEntity();
            try {
                Map<String, Object> jsonMap = JsonMapper.mapJsonToObject(entityString, Map.class);
                if (jsonMap.get("userId") != null) {
                    userId = String.valueOf(jsonMap.get("userId"));
                }
            } catch (Exception e) {
                LOGGER.error("Error while getting userId from OAuth :{}", e.getMessage());
            }
        }
        return userId;
    }

    public HttpRequestPayload<String> generatePayloadForV2User(String mobileNumber) {

        String clientId = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_ID).getValue();
        String clientSecret = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_SECRET_KEY).getValue();

        HttpRequestPayload<String> payload = new HttpRequestPayload();
        MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap();
        headerMap.add(AUTHORIZATION, "Basic " + getParam(clientId, clientSecret));
        headerMap.add("verification_type", "service_token");
        String authUrl = getV2UserUrl(mobileNumber);
        payload.setTarget(authUrl);
        payload.setHeaders(headerMap);
        payload.setHttpMethod(HttpMethod.GET);
        return payload;
    }

    private static String getV2UserUrl(String mobileNumber) {
        StringBuilder queryParam = new StringBuilder("?");
        queryParam.append("fetch_strategy=userId").append('&');
        queryParam.append("phone=").append(mobileNumber);
        return getBaseUrl().append(queryParam).toString();
    }

    private static StringBuilder getBaseUrl() {
        StringBuilder targetUrl = new StringBuilder(ConfigurationUtil.getProperty(OAUTH_INTRA_BASE_URL));
        targetUrl.append("/v2/user");
        return targetUrl;
    }

    private static String getParam(String clientId, String secretKey) {
        return new String(Base64.getEncoder().encode((clientId + ":" + secretKey).getBytes()));
    }

    public boolean checkIfPaytmUPIOnboarded(List<UpiBankAccountV4> bankAccounts) {
        boolean paytmUPIExist = false;
        for (UpiBankAccountV4 bankAccount : bankAccounts) {
            if ("Y".equalsIgnoreCase(bankAccount.getMpinSet())) {
                paytmUPIExist = true;
                break;
            }
        }
        return paytmUPIExist;
    }

    @Deprecated
    public CheckUPIAccountResponse validateAndProcessUpiResponse(
            GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse) {

        if (fetchUpiProfileResponse != null && fetchUpiProfileResponse.isSuccessfullyProcessed()
                && fetchUpiProfileResponse.getResponse() != null
                && SUCCESS.equalsIgnoreCase(fetchUpiProfileResponse.getResponse().getStatus())) {
            UserProfileSarvatraV4 response = fetchUpiProfileResponse.getResponse();
            if (response.getRespDetails() != null && response.getRespDetails().getProfileDetail() != null
                    && CollectionUtils.isNotEmpty(response.getRespDetails().getProfileDetail().getVpaDetails())
                    && CollectionUtils.isNotEmpty(response.getRespDetails().getProfileDetail().getBankAccounts())) {
                if (checkIfPaytmUPIOnboarded(response.getRespDetails().getProfileDetail().getBankAccounts())) {
                    return prepareSuccessResponse();
                }
            }
        }
        return prepareFailureResponse();
    }

    @Deprecated
    public CheckUPIAccountResponse prepareFailureResponse() {
        CheckUPIAccountResponse response = new CheckUPIAccountResponse();
        ResponseHeader head = new ResponseHeader();
        CheckUPIAccountResponseBody responseBody = new CheckUPIAccountResponseBody();
        responseBody.setUpiAccountExist(false);
        responseBody.setResultInfo(new ResultInfo("F", "0001", "Failure"));
        response.setHead(head);
        response.setBody(responseBody);
        return response;
    }

    @Deprecated
    private CheckUPIAccountResponse prepareSuccessResponse() {
        CheckUPIAccountResponse response = new CheckUPIAccountResponse();
        ResponseHeader head = new ResponseHeader();
        CheckUPIAccountResponseBody responseBody = new CheckUPIAccountResponseBody();
        responseBody.setUpiAccountExist(true);
        response.setHead(head);
        response.setBody(responseBody);
        return response;
    }

}
