package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("fetchChannelDetailsTask")
public class FetchChannelDetailsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchChannelDetailsTask.class);

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        GenericCoreResponseBean<Object> genericChannelResponse = workFlowHelper.fetchChannelDetails(input);
        if (null == genericChannelResponse || null == genericChannelResponse.getResponse()) {
            LOGGER.info("Could not get any response for ChannelDetails API");
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }
        transBean.setChannelDetails(genericChannelResponse.getResponse());
        return new GenericCoreResponseBean<Boolean>(Boolean.TRUE);
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setChannelDetails(transBean.getChannelDetails());
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_CHANNEL_DETAILS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_CHANNEL_DETAILS_TIME, "300"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {

        QRCodeInfoResponseData qrCodeInfo = inputBean.getqRCodeInfo();
        return inputBean.isMlvSupported() && null != qrCodeInfo && StringUtils.isNotEmpty(qrCodeInfo.getKybId())
                && StringUtils.isNotBlank(qrCodeInfo.getShopId());
    }
}
