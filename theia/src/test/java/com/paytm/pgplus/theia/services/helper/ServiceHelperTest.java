package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class ServiceHelperTest {

    @InjectMocks
    private ServiceHelper serviceHelper = new ServiceHelper();

    @Mock
    private MerchantResponseService merchantResponseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCheckIfBizResponseResponseFailed() {
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                "description", ResponseConstants.SUCCESS);
        serviceHelper.checkIfBizResponseResponseFailed(bizResponseBean);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean1 = new GenericCoreResponseBean<WorkFlowResponseBean>(
                new WorkFlowResponseBean());
        serviceHelper.checkIfBizResponseResponseFailed(bizResponseBean1);
    }

    @Test
    public void testReturnFailureResponseToMerchant() {
        PaymentRequestBean requestData = new PaymentRequestBean();
        when(merchantResponseService.processMerchantFailResponse(any(), (ResponseConstants) any())).thenReturn(
                "htmlPage");
        serviceHelper.returnFailureResponseToMerchant(requestData, ResponseConstants.FAILURE);
    }

    @Test
    public void testProcessBizWorkFlow() {
        IWorkFlow iWorkFlow = mock(IWorkFlow.class);
        IBizService iBizService = mock(IBizService.class);
        serviceHelper.processBizWorkFlow(new WorkFlowRequestBean(), iWorkFlow, iBizService);
    }

}