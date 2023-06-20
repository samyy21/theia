package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author vivek
 */

@Service("retryAddMoneyFlow")
public class RetryAddMoneyFlow implements IWorkFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryAddMoneyFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("addMoneyValidator")
    private IValidator validatorService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // setting TransId
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        // fetch UserDetails
        final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), true);

        if (!userDetails.isSuccessfullyProcessed()) {
            LOGGER.error("user details fetching failed due to ::{} for token::{}", userDetails.getFailureMessage(),
                    flowRequestBean.getToken());
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }

        workFlowTransBean.setUserDetails(userDetails.getResponse());

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

        // cache card
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
        if (createCacheCardResponse != null) {
            return createCacheCardResponse;
        }

        // ConsultAdd Money from wallet
        final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                .consultAddMoneyV2(workFlowTransBean);
        if (!consultAddMoney.isSuccessfullyProcessed()
                || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney.getResponse()
                        .isLimitApplicable())) {
            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }
        workFlowTransBean.setAddMoneyDestination(consultAddMoney.getResponse().getAddMoneyDestination());

        // Consult PayView
        GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView = workFlowHelper.consultPayView(
                workFlowTransBean, true);

        if (!consultPayView.isSuccessfullyProcessed()) {
            LOGGER.error("Consult PayView Failed due to:: {}", consultPayView.getFailureMessage());
            return new GenericCoreResponseBean<>(consultPayView.getFailureMessage(),
                    consultPayView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(consultPayView.getResponse());

        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();

        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setAddMoneyDestination(workFlowTransBean.getAddMoneyDestination());

        return new GenericCoreResponseBean<>(workFlowResponseBean);

    }

}