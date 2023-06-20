package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.cache.model.PspSchemaInfo;
import com.paytm.pgplus.cache.model.PspSchemaResponseBody;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.mappingserviceclient.service.IPspSchemaService;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsRequest;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsResponse;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsResponseBody;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class FetchPspAppsRequestProcessorTest {

    @InjectMocks
    FetchPspAppsRequestProcessor fetchPspAppsRequestProcessor;

    @Mock
    IPspSchemaService pspSchemaService;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Test
    public void testOnProcessWhenPspListNull() throws Exception {
        Mockito.when(pspSchemaService.getPspSchemas()).thenReturn(null);
        Assert.assertNull(fetchPspAppsRequestProcessor.onProcess(new FetchPspAppsRequest(), new FetchPspAppsRequest()));
    }

    @Test
    public void testOnProcessWhenPspListNotNull() throws Exception {
        PspSchemaResponseBody pspSchemeResponseBody = new PspSchemaResponseBody();
        pspSchemeResponseBody.setPspSchemas(new ArrayList<PspSchemaInfo>());
        Mockito.when(pspSchemaService.getPspSchemas()).thenReturn(pspSchemeResponseBody);
        Assert.assertNotNull(fetchPspAppsRequestProcessor.onProcess(new FetchPspAppsRequest(),
                new FetchPspAppsRequest()));
    }

    @Test
    public void testPostProcessWhenResponseNull() throws Exception {
        FetchPspAppsResponse response = fetchPspAppsRequestProcessor.postProcess(new FetchPspAppsRequest(),
                new FetchPspAppsRequest(), null);
        Assert.assertNotNull(response.getBody().getResultInfo());
    }

    @Test
    public void testPostProcessWhenResponseNotNull() throws Exception {
        FetchPspAppsResponse response = new FetchPspAppsResponse();
        response.setBody(new FetchPspAppsResponseBody());
        response = fetchPspAppsRequestProcessor.postProcess(new FetchPspAppsRequest(), new FetchPspAppsRequest(),
                response);
        ResultInfo resultInfo = response.getBody().getResultInfo();
        Assert.assertEquals(resultInfo.getResultStatus(), ResultCode.SUCCESS.getResultStatus());
    }

    @Test(expected = RequestValidationException.class)
    public void testPreProcessInvalidToken() throws Exception {
        FetchPspAppsRequest request = new FetchPspAppsRequest();
        request.setHeader(new TokenRequestHeader());
        request.getHead().setTokenType(TokenType.GUEST);
        fetchPspAppsRequestProcessor.preProcess(request);
    }

    @Test(expected = SessionExpiredException.class)
    public void testPreProcessInvalidTxnToken() throws Exception {
        FetchPspAppsRequest request = new FetchPspAppsRequest();
        request.setHeader(new TokenRequestHeader());
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        request.getHead().setToken("dummy");
        Mockito.when(nativeSessionUtil.validate("dummy")).thenThrow(SessionExpiredException.class);
        fetchPspAppsRequestProcessor.preProcess(request);
    }

    @Test
    public void testPreProcessValidTxnToken() throws Exception {
        FetchPspAppsRequest request = new FetchPspAppsRequest();
        request.setHeader(new TokenRequestHeader());
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        request.getHead().setToken("dummy");
        Mockito.when(nativeSessionUtil.validate("dummy")).thenReturn(new NativeInitiateRequest());

        Assert.assertNotNull(fetchPspAppsRequestProcessor.preProcess(request));
    }

}