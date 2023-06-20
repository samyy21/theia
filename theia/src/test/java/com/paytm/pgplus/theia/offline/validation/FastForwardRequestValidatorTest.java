package com.paytm.pgplus.theia.offline.validation;

import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequest;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class FastForwardRequestValidatorTest extends AOAUtilsTest {

    @InjectMocks
    FastForwardRequestValidator fastForwardRequestValidator;
    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Test
    public void testValidateWhenHeadNUll() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        forwardRequest.setHead(null);
        Assert.assertEquals("InvalidRequestHeader", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenVersionBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        head.setVersion("");
        forwardRequest.setHead(head);
        Assert.assertEquals("InvalidVersion", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenRequestTimestampBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();

        head.setVersion("abc");
        head.setRequestTimestamp("");

        forwardRequest.setHead(head);
        Assert.assertEquals("InvalidTimeStamp", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenClientIdBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();

        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("");
        forwardRequest.setHead(head);
        Assert.assertEquals("InvalidClientId", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenTokenBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();

        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("");
        forwardRequest.setHead(head);
        Assert.assertEquals("InvalidToken", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenTokenNull() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();

        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(null);
        forwardRequest.setHead(head);

        Assert.assertEquals("InvalidTokenType", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenBodyNull() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();

        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        forwardRequest.setHead(head);
        forwardRequest.setBody(null);
        Assert.assertEquals("InvalidRequest", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenReqTypeBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        forwardRequest.setHead(head);
        fastForwardRequestBody.setReqType("");
        forwardRequest.setBody(fastForwardRequestBody);
        Assert.assertEquals("InvalidRequestType", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenMidBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        head.setMid("");
        forwardRequest.setHead(head);
        fastForwardRequestBody.setReqType("CLW_APP_PAY");
        forwardRequest.setBody(fastForwardRequestBody);
        Assert.assertEquals("InvalidMID", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenInvalidTxnAmount() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        head.setMid("test");
        forwardRequest.setHead(head);
        fastForwardRequestBody.setReqType("CLW_APP_PAY");
        fastForwardRequestBody.setTxnAmount("");
        forwardRequest.setBody(fastForwardRequestBody);
        Assert.assertEquals("InvalidTxnAmount", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenCustIdBlank() {
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        head.setMid("test");
        forwardRequest.setHead(head);
        fastForwardRequestBody.setReqType("CLW_APP_PAY");
        fastForwardRequestBody.setTxnAmount("123");
        fastForwardRequestBody.setCustomerId("");
        forwardRequest.setBody(fastForwardRequestBody);
        Assert.assertEquals("InvalidCustomerID", fastForwardRequestValidator.validate(forwardRequest));
    }

    @Test
    public void testValidateWhenMerchantBlocked() {
        when(merchantExtendInfoUtils.isMerchantActiveOrBlocked(anyString())).thenReturn(true);
        FastForwardRequest forwardRequest = new FastForwardRequest();
        RequestHeader head = new RequestHeader();
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        head.setVersion("test");
        head.setRequestTimestamp("test");
        head.setClientId("test");
        head.setToken("test");
        head.setTokenType(TokenType.SSO);
        head.setMid("test");
        forwardRequest.setHead(head);
        fastForwardRequestBody.setReqType("CLW_APP_PAY");
        fastForwardRequestBody.setTxnAmount("123");
        fastForwardRequestBody.setCustomerId("test");
        forwardRequest.setBody(fastForwardRequestBody);
        Assert.assertEquals("MERCHANT_BLOCKED", fastForwardRequestValidator.validate(forwardRequest));
    }

}