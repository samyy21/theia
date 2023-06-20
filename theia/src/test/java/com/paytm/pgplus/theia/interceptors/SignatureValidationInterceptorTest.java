package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.JWTValidationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import mockit.MockUp;
import org.json.JSONException;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignatureValidationInterceptorTest {

    @InjectMocks
    private SignatureValidationInterceptor signatureValidationInterceptor = new SignatureValidationInterceptor();

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private JWTValidationUtil jwtValidationUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPreHandle() throws Exception {
        HttpServletRequest request = mock(MultiReadHttpServletRequestWrapper.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS);
        signatureValidationInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"tokenType\":\"JWT\"}}");
        when(jwtValidationUtil.validateJWT(any())).thenReturn(true);
        signatureValidationInterceptor.preHandle(request, response, "handler");

        exceptionRule.expect(RequestValidationException.class);
        when(jwtValidationUtil.validateJWT(any())).thenReturn(false);
        signatureValidationInterceptor.preHandle(request, response, "handler");
    }

    @Test
    public void testPreHandle1() throws Exception {
        HttpServletRequest request = mock(MultiReadHttpServletRequestWrapper.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        new MockUp<ValidateChecksum>() {
            @mockit.Mock
            public ValidateChecksum getInstance() throws SecurityException {
                ValidateChecksum validateChecksum = mock(ValidateChecksum.class);
                when(validateChecksum.verifyCheckSum(any())).thenReturn(true);
                return validateChecksum;
            }
        };
        when(request.getMethod()).thenReturn("method");
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody())
                .thenReturn(
                        "{\"body\":{\"aggMid\":\"mid\"},\"head\":{\"clientId\":\"clientId\",\"signature\":\"merchantSignature\"}}");
        when(jwtValidationUtil.validateJWT(any())).thenReturn(true);
        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(any(), any())).thenReturn("merchantKey");
        signatureValidationInterceptor.preHandle(request, response, "handler");

        exceptionRule.expect(JSONException.class);
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn("{\"head\":{},\"body\":{}}");
        signatureValidationInterceptor.preHandle(request, response, "handler");
    }

    @Test
    public void testPreHandleExceptions() throws Exception {
        HttpServletRequest request = mock(MultiReadHttpServletRequestWrapper.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("method");
        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(false);
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"tokenType\":\"SSO\"}}");
        signatureValidationInterceptor.preHandle(request, response, "handler");

        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(true);
        try {
            when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                    "{\"body\":{},\"head\":{\"signature\":\"mchantSignature\"}}");
            signatureValidationInterceptor.preHandle(request, response, "handler");
        } catch (RequestValidationException e) {
        }

        try {
            when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn("{\"head\":{}}");
            signatureValidationInterceptor.preHandle(request, response, "handler");
        } catch (RuntimeException e) {
        }

        try {
            when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn("{\"body\":{}}");
            signatureValidationInterceptor.preHandle(request, response, "handler");
        } catch (RuntimeException e) {
        }

        try {
            when(merchantExtendInfoUtils.getMerchantKey(any(), any())).thenReturn("merchantKey");
            when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                    "{\"body\":{\"aggMid\":{}},\"head\":{\"signature\":\"mchantSignature\"}}");
            signatureValidationInterceptor.preHandle(request, response, "handler");
        } catch (RequestValidationException e) {
        }

        try {
            when(merchantExtendInfoUtils.getMerchantKey(any(), any())).thenReturn("merchantKey");
            when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                    "{\"body\":{\"requestType\":\"NATIVE_MF\"},\"head\":{\"signature\":\"mchantSignature\"}}");
            signatureValidationInterceptor.preHandle(request, response, "handler");
        } catch (RequestValidationException e) {
        }
    }

}