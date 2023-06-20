package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.merchantlimit.enums.LimitType;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitConsumptionResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitDetail;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service("fetchLimitConsumptionTask")
public class FetchLimitConsumptionTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) throws BaseException {
        try {
            GenericCoreResponseBean<LimitConsumptionResponse> limitConsumptionResponse = workFlowHelper
                    .getConsumedLimitDetail(input.getPaytmMID());
            if (limitConsumptionResponse.getResponse() != null) {
                transBean.setLimitDetails(limitConsumptionResponse.getResponse().getLimitDetails());
                return new GenericCoreResponseBean<>(Boolean.TRUE);
            }
            return new GenericCoreResponseBean<>(Boolean.FALSE);

        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to get data from LimitConsumptionDetails API : ", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_LIMIT_CONSUMPTION_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.MERCHANT_TOTAL_LIMIT_TIME, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        Routes route = null != input.getExtendInfo() ? input.getExtendInfo().getLpvRoute() : null;
        return input.isFullPg2TrafficEnabled()
                && !Routes.PG2.equals(route)
                && !(StringUtils.isNotBlank(input.getAccessToken()) && ff4jUtils.featureEnabledOnMultipleKeys(
                        input.getPaytmMID(), userId,
                        BizConstant.Ff4jFeature.BLACKLIST_SAVED_CARDS_IN_FPO_V2_WITH_ACCESS_TOKEN, false));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        // Populating monthly value in weekly as weekly values won't be correct
        // from pg2
        if (!CollectionUtils.isEmpty(transBean.getLimitDetails())) {
            List<LimitDetail> limitDetailList = transBean.getLimitDetails();
            Optional<LimitDetail> monthlyLimit = limitDetailList.stream()
                    .filter(limitDetail -> LimitType.MONTHLY.equals(limitDetail.getFrequency())).findFirst();
            Optional<LimitDetail> weeklyLimit = limitDetailList.stream()
                    .filter(limitDetail -> LimitType.WEEKLY.equals(limitDetail.getFrequency())).findFirst();
            if (monthlyLimit.isPresent()) {
                if (weeklyLimit.isPresent()) {
                    weeklyLimit.get().setMaxLimit(monthlyLimit.get().getMaxLimit());
                    weeklyLimit.get().setAccumulatedValue(monthlyLimit.get().getAccumulatedValue());
                } else {
                    LimitDetail limitDetail = new LimitDetail();
                    limitDetail.setMaxLimit(monthlyLimit.get().getMaxLimit());
                    limitDetail.setAccumulatedValue(monthlyLimit.get().getAccumulatedValue());
                    limitDetail.setFrequency(LimitType.WEEKLY);
                    limitDetailList.add(limitDetail);
                }
            }
            response.setLimitDetails(limitDetailList);
        }
    }
}
