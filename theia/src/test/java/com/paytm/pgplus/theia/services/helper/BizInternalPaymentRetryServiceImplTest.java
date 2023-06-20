package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class BizInternalPaymentRetryServiceImplTest {

    private BizInternalPaymentRetryServiceImpl bizInternalPaymentRetryService = new BizInternalPaymentRetryServiceImpl();

    private static final Logger LOGGER = LoggerFactory.getLogger(BizServiceImpl.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testRetryBankFormFetchWithPayment() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        IWorkFlow iWorkFlow = mock(IWorkFlow.class);
        workFlowRequestBean.setDirectBankCardFlow(true);
        workFlowRequestBean.setFromAoaMerchant(true);
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setQueryPaymentStatus(new QueryPaymentStatus(
                new QueryPaymentStatus.QueryPaymentStatusBuilder("transId", "description", "amount", "value", "userId",
                        "statusValue")));
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBeanGeneric = new GenericCoreResponseBean<WorkFlowResponseBean>(
                workFlowResponseBean);
        workFlowResponseBeanGeneric.setRetryStatus(RetryStatus.BANK_FORM_FETCH_FAILED);
        when(iWorkFlow.process(any())).thenReturn(workFlowResponseBeanGeneric);
        bizInternalPaymentRetryService.retryBankFormFetchWithPayment(workFlowRequestBean, iWorkFlow);

        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBeanGeneric1 = new GenericCoreResponseBean<WorkFlowResponseBean>(
                new WorkFlowResponseBean());
        when(iWorkFlow.process(any())).thenReturn(workFlowResponseBeanGeneric1);
        bizInternalPaymentRetryService.retryBankFormFetchWithPayment(workFlowRequestBean, iWorkFlow);

        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean.setDirectBankCardFlow(false);
        bizInternalPaymentRetryService.retryBankFormFetchWithPayment(workFlowRequestBean1, iWorkFlow);

    }
}