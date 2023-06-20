package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.upi.IFetchRecurringMandateService;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.recurringMandateResponse.RecurringMandateBanksAndPspListResponse;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("fetchMandateBankAndPspTask")
public class FetchMandateBankAndPspTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMandateBankAndPspTask.class);

    @Autowired
    IFetchRecurringMandateService fetchRecurringMandateService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        try {
            RecurringMandateBanksAndPspListResponse recurringMandateBanksAndPspListResponse;
            recurringMandateBanksAndPspListResponse = (RecurringMandateBanksAndPspListResponse) theiaSessionRedisUtil
                    .get(TheiaConstant.ExtraConstants.UPI_PSP_BANK_RESPONSE);
            if (recurringMandateBanksAndPspListResponse == null) {
                recurringMandateBanksAndPspListResponse = fetchRecurringMandateService.fetchRecurringBanksAndPsp();
                if (recurringMandateBanksAndPspListResponse != null
                        && "SUCCESS".equals(recurringMandateBanksAndPspListResponse.getStatus())) {
                    theiaSessionRedisUtil.setnx(TheiaConstant.ExtraConstants.UPI_PSP_BANK_RESPONSE,
                            recurringMandateBanksAndPspListResponse, 7200);
                }
            }
            if (recurringMandateBanksAndPspListResponse != null
                    && "FAILURE".equals(recurringMandateBanksAndPspListResponse.getStatus())) {
                LOGGER.error("Error in fetching mandate bank and psp error code : {}, error message : {}",
                        recurringMandateBanksAndPspListResponse.getRespCode(),
                        recurringMandateBanksAndPspListResponse.getRespMessage());
                return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
            }
            transBean.setPspList(recurringMandateBanksAndPspListResponse.getPspList());
            transBean.setBankList(recurringMandateBanksAndPspListResponse.getBankList());
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching mandate bank and psp  :{} error : {} ", transBean
                    .getWorkFlowBean().getOrderID(), e);
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }

        return new GenericCoreResponseBean<Boolean>(Boolean.TRUE);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_MANDATE_BANK_AND_PSP;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return (("UNKNOWN".equals(input.getPaymentRequestBean().getSubsPaymentMode()) || "UPI".equals(input
                .getPaymentRequestBean().getSubsPaymentMode())) && (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(input
                .getRequestType()) || ERequestType.NATIVE_MF_SIP_PAY.equals(input.getRequestType())));
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        // to be discussed
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_MANDATE_BANK_AND_PSP, "1000"));

    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        super.doBizPostProcess(transBean, response);
        response.setPspList(transBean.getPspList());
        response.setBankList(transBean.getBankList());
    }
}
