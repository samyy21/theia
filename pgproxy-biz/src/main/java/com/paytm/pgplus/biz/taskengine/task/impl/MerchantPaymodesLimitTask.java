package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.facade.utils.MappingServiceClient;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.model.MerchantLimitResponse;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantLimit;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("merchantPaymodesLimitTask")
public class MerchantPaymodesLimitTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantPaymodesLimitTask.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantPaymodesLimitTask.class);

    @Autowired
    IMerchantLimit merchantLimit;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        try {

            if (StringUtils.isBlank(input.getPaytmMID())) {
                throw new Exception("Paytm mid does not exist");
            }
            List<String> checkPayModesLimitTypeList = Arrays.asList(com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(BizConstant.PAYMODES_LIMIT_TYPE_LIST, "").split(","));
            if (checkPayModesLimitTypeList != null
                    && checkPayModesLimitTypeList.size() > 0
                    && !(checkPayModesLimitTypeList.size() == 1 && StringUtils.isBlank(checkPayModesLimitTypeList
                            .get(0)))) {
                MerchantExtendedInfoResponse merchantExtendedInfoResponse = MappingServiceClient
                        .getMerchantExtendedInfo(input.getPaytmMID());
                if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                        && merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit() != null) {
                    String merchantLimitType = String.valueOf(merchantExtendedInfoResponse.getExtendedInfo()
                            .getMerchantLimit());
                    if (merchantLimitType != null && checkPayModesLimitTypeList.contains(merchantLimitType)) {
                        LOGGER.info("MerchantPaymodesLimitTask | fetching paymodes limits for merchantLimitType : {}",
                                merchantLimitType);
                        transBean.setMerchantLimitType(merchantExtendedInfoResponse.getExtendedInfo()
                                .getMerchantLimit());
                        MerchantLimitResponse merchantLimitResponse = merchantLimit
                                .getMerchantLimits(BizConstant.PPI_LIMIT_PREFIX + merchantLimitType);
                        EXT_LOGGER.customInfo("Mapping response - MerchantLimitResponse :: {}", merchantLimitResponse);
                        if (merchantLimitResponse != null) {
                            transBean.setmerchantPaymodesLimits(merchantLimitResponse.getMerchantLimits());
                        }
                    }
                }
            }
            return new GenericCoreResponseBean<>(Boolean.TRUE);

        } catch (Exception e) {
            LOGGER.error("Unable to get data from merchant extendInfo and merchant paymodes Limit API : ", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.MERCHANT_PAYMODES_LIMIT_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.MERCHANT_PAYMODES_LIMIT_TIME, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return Boolean.valueOf(com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                BizConstant.MERCHANT_PAYMODES_LIMIT_TASK_ENABLE, "false"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setMerchantLimitType(transBean.getMerchantLimitType());
        response.setmerchantPaymodesLimits(transBean.getmerchantPaymodesLimits());
    }
}
