package com.paytm.pgplus.theia.services.upiAccount.helper;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetailsV4;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.UpiProfileDetailV4;
import com.paytm.pgplus.facade.user.models.VpaDetailV4;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequestHeader;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.InjectMocks;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import java.util.Collections;

public class CheckUPIAccountServiceHelperTest {

    @InjectMocks
    private CheckUPIAccountServiceHelper checkUPIAccountServiceHelper = new CheckUPIAccountServiceHelper();

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckUPIAccountServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidateRequest() {
        CheckUPIAccountRequest request = new CheckUPIAccountRequest();
        CheckUPIAccountRequestHeader head = new CheckUPIAccountRequestHeader();
        head.setTokenType(TokenType.CHECKSUM);
        head.setToken("token");
        CheckUPIAccountRequestBody body = new CheckUPIAccountRequestBody();
        body.setMobileNumber("9999999999");
        body.setMid("mid");
        request.setBody(body);
        request.setHead(head);
        when(merchantPreferenceService.isCheckUPIAccountSupported(any())).thenReturn(true);
        checkUPIAccountServiceHelper.validateRequest(request);

        try {
            request.getHead().setTokenType(TokenType.SSO);
            checkUPIAccountServiceHelper.validateRequest(request);
        } catch (BaseException e) {
        }

        try {
            when(merchantPreferenceService.isCheckUPIAccountSupported(any())).thenReturn(false);
            checkUPIAccountServiceHelper.validateRequest(request);
        } catch (BaseException e) {
        }

        exceptionRule.expect(BaseException.class);
        request.getBody().setMid(null);
        checkUPIAccountServiceHelper.validateRequest(request);
    }

    @Test
    public void testFetchUserProfileVpaV4() {
        CheckUPIAccountRequest request = new CheckUPIAccountRequest();
        CheckUPIAccountRequestBody body = new CheckUPIAccountRequestBody();
        body.setDeviceId("deviceId");
        request.setBody(body);
        CheckUPIAccountRequestHeader header = new CheckUPIAccountRequestHeader();
        header.setTokenType(TokenType.SSO);
        request.setHead(header);
        when(sarvatraVpaDetails.fetchUserProfileVpaV4(any())).thenReturn(null);
        checkUPIAccountServiceHelper.fetchUserProfileVpaV4(request, "");
        checkUPIAccountServiceHelper.fetchUserProfileVpaV4(request, "userId");
    }

    @Test
    public void testValidateAndProcessUpiResponse() {
        GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse = new GenericCoreResponseBean<UserProfileSarvatraV4>(
                new UserProfileSarvatraV4());
        checkUPIAccountServiceHelper.validateAndProcessUpiResponse(fetchUpiProfileResponse);

        fetchUpiProfileResponse.getResponse().setStatus(SUCCESS);
        checkUPIAccountServiceHelper.validateAndProcessUpiResponse(fetchUpiProfileResponse);

        PaytmVpaDetailsV4 paytmVpaDetailsV4 = new PaytmVpaDetailsV4();
        UpiProfileDetailV4 upiProfileDetailV4 = new UpiProfileDetailV4();
        upiProfileDetailV4.setVpaDetails(Collections.singletonList(new VpaDetailV4()));
        upiProfileDetailV4.setBankAccounts(Collections.singletonList(new UpiBankAccountV4()));
        paytmVpaDetailsV4.setProfileDetail(upiProfileDetailV4);
        fetchUpiProfileResponse.getResponse().setRespDetails(paytmVpaDetailsV4);
        checkUPIAccountServiceHelper.validateAndProcessUpiResponse(fetchUpiProfileResponse);

        UpiBankAccountV4 upiBankAccountV4 = new UpiBankAccountV4();
        upiBankAccountV4.setMpinSet("Y");
        upiProfileDetailV4.setBankAccounts(Collections.singletonList(upiBankAccountV4));
        checkUPIAccountServiceHelper.validateAndProcessUpiResponse(fetchUpiProfileResponse);
    }
}