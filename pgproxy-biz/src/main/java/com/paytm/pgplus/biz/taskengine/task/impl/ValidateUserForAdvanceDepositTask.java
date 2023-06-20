package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("validateUserForAdvanceDepositTask")
public class ValidateUserForAdvanceDepositTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {
    /**
     * These paymode will not be supported for corporate Advnace Deposit since
     * child token is passed in request.
     */
    static List<String> disablePayModes = new ArrayList<>();
    static {
        disablePayModes.add(PayMethod.BALANCE.getMethod());
        disablePayModes.add(PayMethod.PPBL.getMethod());
        disablePayModes.add(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
    }

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        final GenericCoreResponseBean<Boolean> kybResponse = workFlowHelper.validateUserForAdvanceDeposit(transBean
                .getUserDetails().getChildUserId(), input.getbId(), input.getCorporateCustId());
        input.setDisabledPaymentModes(disablePayModes);
        if (kybResponse.isSuccessfullyProcessed() && kybResponse.getResponse()) {
            EventUtils.pushTheiaEvents(EventNameEnum.CORPORATE_ADVANCE_DEPOSIT_AVAILABLE, new ImmutablePair<>(
                    "CORPORATE_ADVANCE_DEPOSIT_STATUS", "AVAILABLE"));
            response.setAdvanceDepositAvailable(kybResponse.getResponse());
        }
        return kybResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.VALIDATE_USER_FOR_ADVANCE_DEPOSIT;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return StringUtils.isNotBlank(input.getToken()) && StringUtils.isNotBlank(input.getCorporateCustId())
                && StringUtils.isNotBlank(input.getbId());
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.VALIDATE_USER_FOR_ADVANCE_DEPOSIT, "5000"));
    }
}
