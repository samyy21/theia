package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.enums.ChannelType;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.cache.model.EmiDetailRequest;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("fetchMerchantEmiDetailsTask")
public class FetchMerchantEmiDetailsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMerchantEmiDetailsTask.class);

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean response) {

        EmiDetailRequest emiDetailRequest = new EmiDetailRequest();
        emiDetailRequest.setMid(workFlowRequestBean.getPaytmMID());
        emiDetailRequest.setStatus(Boolean.TRUE);
        emiDetailRequest.setChannelType(ChannelType.WAP);

        try {
            EMIDetailList emiDetailList = workFlowHelper.fetchEmiDetailsList(emiDetailRequest);
            if (null == emiDetailList || CollectionUtils.isEmpty(emiDetailList.getEmiDetails())) {
                LOGGER.info("Emi not configured on merchant get empty response from mapping-service");
            }
            response.setEmiDetailList(emiDetailList);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching emi details :{} error : {} ", emiDetailRequest, e);
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }

        return new GenericCoreResponseBean<Boolean>(Boolean.TRUE);

    }

    @Override
    public TaskName getTaskName() {
        return TaskName.MERCHANT_EMI_DETAILS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.MERCHANT_EMI_DETAILS_TIME, "2050"));
    }
}
