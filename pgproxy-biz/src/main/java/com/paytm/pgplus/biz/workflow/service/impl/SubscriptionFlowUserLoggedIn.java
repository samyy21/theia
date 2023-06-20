/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_SUCCESS_CODE;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;

/*
 * This class Maps MID , fetch UserDetails to check if token is valid ,
 * CreateOrder , ConsultView
 */

@Service("subscriptionFlowUserLoggedIn")
public class SubscriptionFlowUserLoggedIn implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionFlowUserLoggedIn.class);

    @Autowired
    @Qualifier("subscriptionFreshCCOnlyPreValidator")
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

        // fetch UserDetails
        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), true);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        // Set user detailsh
        workFlowTransBean.setUserDetails(userDetails.getResponse());
        workFlowHelper.filterSavedCardsForSubscription(workFlowTransBean);

        final SubscriptionResponse freshSubscriptionResponse = workFlowHelper
                .processFreshSubscriptionContrat(workFlowTransBean);
        if (!freshSubscriptionResponse.getRespCode().equals(SUBSCRIPTION_SUCCESS_CODE)) {
            LOGGER.error("Subscription Contract Creation failed due to : {}", freshSubscriptionResponse.getRespMsg());
            return new GenericCoreResponseBean<>(freshSubscriptionResponse.getRespMsg(), ResponseConstants.SYSTEM_ERROR);
        }
        workFlowTransBean.setSubscriptionServiceResponse(freshSubscriptionResponse);
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setSubscriptionId(freshSubscriptionResponse.getSubscriptionId());
        if (StringUtils.isNotBlank(flowRequestBean.getSubsServiceID())) {
            workFlowTransBean.getWorkFlowBean().getExtendInfo().setSubsServiceId(flowRequestBean.getSubsServiceID());
        }

        // Create Order
        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultPayView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(merchantConsultPayView.getFailureMessage(),
                    merchantConsultPayView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultPayView.getResponse());
        workFlowHelper.filterOperations(workFlowTransBean, true);

        // Fetch PayMode(AddAndPay or Hybrid)
        EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);
        if (EPayMode.HYBRID.equals(allowedPayMode)) {
            allowedPayMode = EPayMode.NONE;
        }
        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponse = null;
        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // Consult View for AddAndPay
            addAndPayConsultViewResponse = workFlowHelper.consultPayView(workFlowTransBean, true);
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponse.getResponse());
            workFlowHelper.filterOperations(workFlowTransBean, false);
        }

        if (workFlowHelper.paymentsBankAllowed(workFlowTransBean)) {
            // Invoke BMW API
            GenericCoreResponseBean<AccountBalanceResponse> bankResponse = workFlowHelper
                    .fetchAccountBalance(workFlowTransBean);
            if (bankResponse.isSuccessfullyProcessed() && bankResponse.getResponse() != null) {
                workFlowTransBean.setAccountBalanceResponse(bankResponse.getResponse());
            }
        }
        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setSubscriptionID(workFlowTransBean.getSubscriptionServiceResponse().getSubscriptionId());
        responseBean.setSubsType(workFlowTransBean.getWorkFlowBean().getSubsTypes());
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        responseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SubscriptionFlowUserLoggedIn",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}