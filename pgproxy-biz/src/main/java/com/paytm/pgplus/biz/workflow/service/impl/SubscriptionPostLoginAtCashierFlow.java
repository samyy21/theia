/**
 * This flow , validate authCode , fetch userdetails , consultpayView , if add
 * and paya allowed, fetch view for add and pay
 * 
 * For reference see flow number 15
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("subscriptionPostLoginAtCashierFlow")
public class SubscriptionPostLoginAtCashierFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPostLoginAtCashierFlow.class);

    @Autowired
    @Qualifier("normalPostLoginValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        // Verify Login
        final GenericCoreResponseBean<VerifyLoginResponseBizBean> verifyLoginResponse = workFlowHelper
                .verifyLogin(workFlowTransBean);
        LOGGER.debug("Verify Login Response :: {}", verifyLoginResponse);
        if (!verifyLoginResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Verification of user failed due to ::{}", verifyLoginResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(verifyLoginResponse.getFailureMessage(),
                    verifyLoginResponse.getResponseConstant());
        }
        workFlowTransBean.setUserDetails(verifyLoginResponse.getResponse().getUserDetails());
        workFlowHelper.filterSavedCardsForSubscription(workFlowTransBean);

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(merchantConsultView.getFailureMessage(),
                    merchantConsultView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultView.getResponse());
        workFlowHelper.filterOperations(workFlowTransBean, true);

        // ConsultAdd Check Allowed PayModes
        EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);
        if (EPayMode.HYBRID.equals(allowedPayMode)) {
            allowedPayMode = EPayMode.NONE;
        }
        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // ConsultPayView for AddAndPay
            final GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponseBean = workFlowHelper
                    .consultPayView(workFlowTransBean, true);
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponseBean.getResponse());
            workFlowHelper.filterOperations(workFlowTransBean, false);
        }
        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        final WorkFlowResponseBean flowResponseBean = new WorkFlowResponseBean();
        flowResponseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        flowResponseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        flowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        flowResponseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        flowResponseBean.setTransID(workFlowTransBean.getWorkFlowBean().getTransID());
        flowResponseBean.setSubsType(workFlowTransBean.getWorkFlowBean().getSubsTypes());
        flowResponseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SubscriptionPostLoginAtCashierFlow",
                flowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(flowResponseBean);
    }
}