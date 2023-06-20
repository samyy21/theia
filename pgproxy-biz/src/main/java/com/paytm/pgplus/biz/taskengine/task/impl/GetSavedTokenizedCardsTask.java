package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.TokenizedCardsResponseBizBean;
import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.TokenizedCardsWorkflowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("getSavedTokenizedCardsTask")
public class GetSavedTokenizedCardsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetSavedTokenizedCardsTask.class);

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    @Qualifier("tokenizedCardsWorkFlowHelper")
    private TokenizedCardsWorkflowHelper workFlowHelper;

    @Autowired
    private CoftUtil coftUtil;

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {

        boolean returnToken = (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(requestBean.getRequestType()) || ERequestType.NATIVE_MF_SIP_PAY
                .equals(requestBean.getRequestType())) ? iPgpFf4jClient.checkWithdefault(
                "theia.getTokenizedCardsInSubscriptionFPO", null, false) : iPgpFf4jClient.checkWithdefault(
                "theia.getTokenizedCardsInFPO", null, true);

        LOGGER.info("return token and requestType: {} , {} ", returnToken, requestBean.getRequestType());

        if ((!requestBean.isFromAoaMerchant())
                && returnToken
                && (mappingUtil.isLocalVaultEnabled(requestBean.getPaytmMID(), requestBean.getCustID()) || ff4JUtil
                        .isGlobalVaultEnabled(transBean, requestBean.getPaytmMID()))) {
            return (coftUtil.isCardPaymethodDisabled(requestBean) ? (isAddNPayLPVFlow(requestBean, transBean) ? true
                    : false) : true);
        }

        return false;
    }

    @Override
    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean)
            throws BaseException {

        LOGGER.info("Starting get tokenized cards task");
        GenericCoreResponseBean<TokenizedCardsResponseBizBean> consultResponse = workFlowHelper
                .fetchTokenizedCardsConsult(workFlowTransactionBean);
        if (consultResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Successfully processed tokenized cards response");
            workFlowTransactionBean.setTokenizedCards(consultResponse.getResponse());
        } else {
            LOGGER.error("error while processing tokenized cards response {}", consultResponse);
        }
        return consultResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.GET_TOKENIZED_CARDS;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.GET_TOKENIZED_CARDS_TIME, "150000"));
    }

    public boolean isAddNPayLPVFlow(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {
        Map<String, String> extendInfoMap = transBean.getMerchantViewConsult().getExtendInfo();
        if (null != extendInfoMap && !extendInfoMap.isEmpty()) {
            String addAndPayAllowed = extendInfoMap
                    .get(BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_KEY);
            if (BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_YES.equals(addAndPayAllowed)
                    && (org.apache.commons.lang3.StringUtils.isNotBlank(requestBean.getToken()) || requestBean
                            .getUserDetailsBiz() != null)) {
                return true;
            }
        }
        return false;
    }
}
