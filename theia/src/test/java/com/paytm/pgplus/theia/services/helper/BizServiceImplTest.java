package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.services.InternalPaymentRetryService;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class BizServiceImplTest {

    @InjectMocks
    private BizServiceImpl bizService = new BizServiceImpl();

    @Mock
    private InternalPaymentRetryService internalPaymentRetryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizServiceImpl.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testProcessWorkFlow() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        IWorkFlow iWorkFlow = mock(IWorkFlow.class);
        GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                new WorkFlowResponseBean());
        when(iWorkFlow.process(any())).thenReturn(flowResponseBean);
        when(internalPaymentRetryService.isInternalPaymentRetryRequired(any())).thenReturn(true);
        when(internalPaymentRetryService.retryBankFormFetchWithPayment(any(), any())).thenReturn(flowResponseBean);
        bizService.processWorkFlow(workFlowRequestBean, iWorkFlow);

        when(internalPaymentRetryService.isInternalPaymentRetryRequired(any())).thenReturn(false);
        bizService.processWorkFlow(workFlowRequestBean, iWorkFlow);

        when(internalPaymentRetryService.isInternalPaymentRetryRequired(any())).thenReturn(true);
        when(internalPaymentRetryService.retryBankFormFetchWithPayment(any(), any())).thenReturn(null);
        bizService.processWorkFlow(workFlowRequestBean, iWorkFlow);
    }
}