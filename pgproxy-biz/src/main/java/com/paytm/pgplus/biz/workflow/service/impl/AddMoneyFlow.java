/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("addMoneyFlow")
public class AddMoneyFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyFlow.class);

    @Autowired
    @Qualifier("addMoneyValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {
        // Request Bean Validation
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // fetch UserDetails
        final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), true);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }

        workFlowTransBean.setUserDetails(userDetails.getResponse());

        if (null != workFlowTransBean.getWorkFlowBean() && null != workFlowTransBean.getWorkFlowBean().getExtendInfo()
                && StringUtils.isBlank(workFlowTransBean.getWorkFlowBean().getExtendInfo().getClientId())) {
            LOGGER.info("Setting clientId for top Up flow");
            workFlowTransBean.getWorkFlowBean().getExtendInfo().setClientId(userDetails.getResponse().getClientId());
        }

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

        /*
         * KYC On-Boarding
         */
        workFlowHelper.setIfKYCRequired(consultAddMoney.getResponse(), workFlowTransBean);

        // Create TopUp
        final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUpResponse = workFlowHelper.createTopUp(
                workFlowTransBean, false);
        if (!createTopUpResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createTopUpResponse.getFailureMessage(),
                    createTopUpResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());

        // Consult PayView
        GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView = workFlowHelper.consultPayView(
                workFlowTransBean, true);
        if (!consultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultPayView.getFailureMessage(),
                    consultPayView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(consultPayView.getResponse());

        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        if (workFlowTransBean.getMerchantViewConsult() != null
                && workFlowTransBean.getMerchantViewConsult().isPaymentsBankSupported()
                && workFlowTransBean.getUserDetails() != null
                && workFlowTransBean.getUserDetails().isSavingsAccountRegistered()) {
            // Invoke BMW API
            GenericCoreResponseBean<AccountBalanceResponse> bankResponse = workFlowHelper
                    .fetchAccountBalance(workFlowTransBean);
            if (bankResponse.isSuccessfullyProcessed() && bankResponse.getResponse() != null) {
                workFlowTransBean.setAccountBalanceResponse(bankResponse.getResponse());
            }
        }

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setAddMoneyDestination(workFlowTransBean.getAddMoneyDestination());
        workFlowResponseBean.setTargetPhoneNo(workFlowTransBean.getWorkFlowBean().getTargetPhoneNo());
        workFlowResponseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        workFlowResponseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());
        workFlowResponseBean.setTrustFactor(consultAddMoney.getResponse().getTrustFactor());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "AddMoneyFlow",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }
}