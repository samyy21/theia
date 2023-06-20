package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.bankrequest.IValidateVpaService;
import com.paytm.pgplus.facade.bankrequest.model.ValidateVpaAndPspResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.NativeValidateVpaRequest;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequest;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequestBody;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class NativeValidateVpaRequestProcessorTest {

    @InjectMocks
    NativeValidateVpaRequestProcessor nativeValidateVpaRequestProcessor;

    @Mock
    IMerchantMappingService merchantMappingService;

    @Mock
    private IValidateVpaService validateVpaService;

    @Mock
    private LocaleFieldAspect localeFieldAspect;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Test
    public void testOnProcessRequestForNumeicId() throws Exception {

        VpaValidateRequest vpaValidateRequest = new VpaValidateRequest();
        String mid = "mPmaOY80417278861100";
        String numericId = "9709670803";
        String orderId = "myid123";
        VpaValidateRequestBody body = new VpaValidateRequestBody();
        body.setMid(mid);
        body.setNumericId(numericId);
        vpaValidateRequest.setBody(body);
        NativeValidateVpaRequest nativeValidateVpaRequest = new NativeValidateVpaRequest(mid, null, orderId, null,
                false, numericId);
        GenericCoreResponseBean<MappingMerchantData> response = Mockito.mock(GenericCoreResponseBean.class);
        MappingMerchantData mappinMerchantData = Mockito.mock(MappingMerchantData.class);
        Mockito.when(mappinMerchantData.getOfficialName()).thenReturn("tytyty");
        Mockito.when(response.getResponse()).thenReturn(mappinMerchantData);
        Mockito.when(response.isSuccessfullyProcessed()).thenReturn(true);
        Mockito.when(merchantMappingService.fetchMerchanData(mid)).thenReturn(response);

        ValidateVpaAndPspResponse validateVpaAndPspResponse = Mockito.mock(ValidateVpaAndPspResponse.class);
        Mockito.when(validateVpaAndPspResponse.getStatus()).thenReturn("FAILURE");
        Mockito.when(validateVpaService.fetchValidatedVpa(any())).thenReturn(validateVpaAndPspResponse);
        nativeValidateVpaRequestProcessor.onProcess(vpaValidateRequest, nativeValidateVpaRequest);

    }

    @Test
    public void testOnProcessRequestForVpa() throws Exception {

        VpaValidateRequest vpaValidateRequest = new VpaValidateRequest();

        String mid = "mPmaOY80417278861100";

        String vpa = "test9972746530@paytm";

        String orderId = "myid123";

        VpaValidateRequestBody body = new VpaValidateRequestBody();

        body.setVpa(vpa);

        body.setMid(mid);

        vpaValidateRequest.setBody(body);

        NativeValidateVpaRequest nativeValidateVpaRequest = new NativeValidateVpaRequest(mid, vpa, orderId, null,
                false, null);

        GenericCoreResponseBean<MappingMerchantData> response = Mockito.mock(GenericCoreResponseBean.class);

        MappingMerchantData mappinMerchantData = Mockito.mock(MappingMerchantData.class);

        Mockito.when(mappinMerchantData.getOfficialName()).thenReturn("tytyty");

        Mockito.when(response.getResponse()).thenReturn(mappinMerchantData);

        Mockito.when(response.isSuccessfullyProcessed()).thenReturn(true);

        Mockito.when(merchantMappingService

        .fetchMerchanData(mid)).thenReturn(response);

        ValidateVpaAndPspResponse validateVpaAndPspResponse = Mockito.mock(ValidateVpaAndPspResponse.class);

        Mockito.when(validateVpaAndPspResponse.getStatus()).thenReturn("FAILURE");

        Mockito.when(validateVpaService.fetchValidatedVpa(any())).thenReturn(validateVpaAndPspResponse);

        nativeValidateVpaRequestProcessor.onProcess(vpaValidateRequest, nativeValidateVpaRequest);

    }

    @Test
    public void testPreProcessForNumericId() {

        VpaValidateRequest request = new VpaValidateRequest();

        VpaValidateRequestBody body = new VpaValidateRequestBody();

        TokenRequestHeader head = new TokenRequestHeader();

        String mid = "mPmaOY80417278861100";

        String numericId = "9709670803";

        String orderId = "myid123";

        String txnToken = "123456";

        String workFlow = "abcde";

        String queryParams = "qwerty";

        body.setNumericId(numericId);

        body.setMid(mid);

        body.setQueryParams(queryParams);

        head.setTokenType(TokenType.TXN_TOKEN);

        head.setToken(txnToken);

        head.setWorkFlow(workFlow);

        request.setBody(body);

        request.setHead(head);

        NativeInitiateRequest nativeInitiateRequest = Mockito.mock(NativeInitiateRequest.class);

        InitiateTransactionRequestBody orderDetail = Mockito.mock(InitiateTransactionRequestBody.class);

        Mockito.when(orderDetail.isAoaSubsOnPgMid()).thenReturn(true);

        Mockito.when(orderDetail.getTxnType()).thenReturn(TxnType.AUTH);

        InitiateTransactionRequest initiateTransactionRequest = Mockito.mock(InitiateTransactionRequest.class);

        Mockito.when(initiateTransactionRequest.getBody()).thenReturn(orderDetail);

        Mockito.when(nativeInitiateRequest.getInitiateTxnReq()).thenReturn(initiateTransactionRequest);

        Mockito.when(nativeSessionUtil.validate(txnToken)).thenReturn(nativeInitiateRequest);

        Mockito.when(nativeSessionUtil.checkVPAValidationLimit(txnToken)).thenReturn(false);

        nativeValidateVpaRequestProcessor.preProcess(request);

    }

    @Test
    public void testPreProcessForVpa() {

        VpaValidateRequest request = new VpaValidateRequest();

        VpaValidateRequestBody body = new VpaValidateRequestBody();

        TokenRequestHeader head = new TokenRequestHeader();

        String mid = "mPmaOY80417278861100";

        String vpa = "test9972746530@paytm";

        String orderId = "myid123";

        String txnToken = "123456";

        String workFlow = "abcde";

        String queryParams = "qwerty";

        body.setMid(mid);

        body.setQueryParams(queryParams);

        head.setTokenType(TokenType.TXN_TOKEN);

        head.setToken(txnToken);

        head.setWorkFlow(workFlow);

        body.setVpa(vpa);

        request.setBody(body);

        request.setHead(head);

        NativeInitiateRequest nativeInitiateRequest = Mockito.mock(NativeInitiateRequest.class);

        InitiateTransactionRequestBody orderDetail = Mockito.mock(InitiateTransactionRequestBody.class);

        Mockito.when(orderDetail.isAoaSubsOnPgMid()).thenReturn(true);

        Mockito.when(orderDetail.getTxnType()).thenReturn(TxnType.AUTH);

        InitiateTransactionRequest initiateTransactionRequest = Mockito.mock(InitiateTransactionRequest.class);

        Mockito.when(initiateTransactionRequest.getBody()).thenReturn(orderDetail);

        Mockito.when(nativeInitiateRequest.getInitiateTxnReq()).thenReturn(initiateTransactionRequest);

        Mockito.when(nativeSessionUtil.validate(txnToken)).thenReturn(nativeInitiateRequest);

        Mockito.when(nativeSessionUtil.checkVPAValidationLimit(txnToken)).thenReturn(false);

        nativeValidateVpaRequestProcessor.preProcess(request);

    }

}