package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.facade.emisubvention.constants.EmiSubventionConstants;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.emisubvention.service.IEmiSubventionService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.EMI_SUBVENTION_PG_MID;

@Service("fetchEmiSubventedBanksTask")
public class FetchEmiSubventedBanksTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchEmiSubventedBanksTask.class);

    @Autowired
    IEmiSubventionService emiSubventionService;

    @Autowired
    WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        try {
            Map<String, String> header = new HashMap<>();
            if (StringUtils.equalsIgnoreCase("ITEM_BASED", input.getEmiSubventionStratergy())) {
                header.put(EmiSubventionConstants.CLIENT, EmiSubventionConstants.ITEM_LEVEL_CLIENT_VALUE);
            }
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put(EMI_SUBVENTION_PG_MID, input.getPaytmMID());
            GenericEmiSubventionResponse<BanksResponse> banksResponse = emiSubventionService.getBanks(
                    workFlowRequestCreationHelper.createEmiSubventionBanksRequest(input.getItems()), header,
                    queryParams);
            LOGGER.info("response returned from  emi subvention engine is for banks api is  : {}", banksResponse);

            if (banksResponse != null && banksResponse.getStatus() == 0) {
                LOGGER.info("Error in fetching emi subvention banks list error code : {}, error message : {}",
                        banksResponse.getError().getCode(), banksResponse.getError().getMessage());
                return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
            }

            BanksResponse emiSubventionBanks = banksResponse.getData();
            transBean.setEmiSubventionPlans(emiSubventionBanks);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching emi subvented bank request  :{} error : {} ",
                    workFlowRequestCreationHelper.createEmiSubventionBanksRequest(input.getItems()), e);
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }

        return new GenericCoreResponseBean<>(true);
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setEmiSubventionPlans(transBean.getEmiSubventionPlans());
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_EMI_SUBVENTED_BANKS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {

        if (input.getItems() != null && input.isEmiSubventionRequired()) {
            return true;
        }
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        // time to be decided
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.EMI_SUBVENTION_BANKS_TIME, "200"));

    }
}
