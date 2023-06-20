package com.paytm.pgplus.biz.taskengine.task;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.taskengine.task.impl.FetchPostpaidBalanceTask;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:biz-context-test.xml" })
public class FetchPostpaidBalanceTaskTest {

    @Value("classpath:MerchantPayMethod.json")
    Resource merchantPayMethodJson;
    private AbstractTask fetchPostpaidBalanceTask;
    private WorkFlowRequestBean workFlowRequestBean;
    private WorkFlowTransactionBean workFlowTransactionBean;
    @Mock
    private WorkFlowHelper workFlowHelper;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        fetchPostpaidBalanceTask = Mockito.mock(FetchPostpaidBalanceTask.class, Mockito.CALLS_REAL_METHODS);
        workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setOfflineFetchPayApi(true);
        workFlowRequestBean.setToken("mock");
        workFlowRequestBean.setPaytmMID("mock");
        workFlowTransactionBean = new WorkFlowTransactionBean();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = mapper.readValue(
                merchantPayMethodJson.getURL(), LitePayviewConsultResponseBizBean.class);
        workFlowTransactionBean.setMerchantLiteViewConsult(litePayviewConsultResponseBizBean);
        workFlowTransactionBean.setWorkFlowBean(workFlowRequestBean);
    }

    @Test
    public void shouldDisablePayMethodWhenBalanceIsInValid() {

        workFlowRequestBean.setNativeAddMoney(true);
        workFlowRequestBean.setTxnAmount("2000");
        when(workFlowHelper.fetchPostpaidBalanceResponse(any(), any())).thenReturn(
                new GenericCoreResponseBean<>(Boolean.TRUE));
        fetchPostpaidBalanceTask.doBizPostProcess(workFlowTransactionBean, new WorkFlowResponseBean());
        workFlowTransactionBean
                .getMerchantLiteViewConsult()
                .getPayMethodViews()
                .stream()
                .filter(payMethodViewsBiz -> EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(
                        payMethodViewsBiz.getPayMethod())).findFirst().ifPresent(payMethodViewsBiz ->

                Assert.assertFalse(payMethodViewsBiz.getPayChannelOptionViews().get(0).isEnableStatus())

                );

    }

    @Test
    public void shouldEnablePayMethodWhenBalanceIsValid() {

        workFlowRequestBean.setNativeAddMoney(true);
        workFlowRequestBean.setTxnAmount("2600");
        when(workFlowHelper.fetchPostpaidBalanceResponse(any(), any())).thenReturn(
                new GenericCoreResponseBean<>(Boolean.TRUE));
        fetchPostpaidBalanceTask.doBizPostProcess(workFlowTransactionBean, new WorkFlowResponseBean());
        workFlowTransactionBean
                .getMerchantLiteViewConsult()
                .getPayMethodViews()
                .stream()
                .filter(payMethodViewsBiz -> EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(
                        payMethodViewsBiz.getPayMethod())).findFirst().ifPresent(payMethodViewsBiz ->

                Assert.assertTrue(payMethodViewsBiz.getPayChannelOptionViews().get(0).isEnableStatus())

                );

    }
}
