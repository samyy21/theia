package com.paytm.pgplus.theia.services.upiAccount.helper;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsAndAccountCheckRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.UpiAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsAndAccountCheckResponseBody;
import com.paytm.pgplus.theia.models.upiAccount.response.UpiAccountResponse;
import com.paytm.pgplus.theia.nativ.model.common.UpiPspOptions;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theiacommon.exception.BaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.UPI_PSP_ICON_BASE_PATH;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.UPI_PSP_NAME;

@Service
public class UpiIntentServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpiIntentServiceHelper.class);

    @Autowired
    CheckUPIAccountServiceHelper accountServiceHelper;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    public List<UpiPspOptions> fetchUpiOptions() throws BaseException {

        String pspIconBaseUrl = PaytmTLD.getStaticUrlPrefix();
        String upiPspAppNames = ConfigurationUtil.getProperty(UPI_PSP_NAME);
        LOGGER.info("psp Icon Base Url ->{} and upiPspAppNames ->{} for fetchUpiOptionsRequest ", pspIconBaseUrl,
                upiPspAppNames);
        if (StringUtils.isNotBlank(pspIconBaseUrl) && StringUtils.isNotBlank(upiPspAppNames)) {
            List<UpiPspOptions> upiPspOptionsList = new ArrayList<>();
            String[] upiPspNames = upiPspAppNames.split(",");
            Arrays.stream(upiPspNames).forEach(
                    s -> upiPspOptionsList.add(new UpiPspOptions(s, pspIconBaseUrl.concat(UPI_PSP_ICON_BASE_PATH)
                            .concat(s).concat(".png"))));
            return upiPspOptionsList;
        }
        throw BaseException.getException("PspOptions configuration error!");
    }

    public boolean checkIfUPIAccountExistForUser(UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request) {

        // getUserId from oauth team
        String userIdOAuth = getUserIdFromOAuth(request.getBody().getMobileNumber());

        if (StringUtils.isBlank(userIdOAuth)) {
            LOGGER.debug("Unable to retrieve userId for this mobile number");
            return false;
        }
        GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse = null;

        fetchUpiProfileResponse = fetchUserProfileVpaV4(request, userIdOAuth);

        return validateAndProcessUpiResponse(fetchUpiProfileResponse);
    }

    private String getUserIdFromOAuth(String mobileNumber) {

        HttpRequestPayload<String> payload = accountServiceHelper.generatePayloadForV2User(mobileNumber);
        Response response = null;
        try {
            response = JerseyHttpClient.sendHttpGetRequest(payload);
        } catch (Exception e) {
            LOGGER.error("Failed to get custId from OAuth");
            return null;
        }
        return accountServiceHelper.validateAndProcessSuccessResponse(response);
    }

    private GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserProfileVpaV4(
            UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request, String userId) {

        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest();
        if (StringUtils.isNotBlank(userId) && request.getBody() != null) {
            fetchUserPaytmVpaRequest.setUserId(userId);
            fetchUserPaytmVpaRequest.setRequestId(RequestIdGenerator.generateRequestId());
            fetchUserPaytmVpaRequest.setDeviceId(request.getBody().getDeviceId());
        }
        return sarvatraVpaDetails.fetchUserProfileVpaV4(fetchUserPaytmVpaRequest);
    }

    private boolean validateAndProcessUpiResponse(GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse) {

        if (fetchUpiProfileResponse != null && fetchUpiProfileResponse.isSuccessfullyProcessed()
                && fetchUpiProfileResponse.getResponse() != null
                && SUCCESS.equalsIgnoreCase(fetchUpiProfileResponse.getResponse().getStatus())) {
            UserProfileSarvatraV4 response = fetchUpiProfileResponse.getResponse();
            if (response.getRespDetails() != null && response.getRespDetails().getProfileDetail() != null
                    && CollectionUtils.isNotEmpty(response.getRespDetails().getProfileDetail().getVpaDetails())
                    && CollectionUtils.isNotEmpty(response.getRespDetails().getProfileDetail().getBankAccounts())) {
                if (accountServiceHelper.checkIfPaytmUPIOnboarded(response.getRespDetails().getProfileDetail()
                        .getBankAccounts())) {
                    return true;
                }
            }
        }
        return false;
    }

    public UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> prepareFailureResponse(boolean accountExists) {
        UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> response = new UpiAccountResponse<>();
        ResponseHeader head = new ResponseHeader();
        FetchUpiOptionsAndAccountCheckResponseBody responseBody = new FetchUpiOptionsAndAccountCheckResponseBody();
        responseBody.setAccountExists(accountExists);
        responseBody.setResultInfo(new ResultInfo("F", "0001", "Failure"));
        response.setHead(head);
        response.setBody(responseBody);
        return response;
    }

    public UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> prepareSuccessResponse(boolean accountExists,
            List<UpiPspOptions> upiPspOptions) {
        UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> response = new UpiAccountResponse<>();
        ResponseHeader head = new ResponseHeader();
        FetchUpiOptionsAndAccountCheckResponseBody responseBody = new FetchUpiOptionsAndAccountCheckResponseBody();
        responseBody.setAccountExists(accountExists);
        responseBody.setUpiPspOptions(upiPspOptions);
        response.setHead(head);
        response.setBody(responseBody);
        return response;
    }

}
