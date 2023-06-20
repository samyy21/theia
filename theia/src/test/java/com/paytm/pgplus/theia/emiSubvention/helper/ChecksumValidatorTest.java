package com.paytm.pgplus.theia.emiSubvention.helper;

import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.junit.*;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChecksumValidatorTest {

    @InjectMocks
    private ChecksumValidator checksumValidator = new ChecksumValidator();

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumValidator.class);
    private static final Pattern bodyStartPatternForJson = Pattern.compile("\"body\"[ \n\r]*:[ \n\r]*\\{");
    private static final Pattern headStartPatternForJson = Pattern.compile("\"head\"[ \n\r]*:[ \n\r]*\\{");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidateChecksum() throws SecurityException {
        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(true);
        checksumValidator.validateChecksum("body", "mid", "");

        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(false);
        checksumValidator.validateChecksum("body", "mid", "signature");

        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn("merchantKey");
        checksumValidator.validateChecksum("{\"paymentDetails\":\"paymentDetails\"}", "mid", "signature");

        when(merchantPreferenceService.isChecksumEnabled(any())).thenReturn(true);
        exceptionRule.expect(RequestValidationException.class);
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn(null);
        checksumValidator.validateChecksum("body", "mid", "signature");
    }

    @Test
    public void testGetBodyString() throws IOException {
        HttpServletRequest httpServletRequest = mock(MultiReadHttpServletRequestWrapper.class);
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        exceptionRule.expect(RuntimeException.class);
        when(((MultiReadHttpServletRequestWrapper) httpServletRequest).getMessageBody()).thenReturn(
                String.valueOf(headStartPatternForJson));
        checksumValidator.getBodyString();
    }

    @Test
    public void testGetBodyString1() throws IOException {
        HttpServletRequest httpServletRequest = mock(MultiReadHttpServletRequestWrapper.class);
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        exceptionRule.expect(RuntimeException.class);
        when(((MultiReadHttpServletRequestWrapper) httpServletRequest).getMessageBody()).thenReturn("\"head\":{");
        checksumValidator.getBodyString();
    }

    @Test
    public void testGetBodyString2() throws IOException {
        HttpServletRequest httpServletRequest = mock(MultiReadHttpServletRequestWrapper.class);
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        when(((MultiReadHttpServletRequestWrapper) httpServletRequest).getMessageBody()).thenReturn(
                "\"body\":{},\"head\":{}");
        checksumValidator.getBodyString();

        exceptionRule.expect(StringIndexOutOfBoundsException.class);
        when(((MultiReadHttpServletRequestWrapper) httpServletRequest).getMessageBody()).thenReturn(
                "\"head\":{},\"body\":{}");
        checksumValidator.getBodyString();
    }
}