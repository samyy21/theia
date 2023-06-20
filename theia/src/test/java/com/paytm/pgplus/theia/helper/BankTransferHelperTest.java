package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequest;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenSecureRequestHeader;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import mockit.MockUp;
import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BankTransferHelperTest {

    @InjectMocks
    BankTransferHelper bankTransferHelper;

    @Mock
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    private IJsonResponsePaymentService bankTransferService;

    @Mock
    private TheiaResponseGenerator theiaResponseGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processNativeJsonRequest() throws IOException, TheiaDataMappingException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(null);
        new MockUp<IOUtils>() {

            @mockit.Mock
            public String toString(InputStream input, String encoding) throws IOException {
                return "requestData";
            }
        };
        InternalTransactionRequest internalTransactionRequest = new InternalTransactionRequest();
        new MockUp<JsonMapper>() {

            @mockit.Mock
            public <T> T mapJsonToObject(String requestData, Class<T> clazz) {
                return (T) internalTransactionRequest;
            }
        };

        internalTransactionRequest.setHead(new TokenSecureRequestHeader());
        internalTransactionRequest.getHead().setChannelId(EChannelId.APP);
        internalTransactionRequest.setBody(new InternalTransactionRequestBody());
        internalTransactionRequest.getBody().setTxnAmount(new Money());
        when(bankTransferService.processPaymentRequest(any())).thenReturn(new WorkFlowResponseBean());
        NativeJsonResponse response = new NativeJsonResponse();
        response.setBody(new NativeJsonResponseBody());
        response.getBody().setTxnInfo(new HashMap<>());
        when(theiaResponseGenerator.getNativeJsonResponse(any(), any(), any())).thenReturn(response);
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setChannelInfo(new HashMap<>());
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        assertNotNull(bankTransferHelper.processNativeJsonRequest(request));
    }
}