/**
 * Alipay.com Inc. * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.OAuthHelper;
import com.paytm.pgplus.theia.nativ.model.user.UserPayModeStatusRequest;
import com.paytm.pgplus.theia.nativ.model.user.UserPayModeStatusResponse;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.collections.CollectionUtils;

/**
 * * @author Kushwanth Reddy * @version $Id:
 * FetchUserPaymodeStatusProcessorTest.java, v 0.1 2022-02-28 2:27 PM
 * {YOUR_NAME} Exp $$
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchUserPaymodeStatusProcessorTest {

    @InjectMocks
    private FetchUserPaymodeStatusRequestProcessor fetchUserPaymodeStatusProcessor;

    @Mock
    private INativeValidationService nativeValidationService;

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    @Mock
    private AccessTokenUtils accessTokenUtils;

    @Mock
    private OAuthHelper oauthHelper;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUserPaymodeStatusProcessorTest.class);

    @Test
    public void preProcess_MandatoryParams_FAILURE() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"ACCESS\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);

        // paymodes null
        request.getBody().setPaymentMode(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // mid null
        request.getBody().setMid(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // Token type null
        request.getHead().setTokenType(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // Token null
        request.getHead().setToken(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // Body param null
        request.setBody(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // Head param null
        request.setHead(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

        // request null
        request = null;
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }

    }

    @Test
    public void preProcess_with_ACCESS_TOKEN_SUCCESS() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"ACCESS\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        Mockito.when(
                accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                        request.getHead().getToken())).thenReturn(null);
        UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        Assert.assertEquals(request, serviceRequest);
    }

    @Test
    public void preProcess_with_ACCESS_TOKEN_FAILURE() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"ACCESS\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        Mockito.when(
                accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                        request.getHead().getToken())).thenReturn(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }
    }

    @Test
    public void preProcess_with_CHECKSUM_TOKEN_SUCCESS() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        Mockito.when(
                tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                        request.getBody(), request.getBody().getMid())).thenReturn(true);
        UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        Assert.assertEquals(request, serviceRequest);
    }

    @Test
    public void preProcess_with_SSO_TOKEN_SUCCESS() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"SSO\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        String ssoToken = request.getHead().getToken();
        String mid = request.getBody().getMid();
        UserDetailsBiz userDetails = new UserDetailsBiz();
        userDetails.setUserId("123456");

        Mockito.when(nativeValidationService.validateSSOToken(ssoToken, mid)).thenReturn(userDetails);
        UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        Assert.assertEquals(serviceRequest.getBody().getUserId(), userDetails.getUserId());
    }

    @Test
    public void preProcess_with_SSO_TOKEN_FAILURE() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"SSO\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        String ssoToken = request.getHead().getToken();
        String mid = request.getBody().getMid();

        Mockito.when(nativeValidationService.validateSSOToken(ssoToken, mid)).thenReturn(null);
        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.INVALID_SSO_TOKEN).getResultMsg(),
                    e.getMessage());
        }
    }

    @Test
    public void preProcess_with_INVAILD_TOKEN_FAILURE() throws Exception {
        String data = "{\"head\":{\"tokenType\":\"JWT\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);

        try {
            UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION).getResultMsg(),
                    e.getMessage());
        }
    }

    @Test
    public void preProcess_Validate_MobileNO() throws Exception {
        // valid mobileNo
        String data = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        Mockito.when(
                tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                        request.getBody(), request.getBody().getMid())).thenReturn(true);

        UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        Assert.assertEquals(serviceRequest, request);

        // inVaild mobileNo
        request.getBody().setMobileNo("123456789");
        Mockito.when(
                tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                        request.getBody(), request.getBody().getMid())).thenReturn(true);
        try {
            serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .getResultMsg(), e.getMessage());
        }
    }

    @Test
    public void preProcess_Validate_UserId() throws Exception {
        // valid UserId
        String data = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"userId\":\"123456\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(data, UserPayModeStatusRequest.class);
        Mockito.when(
                tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                        request.getBody(), request.getBody().getMid())).thenReturn(true);

        UserPayModeStatusRequest serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        Assert.assertEquals(serviceRequest, request);

        // inVaild UserId
        request.getBody().setUserId("12345@=+?6");
        Mockito.when(
                tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                        request.getBody(), request.getBody().getMid())).thenReturn(true);
        try {
            serviceRequest = fetchUserPaymodeStatusProcessor.preProcess(request);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .getResultMsg(), e.getMessage());
        }

    }

    @Test
    public void onProcess_SUCCESS() throws Exception {
        String requestData = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(requestData, UserPayModeStatusRequest.class);
        UserPayModeStatusRequest serviceRequest = request;
        String userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\",\"POSTPAID_STATUS\":\"LIVE\",\"CREDIT_CARD\":\"true\"},\"userTypes\":[\"BANK_CUSTOMER\",\"POSTPAID_USER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        UserDetailsV2 userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);

        Mockito.when(merchantPreferenceService.isUserPostpaidStatusCheckEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(userDetails);

        UserPayModeStatusResponse serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request, serviceRequest);
        Assert.assertNotNull(serviceResponse);
        Assert.assertEquals(CollectionUtils.hasElements(serviceResponse.getBody().getPaymentModeStatus()), true);

        // isCredit false
        userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\",\"POSTPAID_STATUS\":\"LIVE\"},\"userTypes\":[\"BANK_CUSTOMER\",\"POSTPAID_USER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);
        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(userDetails);

        serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request, serviceRequest);
        Assert.assertNotNull(serviceResponse);
        Assert.assertEquals(CollectionUtils.hasElements(serviceResponse.getBody().getPaymentModeStatus()), true);

        // inActivePostpaidUser
        userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\"},\"userTypes\":[\"BANK_CUSTOMER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);
        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(userDetails);
        serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request, serviceRequest);
        Assert.assertNotNull(serviceResponse);
        Assert.assertEquals(CollectionUtils.hasElements(serviceResponse.getBody().getPaymentModeStatus()), true);
    }

    @Test
    public void onProcess_FAILURE() throws Exception {
        String requestData = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(requestData, UserPayModeStatusRequest.class);
        UserPayModeStatusRequest serviceRequest = request;
        String userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\",\"POSTPAID_STATUS\":\"LIVE\",\"CREDIT_CARD\":\"true\"},\"userTypes\":[\"BANK_CUSTOMER\",\"POSTPAID_USER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        UserDetailsV2 userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);

        Mockito.when(merchantPreferenceService.isUserPostpaidStatusCheckEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(userDetails);

        UserPayModeStatusResponse serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request, serviceRequest);
        Assert.assertNotNull(serviceResponse);
        Assert.assertEquals(
                serviceResponse.getBody().getPaymentModeStatus().contains(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod()),
                false);
    }

    @Test
    public void onProcess_MISSING_MANDATORY_ELEMENT_FAILURE() throws Exception {
        String requestData = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(requestData, UserPayModeStatusRequest.class);
        UserPayModeStatusRequest serviceRequest = request;
        String userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\",\"POSTPAID_STATUS\":\"LIVE\",\"CREDIT_CARD\":\"true\"},\"userTypes\":[\"BANK_CUSTOMER\",\"POSTPAID_USER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        UserDetailsV2 userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);

        Mockito.when(merchantPreferenceService.isUserPostpaidStatusCheckEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString())).thenThrow(
                PaymentRequestProcessingException.getException(ResultCode.MISSING_MANDATORY_ELEMENT));

        try {
            UserPayModeStatusResponse serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request,
                    serviceRequest);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT).getResultMsg(),
                    e.getMessage());
        }
    }

    @Test
    public void onProcess_REQUEST_PARAMS_VALIDATION_EXCEPTION_FAILURE() throws Exception {
        String requestData = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"db3d0e15112e42668f08d8b5eecb584b1646031442480\"},\"body\":{\"mid\":\"k1G7l346739004133410\",\"mobileNo\":\"8006006993\",\"referenceId\":\"ref_987654321\",\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]}}'";
        UserPayModeStatusRequest request = JsonMapper.mapJsonToObject(requestData, UserPayModeStatusRequest.class);
        UserPayModeStatusRequest serviceRequest = request;
        String userDetailsData = "{\"basicInfo\":{\"countryCode\":\"91\",\"displayName\":\"Vidhi Gupta\",\"email\":\"vidhi.gupta@paytm.com\",\"firstName\":\"PRATEEK SRIVASTAVA\",\"lastName\":\"\",\"phone\":\"8006006993\"},\"userAttributeInfo\":{\"DEVICE_PASSCODE_VERIFIED\":\"73cc92b18960f5b0\",\"POSTPAID_STATUS\":\"LIVE\",\"CREDIT_CARD\":\"true\"},\"userTypes\":[\"BANK_CUSTOMER\",\"POSTPAID_USER\",\"PPB_CUSTOMER\",\"MERCHANT\"]}";
        UserDetailsV2 userDetails = JsonMapper.mapJsonToObject(userDetailsData, UserDetailsV2.class);

        Mockito.when(merchantPreferenceService.isUserPostpaidStatusCheckEnabled(Mockito.anyString())).thenReturn(true);

        Mockito.when(oauthHelper.fetchUserDetailsViaPhoneOrUserId(Mockito.anyString(), Mockito.anyString())).thenThrow(
                RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION));
        try {
            UserPayModeStatusResponse serviceResponse = fetchUserPaymodeStatusProcessor.onProcess(request,
                    serviceRequest);
        } catch (Exception e) {
            Assert.assertEquals(OfflinePaymentUtils.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .getResultMsg(), e.getMessage());
        }
    }

    @Test
    public void postProcess_SUCCESS() throws Exception {
        UserPayModeStatusRequest request = null;

        String responseData = "{\"body\":{\"extraParamsMap\":null,\"resultInfo\":{\"resultStatus\":\"S\",\"resultCode\":\"0000\",\"resultMsg\":\"Success\"},\"paymentModeStatus\":[{\"paymentMode\":\"PAYTM_DIGITAL_CREDIT\",\"status\":\"INACTIVE\"}]}}";
        UserPayModeStatusResponse serviceResponce = JsonMapper.mapJsonToObject(responseData,
                UserPayModeStatusResponse.class);
        UserPayModeStatusResponse response = fetchUserPaymodeStatusProcessor
                .postProcess(request, null, serviceResponce);
        Assert.assertNotNull(response);
        Assert.assertEquals(CollectionUtils.hasElements(response.getBody().getPaymentModeStatus()), true);
        Assert.assertEquals(response.getBody().getResultInfo().getResultStatus(), "S");
    }

}