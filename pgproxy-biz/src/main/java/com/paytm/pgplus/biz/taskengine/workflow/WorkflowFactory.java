package com.paytm.pgplus.biz.taskengine.workflow;

import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.taskengine.workflow.impl.TaskedNativeEnhancedSubscriptionFetchPayOptionFlow;
import com.paytm.pgplus.biz.taskengine.workflow.impl.TaskedNativeSubscriptionFetchPayOptionsFlow;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("workFlowFactory")
public class WorkflowFactory {

    @Autowired
    @Qualifier("defaultFlow")
    private TaskedWorkflow defaultFlow;

    @Autowired
    @Qualifier("taskedNativeEnhancedFlow")
    private TaskedWorkflow nativeEnhancedFlow;

    @Autowired
    @Qualifier("taskedSubscriptionFlow")
    private TaskedWorkflow taskedSubscriptionFlow;

    @Autowired
    @Qualifier("taskedNativeFetchPayOptionsFlow")
    private TaskedWorkflow nativeFetchPayOptionFlow;

    @Autowired
    @Qualifier("taskedNativeSubscriptionFetchPayOptionsFlow")
    private TaskedNativeSubscriptionFetchPayOptionsFlow nativeSubscriptionFetchPayOptions;

    @Autowired
    @Qualifier("taskedNativeEnhancedSubscriptionFetchPayOptionFlow")
    private TaskedNativeEnhancedSubscriptionFetchPayOptionFlow nativeEnhancedSubscriptionFetchPayOptionFlow;

    @Autowired
    @Qualifier("defaultUserNotLoggedInValidator")
    private IValidator defaulFlowValidator;

    @Autowired
    @Qualifier("userNotLoggedInLitePayviewConsultValidator")
    private IValidator fetchPayOptionValidator;

    @Autowired
    @Qualifier("offlineFlow")
    private TaskedWorkflow offlineFlow;

    @Autowired
    @Qualifier("subscriptionFreshCCOnlyPreValidator")
    private IValidator subscriptionFlowValidator;

    @Autowired
    @Qualifier("buyerPaysChargeTaskFlow")
    private TaskedWorkflow buyerPaysChargeFlow;

    @Autowired
    @Qualifier("userNotLoggedInLitePayviewConsultValidator")
    private IValidator nativeFetchPayOptionValidator;

    @Autowired
    @Qualifier("postConvinienceFeeValidator")
    private IValidator buyerPaysChargeFlowValidator;

    @Autowired
    @Qualifier("taskedEmiDetailsFlow")
    private TaskedWorkflow taskedEmiDetailsFlow;

    @Autowired
    @Qualifier("taskedNotificationAppInvokeFlow")
    private TaskedWorkflow taskedNotificationAppInvokeFlow;

    public TaskedWorkflow getWorkFlow(WorkFlowRequestBean input, WorkFlowTransactionBean workFlowTransactionBean) {

        TaskedWorkflow workflow = null;

        if (input.isEmiDetailsApi()) {
            workflow = taskedEmiDetailsFlow;
            return workflow;
        }

        if (input.isNotificationAppInvoke()) {
            workflow = taskedNotificationAppInvokeFlow;
            return workflow;
        }

        if ((ERequestType.DEFAULT.equals(input.getRequestType()) || ERequestType.DYNAMIC_QR_2FA.equals(input
                .getRequestType())) && input.isPostConvenience()) {
            workFlowTransactionBean.setPostConvenienceFeeModel(true);
            workflow = buyerPaysChargeFlow;
            workFlowTransactionBean.setValidator(buyerPaysChargeFlowValidator);
            return workflow;
        }

        if (ERequestType.DEFAULT.equals(input.getRequestType())
                || ERequestType.CC_BILL_PAYMENT.equals(input.getRequestType())
                || ERequestType.RESELLER.equals(input.getRequestType())) {
            workflow = defaultFlow;
            workFlowTransactionBean.setValidator(defaulFlowValidator);
            workFlowTransactionBean.setDefaultLiteViewFlow(true);

        }

        if (input.isOfflineFetchPayApi()) {
            workflow = offlineFlow;
            workFlowTransactionBean.setValidator(fetchPayOptionValidator);
            // setting this flag as false since request type would be
            // default and as per previous check it will be set to true but in
            // offline we dont want to set it as true
            workFlowTransactionBean.setDefaultLiteViewFlow(false);
        }

        if (ERequestType.SUBSCRIBE.equals(input.getRequestType())) {
            workflow = taskedSubscriptionFlow;
            workFlowTransactionBean.setValidator(subscriptionFlowValidator);

        }

        if (input.getRequestType() != null
                && (ERequestType.isNativeOrUniOrNativeMFOrNativeSTRequest(input.getRequestType().getType()) || (input
                        .getSubRequestType() != null && ERequestType.isNativeOrUniOrNativeMFOrNativeSTRequest(input
                        .getSubRequestType().getType())))) {
            workflow = nativeFetchPayOptionFlow;
            workFlowTransactionBean.setValidator(nativeFetchPayOptionValidator);
            workFlowTransactionBean.setPostConvenienceFeeModel(input.isPostConvenience());
        }

        if (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(input.getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.equals(input.getRequestType())) {
            if (input.isEnhancedCashierPageRequest()) {
                workflow = nativeEnhancedSubscriptionFetchPayOptionFlow;
            } else {
                workflow = nativeSubscriptionFetchPayOptions;
            }
            workFlowTransactionBean.setValidator(nativeFetchPayOptionValidator);
            workFlowTransactionBean.setPostConvenienceFeeModel(input.isPostConvenience());
        } else if (input.isEnhancedCashierPageRequest()) {
            workflow = nativeEnhancedFlow;
            workFlowTransactionBean.setValidator(nativeFetchPayOptionValidator);
            workFlowTransactionBean.setPostConvenienceFeeModel(input.isPostConvenience());
        }

        return workflow;
    }
}
