package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("realTimeReconciliationTask")
public class RealTimeReconciliationTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeReconciliationTask.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(RealTimeReconciliationTask.class);

    private static final Logger STATS_LOGGER = LoggerFactory.getLogger("STATS_LOGGER");

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    CashierDomainResponseMapperUtil mapperUtil;

    @Autowired
    SavedAssetMigrationReconUtil reconUtil;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Override
    protected GenericCoreResponseBean<WorkFlowResponseBean> doBizExecute(WorkFlowRequestBean requestBean,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId()
                : null != requestBean.getUserDetailsBiz() ? requestBean.getUserDetailsBiz().getUserId() : null;

        /**
         * Added ff4j flag here to check whether we have fetched data from
         * savedcardservice by not shortcircuiting it
         *
         */
        if (ff4JUtil.fetchSavedCardFromService(userId, requestBean.getPaytmMID(), requestBean.getCustID())) {
            LOGGER.info("Starting real time asset reconcilation");
            reconUtil.assetRecon(transBean.getWorkFlowBean(), transBean, response);
        }

        if (transBean.isAssetReconcilationStatus()) {
            LOGGER.debug("Recon_successful for custId {}, for mid {}, for userId {}", transBean.getWorkFlowBean()
                    .getCustID(), transBean.getWorkFlowBean().getPaytmMID(), userId);
            STATS_LOGGER.info("Recon_successful, custId {}, mid {}, userId {}",
                    transBean.getWorkFlowBean().getCustID(), transBean.getWorkFlowBean().getPaytmMID(), userId);
        }
        return new GenericCoreResponseBean<>(response);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.REAL_TIME_SAVEDCARD_RECON;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.REAL_TIME_RECON_EXECUTION_TIME, "20"));

    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean request, WorkFlowTransactionBean transBean) {
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != request
                .getUserDetailsBiz() ? request.getUserDetailsBiz().getUserId() : null;
        return !(StringUtils.isNotBlank(request.getAccessToken()) && ff4jUtils.featureEnabledOnMultipleKeys(
                request.getPaytmMID(), userId,
                BizConstant.Ff4jFeature.BLACKLIST_SAVED_CARDS_IN_FPO_V2_WITH_ACCESS_TOKEN, false))
                && ff4JUtil.fetchSavedCardFromPlatform(transBean.getWorkFlowBean(), userId);
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != transBean
                .getWorkFlowBean().getUserDetailsBiz() ? transBean.getWorkFlowBean().getUserDetailsBiz().getUserId()
                : null;
        if (transBean.isAssetReconcilationStatus()
                && ff4JUtil.returnSavedCardsFromPlatform(userId, transBean.getWorkFlowBean().getPaytmMID(), transBean
                        .getWorkFlowBean().getCustID())) {
            // setting savedcard returned from platform;
            EXT_LOGGER.customInfo("returing savedCardFrom from platform for mid {} ", transBean.getWorkFlowBean()
                    .getPaytmMID());
            // when user is loggedIn
            if (response.getUserDetails() != null) {
                response.getUserDetails().setMerchantViewSavedCardsList(
                        mapperUtil.getCardBeanListFromPlatformResponse(response.getMerchnatLiteViewResponse()));
                response.getUserDetails().setAddAndPayViewSavedCardsList(
                        mapperUtil.getCardBeanListFromPlatformResponse(response.getAddAndPayLiteViewResponse()));
                transBean.getUserDetails().setMerchantViewSavedCardsList(
                        response.getUserDetails().getMerchantViewSavedCardsList());
                transBean.getUserDetails().setAddAndPayViewSavedCardsList(
                        response.getUserDetails().getAddAndPayViewSavedCardsList());
            } else {
                if (response.getmIdCustIdCardBizDetails() == null) {
                    response.setmIdCustIdCardBizDetails(new MidCustIdCardBizDetails());
                }
                response.getmIdCustIdCardBizDetails().setMerchantCustomerCardList(
                        mapperUtil.getCardBeanListFromPlatformResponse(response.getMerchnatLiteViewResponse()));
                transBean.setMidCustIdCardBizDetails(response.getmIdCustIdCardBizDetails());
            }
            LOGGER.info("setting of savedCardFromPlatform in response is done sucessfully");

        }
    }
}
