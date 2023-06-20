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
import com.paytm.pgplus.facade.emisubvention.models.request.UserSummaryBulkPaymentOptionsRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.UserSummaryBulkPaymentOptionsResponse;
import com.paytm.pgplus.facade.emisubvention.service.IEmiSubventionService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.*;

@Service("nativeEmiSubventionBulkTask")
public class NativeEmiSubventionBulkTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeEmiSubventionBulkTask.class);

    @Autowired
    WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    IEmiSubventionService emiSubventionService;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        UserSummaryBulkPaymentOptionsRequest userSummaryBulkPaymentOptionsRequest = workFlowRequestCreationHelper
                .createUserSummaryBulkPaymentOptionsRequest(input, transBean, response);

        Map<String, String> header = new HashMap<>();
        header.put(EMI_SUBVENTION_USER_ID, input.getEmiSubventionCustomerId());
        if (StringUtils.equalsIgnoreCase("ITEM_BASED", input.getEmiSubventionStratergy())) {
            header.put(EmiSubventionConstants.CLIENT, EmiSubventionConstants.ITEM_LEVEL_CLIENT_VALUE);
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(EMI_SUBVENTION_CUST_ID, input.getEmiSubventionCustomerId());
        queryParams.put(EMI_SUBVENTION_PG_MID, input.getPaytmMID());

        try {
            GenericEmiSubventionResponse<UserSummaryBulkPaymentOptionsResponse> userSummaryBulkPaymentOptionsResponse = emiSubventionService
                    .bulkEmiApply(userSummaryBulkPaymentOptionsRequest, header, queryParams);
            return workFlowRequestCreationHelper.createUserSummaryBulkPaymentOptionsResponse(input, transBean,
                    response, userSummaryBulkPaymentOptionsResponse);

        } catch (FacadeCheckedException e) {
            LOGGER.error("saved card bulk subvention api failed {}", e);
            return new GenericCoreResponseBean<>(false);
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_EMI_SUBVENTION_BULK_SAVEDCARD_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(EMI_SUBVENTION_BULK_SAVECARD_TIME, "200"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
        response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        boolean isSavedCardPresent = false;
        if (transBean.getMidCustIdCardBizDetails() != null
                && !CollectionUtils.isEmpty(transBean.getMidCustIdCardBizDetails().getMerchantCustomerCardList())
                || transBean.getUserDetails() != null
                && transBean.getUserDetails().getMerchantViewSavedCardsList() != null)
            isSavedCardPresent = true;

        if (input.isEmiSubventionRequired() && input.getItems() != null
                && input.getEmiSubventedTransactionAmount() != null && input.getEmiSubventionCustomerId() != null
                && isSavedCardPresent)
            return true;
        return false;
    }
}
