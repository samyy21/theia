package com.paytm.pgplus.theia.services.upiAccount.helper;

import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequestHeader;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.InjectMocks;

public class FetchUpiOptionsServiceHelperTest {

    @InjectMocks
    private FetchUpiOptionsServiceHelper fetchUpiOptionsServiceHelper = new FetchUpiOptionsServiceHelper();

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUpiOptionsServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidateRequest() {
        FetchUpiOptionsRequest request = new FetchUpiOptionsRequest();
        FetchUpiOptionsRequestHeader head = new FetchUpiOptionsRequestHeader();
        FetchUpiOptionsRequestBody body = new FetchUpiOptionsRequestBody();
        head.setTokenType(TokenType.CHECKSUM);
        head.setToken("token");
        body.setMid("mid");
        request.setBody(body);
        request.setHead(head);
        fetchUpiOptionsServiceHelper.validateRequest(request);

        try {
            request.getHead().setTokenType(TokenType.SSO);
            fetchUpiOptionsServiceHelper.validateRequest(request);
        } catch (BaseException e) {
        }

        exceptionRule.expect(BaseException.class);
        request.getBody().setMid(null);
        fetchUpiOptionsServiceHelper.validateRequest(request);
    }

    @Test
    public void testFetchUpiOptions() {
        FetchUpiOptionsRequest request = new FetchUpiOptionsRequest();
        fetchUpiOptionsServiceHelper.fetchUpiOptions(request);

        new MockUp<PaytmTLD>() {
            @mockit.Mock
            public String getStaticUrlPrefix() {
                return "pspIconBaseUrl";
            }
        };
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return "upiPspAppNames";
            }
        };
        fetchUpiOptionsServiceHelper.fetchUpiOptions(request);
    }
}