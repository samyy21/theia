package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.taskengine.task.impl.FetchWalletBalanceTask;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.facade.risk.services.IRisk;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.payloadvault.subscription.response.ListSubscriptionResponse;
import org.apache.commons.lang3.StringUtils;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import org.apache.commons.lang3.StringUtils;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * Created by rahulverma on 8/9/17.
 */
@Deprecated
@Service("userLoggedInLitePayviewConsultWorkflow")
public class UserLoggedInLitePayviewConsultWorkflow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserLoggedInLitePayviewConsultWorkflow.class);

    @Autowired
    @Qualifier("userLoggedInLitePayviewConsultValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("fetchWalletBalanceTask")
    private FetchWalletBalanceTask walletTask;

    @Autowired
    @Qualifier("mapUtilsBiz")
    MappingUtil mappingUtil;

    @Autowired
    IRisk riskImpl;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Initiating {} workflow.", "UserLoggedInLitePayviewConsultWorkflow");

        // Request Bean Validation
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);

        // to handle without order id flow in native fetch options
        if (ERequestType.NATIVE_PAY.equals(flowRequestBean.getRequestType())
                && requestBeanValidationResult.getResponseConstant() == ResponseConstants.INVALID_ORDER_ID) {
            requestBeanValidationResult = workFlowHelper.specificBeanValidation(flowRequestBean, validatorService);
        }

        if (!requestBeanValidationResult.isSuccessfullyProcessed()
                && requestBeanValidationResult.getResponseConstant() != ResponseConstants.INVALID_ORDER_ID) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setPostConvenienceFeeModel(flowRequestBean.isPostConvenience());

        /*
         * Get and Set UserDetails
         */
        GenericCoreResponseBean<WorkFlowResponseBean> userDetailsError = setUserDetails(workFlowTransBean,
                flowRequestBean);
        /*
         * if userDetailsError is not null, it means Pay API was not processed
         * successfully, so return the error received
         */
        if (userDetailsError != null) {
            return userDetailsError;
        }

        if (null != workFlowTransBean.getWorkFlowBean().getExtendInfo() && null != workFlowTransBean.getUserDetails()) {
            workFlowTransBean.getWorkFlowBean().getExtendInfo()
                    .setPaytmUserId(workFlowTransBean.getUserDetails().getUserId());
        }

        // filter saved cards specific for subscription
        if (flowRequestBean.isSubscription()) {
            workFlowHelper.filterSavedCardsForSubscription(workFlowTransBean);
        }

        // Consult PayView
        final GenericCoreResponseBean<LitePayviewConsultResponseBizBean> merchantConsultPayView = workFlowHelper
                .litePayviewConsult(workFlowTransBean);

        if (!merchantConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(merchantConsultPayView.getFailureMessage(),
                    merchantConsultPayView.getResponseConstant());
        }
        workFlowTransBean.setMerchantLiteViewConsult(merchantConsultPayView.getResponse());

        // set merchant-view-consult
        workFlowTransBean.setMerchantViewConsult(mappingUtil.getMerchantConsultPayViewBean(merchantConsultPayView)
                .getResponse());

        if (ERequestType.NATIVE_PAY.equals(workFlowTransBean.getWorkFlowBean().getRequestType())) {
            workFlowHelper.fetchWalletBalanceResponse(workFlowTransBean);
            workFlowHelper.fetchAndSetMgvBalance(workFlowTransBean);
        }

        if (flowRequestBean.isSubscription()) {
            workFlowHelper.filterOperationsLitePayView(workFlowTransBean, true);
        }

        // Fetch PayMode(AddAndPay or Hybrid)
        final EPayMode allowedPayMode = getAllowedPayMode(workFlowTransBean);

        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> addAndPayLitePayviewConsultResponse;

        if (EPayMode.ADDANDPAY.equals(allowedPayMode)) {
            // Consult View for AddAndPay
            addAndPayLitePayviewConsultResponse = workFlowHelper.litePayviewConsultForAddAndPay(workFlowTransBean);
            if (!addAndPayLitePayviewConsultResponse.isSuccessfullyProcessed()) {
                LOGGER.error("Consult For AddAndPay View failed due to ::{}",
                        addAndPayLitePayviewConsultResponse.getFailureMessage());
            }

            workFlowTransBean.setAddAndPayLiteViewConsult(addAndPayLitePayviewConsultResponse.getResponse());

            // set add-merchant-view-consult
            workFlowTransBean.setAddAndPayViewConsult(mappingUtil.getMerchantConsultPayViewBean(
                    addAndPayLitePayviewConsultResponse).getResponse());

            if (flowRequestBean.isSubscription()) {
                workFlowHelper.filterOperationsLitePayView(workFlowTransBean, false);
            }
        }

        /*
         * // Consult Fee Response if (flowRequestBean.isPostConvenience()) {
         * final GenericCoreResponseBean<ConsultFeeResponse> consultFee =
         * workFlowHelper .consultBulkFeeResponse(workFlowTransBean); if
         * (!consultFee.isSuccessfullyProcessed()) {
         * LOGGER.error("Consult Fee Failed due to :::{}",
         * consultFee.getFailureMessage()); return new
         * GenericCoreResponseBean<>(consultFee.getFailureMessage(),
         * consultFee.getResponseConstant()); }
         * workFlowTransBean.setConsultFeeResponse(consultFee.getResponse()); }
         */
        // fetch sarvatra vpa if for upi-paymode
        boolean isSavedVpaCheckOverride = false;
        if (flowRequestBean.getPaymentRequestBean() != null) {
            isSavedVpaCheckOverride = workFlowHelper.isSavedVpaCheckOverride(flowRequestBean.getPaymentRequestBean()
                    .getAppVersion());
        }
        if (workFlowHelper.checkExpressEnabledInNative(workFlowTransBean)
                || flowRequestBean.isEnhancedCashierPageRequest() || isSavedVpaCheckOverride) {
            GenericCoreResponseBean<UserProfileSarvatra> sarvatraResponse = workFlowHelper
                    .fetchUserProfileFromSarvatra(workFlowTransBean);
            if (sarvatraResponse.isSuccessfullyProcessed() && null != sarvatraResponse.getResponse()) {
                workFlowTransBean.setSarvatraUserProfile(sarvatraResponse.getResponse());
                workFlowTransBean.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(sarvatraResponse.getResponse()
                        .getResponse()));
            }
        }

        // Filter Saved cards based on final lite payview consult
        workFlowHelper.filterSavedCardsForLitePayviewConsult(workFlowTransBean);

        // Fetch active subscriptions to use as payment mode
        if (ERequestType.NATIVE_MF_PAY.equals(workFlowTransBean.getWorkFlowBean().getRequestType())) {
            workFlowHelper.processToFetchActiveSubscriptions(workFlowTransBean);
        }

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(
                        workFlowTransBean.getWorkFlowBean().getPaytmMID()) && !flowRequestBean.isGvFlag()
                && !flowRequestBean.isTransitWallet()) {
            workFlowHelper.checkForRiskConsultForAddMoney(flowRequestBean, workFlowTransBean, responseBean);
        }
        if (ERequestType.NATIVE_PAY.equals(workFlowTransBean.getWorkFlowBean().getRequestType())) {
            responseBean.setExtendedInfo(AlipayRequestUtils.getExtendeInfoMap(workFlowTransBean.getWorkFlowBean()
                    .getExtendInfo()));
        }

        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayLiteViewResponse(workFlowTransBean.getAddAndPayLiteViewConsult());
        responseBean.setMerchnatLiteViewResponse(workFlowTransBean.getMerchantLiteViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        responseBean.setAddMoneyDestination(workFlowTransBean.getAddMoneyDestination());

        // Set vpa details in work-flow-request-bean
        responseBean.setSarvatraUserProfile(workFlowTransBean.getSarvatraUserProfile());
        responseBean.setSarvatraVpa(workFlowTransBean.getSarvatraVpa());
        responseBean.setMerchantUpiPushEnabled(workFlowTransBean.isMerchantUpiPushEnabled());
        responseBean.setMerchantUpiPushExpressEnabled(workFlowTransBean.isMerchantUpiPushExpressEnabled());
        responseBean.setAddUpiPushEnabled(workFlowTransBean.isAddUpiPushEnabled());
        responseBean.setAddUpiPushExpressEnabled(workFlowTransBean.isAddUpiPushExpressEnabled());
        responseBean.setActiveSubscriptions(workFlowTransBean.getActiveSubscriptions());

        LOGGER.info("Returning Response Bean From UserLoggedInLitePayviewConsultWorkflow");
        return new GenericCoreResponseBean<>(responseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> setUserDetails(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getUserDetailsBiz() != null) {
            /*
             * This is done so that oauth is not called again, this happens for
             * enhancedNative
             */
            workFlowHelper.fetchSavedCardsForUserDetail(flowRequestBean, flowRequestBean.getUserDetailsBiz(), true);
            workFlowTransBean.setUserDetails(flowRequestBean.getUserDetailsBiz());
            workFlowHelper.updatePostpaidStatusAndCCEnabledFlag(flowRequestBean.getToken(),
                    flowRequestBean.isPostpaidOnboardingSupported(), flowRequestBean.getOauthClientId(),
                    flowRequestBean.getOauthSecretKey(), flowRequestBean.getUserDetailsBiz());
            return null;
        } else if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            /* fetch user details with fetchSavedCards=true */
            GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                    flowRequestBean.getToken(), true);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
        }

        return null;
    }

    private EPayMode getAllowedPayMode(WorkFlowTransactionBean workFlowTransBean) {
        if (StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getTxnAmount())) {
            return workFlowHelper.allowedPayMode(workFlowTransBean);
        } else {// TODO:No KYC Info support chek for solution
            return workFlowHelper.allowedPayModeForNoTxnAmt(workFlowTransBean);
        }
    }

}
