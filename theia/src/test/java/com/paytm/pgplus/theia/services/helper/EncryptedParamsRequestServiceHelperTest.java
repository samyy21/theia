package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.checksum.crypto.EncryptionFactory;
import com.paytm.pgplus.checksum.crypto.IEncryption;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import org.junit.Test;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.common.util.CommonConstants.ENC_DEC_KEY_SUFFIX;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptedParamsRequestServiceHelperTest {

    @InjectMocks
    private EncryptedParamsRequestServiceHelper encryptedParamsRequestServiceHelper = new EncryptedParamsRequestServiceHelper();

    @Mock
    Environment env;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Mock
    private AESMerchantService aesMerchantService;

    @Mock
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedParamsRequestServiceHelper.class);
    private static final String CHECKSUM_KEY = "checksum_key";
    private static final String ENCRYPT_KEY = "encrypt_key";

    private static Class<?> redisSessionValveClazz;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testWrapHttpRequestIfEncrypted() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = new MerchantPreferenceInfoResponse();
        merchantPreferenceInfoResponse.setMerchantId("merchantId");
        when(httpServletRequest.getParameter(any())).thenReturn("mid");
        exceptionRule.expect(Exception.class);
        when(merchantPreferenceProvider.isEncRequestEnabled((MerchantPreferenceStore) any())).thenReturn(true);
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);
    }

    @Test
    public void testWrapHttpRequestIfEncrypted1() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = new MerchantPreferenceInfoResponse();
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any(), any())).thenReturn("1=1|2=2");
                return encryption;
            }
        };
        merchantPreferenceInfoResponse.setMerchantId("merchantId");
        when(httpServletRequest.getParameter(any())).thenReturn("mid");
        when(merchantPreferenceProvider.isAES256EncRequestEnabled((MerchantPreferenceStore) any())).thenReturn(true);
        when(aesMerchantService.fetchAesEncDecKey(any())).thenReturn("merchantKey");
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any(), any())).thenReturn("1|2");
                return encryption;
            }
        };
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any(), any())).thenReturn(null);
                return encryption;
            }
        };
        exceptionRule.expect(Exception.class);
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);
    }

    @Test
    public void testWrapHttpRequestIfEncrypted2() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = new MerchantPreferenceInfoResponse();
        merchantPreferenceInfoResponse.setMerchantId("merchantId");
        when(httpServletRequest.getParameter(any())).thenReturn("mid");
        when(merchantPreferenceProvider.isAES256EncRequestEnabled((MerchantPreferenceStore) any())).thenReturn(true);
        when(aesMerchantService.fetchAesEncDecKey(any())).thenReturn("merchantKey");
        exceptionRule.expect(Exception.class);
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);
    }

    @Test
    public void testWrapHttpRequestIfEncrypted3() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = new MerchantPreferenceInfoResponse();
        merchantPreferenceInfoResponse.setMerchantId("merchantId");
        when(httpServletRequest.getParameter(any())).thenReturn("mid");
        when(merchantExtendInfoUtils.getMerchantKey(any(), any())).thenReturn("merchantKey");
        when(merchantPreferenceProvider.isEncRequestEnabled((MerchantPreferenceStore) any())).thenReturn(true);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("value");
                when(encryption.decrypt(any(), any())).thenReturn("1|2");
                return encryption;
            }
        };
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("value");
                when(encryption.decrypt(any(), any())).thenReturn("1=1|2=2");
                return encryption;
            }
        };
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn(null);
                when(encryption.decrypt(any(), any())).thenReturn(null);
                return encryption;
            }
        };
        exceptionRule.expect(Exception.class);
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);
    }

    @Test
    public void testWrapHttpRequestIfEncrypted4() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = new MerchantPreferenceInfoResponse();
        when(httpServletRequest.getParameter(any())).thenReturn("mid");
        when(merchantPreferenceService.getMerchantPreferenceStore(any())).thenReturn(new MerchantPreferenceStore());
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public boolean isRedisOPtimizedFlow() {
                return true;
            }
        };
        when(merchantPreferenceProvider.parseResponse(any())).thenReturn(new MerchantPreferenceStore());
        encryptedParamsRequestServiceHelper.wrapHttpRequestIfEncrypted(httpServletRequest, httpServletResponse,
                merchantPreferenceInfoResponse);
    }

}