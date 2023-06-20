package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.risk.models.request.RiskVerifierDoVerifyRequestBody;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoVerifyResponse;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoVerifyResponseBody;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponse;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponseBody;
import com.paytm.pgplus.facade.risk.services.IRiskVerify;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequest;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequestBody;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequest;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequestBody;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponseBody;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import io.swagger.annotations.Api;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RiskVerifierServiceImplTest {

    @InjectMocks
    private RiskVerifierServiceImpl riskVerifierService;

    @Mock
    private IRiskVerify riskVerificationService;

    @Mock
    private INativeValidationService nativeValidationService;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private RiskVerificationUtil riskVerificationUtil;

    @Rule
    public ExpectedException expectedException;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void doView() throws FacadeCheckedException {
        doNothing().when(nativeValidationService).validateRiskDoViewRequest(any());
        RiskVerifierDoViewResponseBody riskVerifierDoViewResponseBody = Mockito
                .mock(RiskVerifierDoViewResponseBody.class);
        when(riskVerifierDoViewResponseBody.getVerifyId()).thenReturn("vID").thenReturn("vID");
        DoViewRequest request = Mockito.mock(DoViewRequest.class);
        DoViewRequestBody requestBody = mock(DoViewRequestBody.class);
        TokenRequestHeader header = new TokenRequestHeader();
        header.setToken("token");
        when(request.getHead()).thenReturn(header).thenReturn(header);
        when(nativeSessionUtil.getRiskVerificationData(request.getHead().getToken())).thenReturn(
                new RiskVerifierPayload("acId", "mid", "orderid", riskVerifierDoViewResponseBody)).thenReturn(
                new RiskVerifierPayload("acId", "mid", "orderid", riskVerifierDoViewResponseBody));
        when(request.getBody()).thenReturn(requestBody).thenReturn(requestBody);
        when(requestBody.getMethod()).thenReturn("method").thenReturn("method");
        RiskVerifierDoViewResponse response = mock(RiskVerifierDoViewResponse.class);
        when(response.getBody()).thenReturn(riskVerifierDoViewResponseBody).thenReturn(riskVerifierDoViewResponseBody);
        ResultInfo resultInfo = Mockito.mock(ResultInfo.class);
        when(riskVerifierDoViewResponseBody.getResultInfo()).thenReturn(resultInfo).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn(com.paytm.pgplus.common.enums.ResultCode.SUCCESS.getCode())
                .thenThrow(Exception.class).thenReturn(com.paytm.pgplus.common.enums.ResultCode.SUCCESS.getCode());
        when(riskVerifierDoViewResponseBody.getMethod()).thenReturn("method").thenReturn("method");
        when(riskVerifierDoViewResponseBody.getRenderData()).thenReturn(new HashMap<>()).thenReturn(new HashMap<>());
        when(response.getBody()).thenReturn(riskVerifierDoViewResponseBody).thenReturn(riskVerifierDoViewResponseBody);
        when(riskVerificationService.doView(any())).thenReturn(response).thenReturn(response);
        AlipayExternalRequestHeader alipayExternalRequestHeader = mock(AlipayExternalRequestHeader.class);
        new MockUp<RequestHeaderGenerator>() {

            @mockit.Mock
            public AlipayExternalRequestHeader getHeader(ApiFunctions apiFunctions) {
                return alipayExternalRequestHeader;
            }
        };
        riskVerifierService.doView(request);
        verify(nativeSessionUtil, times(1)).getRiskVerificationData(request.getHead().getToken());
        riskVerifierService.doView(request);
    }

    @Test
    public void doVerify() throws FacadeCheckedException {

        DoVerifyRequest request = Mockito.mock(DoVerifyRequest.class);
        TokenRequestHeader tokenRequestHeaderheader = new TokenRequestHeader();
        tokenRequestHeaderheader.setToken("token");
        when(request.getHead()).thenReturn(tokenRequestHeaderheader).thenReturn(tokenRequestHeaderheader);
        doNothing().when(nativeValidationService).validateRiskDoViewRequest(any());
        RiskVerifierDoViewResponseBody riskVerifierDoViewResponseBody = Mockito
                .mock(RiskVerifierDoViewResponseBody.class);
        when(riskVerifierDoViewResponseBody.getVerifyId()).thenReturn("vID").thenReturn("vID").thenReturn("vId")
                .thenReturn("vId");
        AlipayExternalRequestHeader header = Mockito.mock(AlipayExternalRequestHeader.class);
        new MockUp<RequestHeaderGenerator>() {
            @mockit.Mock
            public AlipayExternalRequestHeader getHeader(ApiFunctions function) {
                return header;
            }
        };
        RiskVerifierPayload payload = new RiskVerifierPayload("acId", "mid", "order", riskVerifierDoViewResponseBody);
        when(nativeSessionUtil.getRiskVerificationData(any())).thenReturn(payload).thenReturn(payload);
        DoVerifyRequestBody body = Mockito.mock(DoVerifyRequestBody.class);
        when(request.getBody()).thenReturn(body).thenReturn(body);
        when(body.getMethod()).thenReturn("method").thenReturn("method");
        when(body.getValidateData()).thenReturn("validated").thenReturn("validated");
        RiskVerifierDoVerifyResponse response = Mockito.mock(RiskVerifierDoVerifyResponse.class);
        RiskVerifierDoVerifyResponseBody responseBody = Mockito.mock(RiskVerifierDoVerifyResponseBody.class);
        ResultInfo resultInfo = Mockito.mock(ResultInfo.class);
        when(response.getBody()).thenReturn(responseBody).thenReturn(responseBody);
        when(responseBody.getResultInfo()).thenReturn(resultInfo).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn(com.paytm.pgplus.common.enums.ResultCode.SUCCESS.getCode())
                .thenReturn(com.paytm.pgplus.common.enums.ResultCode.SECURITY_VERIFY_FAIL.getCode());
        when(responseBody.getIsFinish()).thenReturn("true");
        doNothing().when(nativeSessionUtil).setKey(anyString(), anyObject(), anyLong());
        when(riskVerificationService.doVerify(any())).thenReturn(response).thenReturn(response);
        when(responseBody.getIsFinish()).thenReturn("fisnisehd");
        when(responseBody.getNextMethod()).thenReturn("nmethod");
        when(responseBody.getRenderData()).thenReturn(new HashMap<>());
        when(responseBody.getCanRetry()).thenReturn("yes");
        assertNotNull(riskVerifierService.doVerify(request));
        assertNotNull(riskVerifierService.doVerify(request));

    }

    @Test
    public void testdoVerifyErrorResponse() {
        new MockUp<RequestHeaderGenerator>() {

            @mockit.Mock
            public AlipayExternalRequestHeader getHeader(ApiFunctions function) throws Exception {
                throw new Exception();
            }

        };
        doNothing().when(nativeValidationService).validateRiskDoViewRequest(any());
        DoVerifyRequest request = mock(DoVerifyRequest.class);
        TokenRequestHeader header = new TokenRequestHeader();
        header.setTxnToken("token");
        when(request.getHead()).thenReturn(header);
        DoVerifyRequestBody requestBody = mock(DoVerifyRequestBody.class);
        when(request.getBody()).thenReturn(requestBody).thenReturn(requestBody);
        when(requestBody.getMethod()).thenReturn("method").thenReturn("method");
        riskVerifierService.doVerify(request);
    }
}