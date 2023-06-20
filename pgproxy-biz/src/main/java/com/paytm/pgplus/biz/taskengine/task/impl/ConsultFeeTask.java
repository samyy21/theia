package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.DISABLE_CHARGE_IN_DYNAMIC_QR_2FA;
import static com.paytm.pgplus.biz.utils.BizConstant.TRUE;

/**
 * Task calls platform+ boss bulk fee convience API for fetching convience fee on
 * Post convience merchant
 */

/**
 * Created by charu on 25/07/18.
 */

@Service("consultFeeTask")
public class ConsultFeeTask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultFeeTask.class);

    @Autowired
    private FF4JUtil ff4JUtil;

    @Override
    public GenericCoreResponseBean<ConsultFeeResponse> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                .consultBulkFeeResponse(workFlowTransactionBean);
        if (consultFeeResponse.isSuccessfullyProcessed()) {
            workFlowTransactionBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            if (!(TRUE.equals(ConfigurationUtil.getTheiaProperty(DISABLE_CHARGE_IN_DYNAMIC_QR_2FA, "false")))
                    && ERequestType.DYNAMIC_QR_2FA.equals(workFlowRequestBean.getRequestType())) {
                updateTransactionAmountWithPCF(consultFeeResponse.getResponse(), workFlowRequestBean);
            }
        }
        return consultFeeResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.CONSULT_FEE_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.CONSULT_FEE_TIME, "90"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setConsultFeeResponse(transBean.getConsultFeeResponse());
    }

    private void updateTransactionAmountWithPCF(final ConsultFeeResponse consultFeeResponse,
            final WorkFlowRequestBean flowRequestBean) {

        Map<EPayMethod, ConsultDetails> consultDetailsMap = consultFeeResponse.getConsultDetails();

        ConsultDetails consultDetails = consultDetailsMap.get(EPayMethod.BALANCE);

        flowRequestBean.setTxnAmount(consultDetails.getTotalTransactionAmount().multiply(new BigDecimal(100))
                .toPlainString());
        flowRequestBean.setChargeAmount(calculateChargeAmountInPaise(consultDetails));

    }

    private String calculateChargeAmountInPaise(ConsultDetails consultDetails) {
        String chargeAmount = null;
        if ((consultDetails != null) && (consultDetails.getTotalTransactionAmount() != null)
                && (consultDetails.getBaseTransactionAmount() != null)) {
            chargeAmount = String.valueOf(consultDetails.getTotalTransactionAmount().subtract(
                    consultDetails.getBaseTransactionAmount()));

            chargeAmount = AmountUtils.getTransactionAmountInPaise(chargeAmount);
            LOGGER.info("Charge amount calculated for ScanNPay is::{}", chargeAmount);
        }
        return chargeAmount;
    }

}
