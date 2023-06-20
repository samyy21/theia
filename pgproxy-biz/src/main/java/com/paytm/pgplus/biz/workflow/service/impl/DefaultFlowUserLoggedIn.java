/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("defaultLoggedInFlow")
public class DefaultFlowUserLoggedIn implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultFlowUserLoggedIn.class);

    @Autowired
    @Qualifier("defaultUserLoggedInValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Initiating {} workflow.", "DefaultFlowUserLoggedIn");

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
        boolean saveCardRequired = true;
        if (ERequestType.RESELLER.name().equals(flowRequestBean.getRequestType().name())) {
            saveCardRequired = false;
        }
        if (CollectionUtils.isNotEmpty(flowRequestBean.getDisabledPaymentModes())
                && flowRequestBean.getDisabledPaymentModes().contains(EPayMethod.SAVED_CARD.toString())) {
            saveCardRequired = false;
        }
        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), saveCardRequired);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        // Set user details
        workFlowTransBean.setUserDetails(userDetails.getResponse());

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

        // Fetch PayMode(AddAndPay or Hybrid)
        final EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);

        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponse;

        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // Consult View for AddAndPay
            addAndPayConsultViewResponse = workFlowHelper.consultPayView(workFlowTransBean, true);
            if (!addAndPayConsultViewResponse.isSuccessfullyProcessed()) {
                LOGGER.error("Consult For AddAndPay View failed due to ::{}",
                        addAndPayConsultViewResponse.getFailureMessage());
            }
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponse.getResponse());
        }

        if (workFlowHelper.paymentsBankAllowed(workFlowTransBean)
                && !flowRequestBean.getDisabledPaymentModes().contains(EPayMethod.PPBL.toString())) {
            // Invoke BMW API
            GenericCoreResponseBean<AccountBalanceResponse> bankResponse = workFlowHelper
                    .fetchAccountBalance(workFlowTransBean);
            if (bankResponse.isSuccessfullyProcessed() && bankResponse.getResponse() != null) {
                workFlowTransBean.setAccountBalanceResponse(bankResponse.getResponse());
            }
        }

        if (workFlowHelper.checkUPIPUSHEnabled(workFlowTransBean)) {
            GenericCoreResponseBean<UserProfileSarvatra> sarvatraResponse = workFlowHelper
                    .fetchUserProfileFromSarvatra(workFlowTransBean);
            if (sarvatraResponse.isSuccessfullyProcessed() && null != sarvatraResponse.getResponse()) {
                workFlowTransBean.setSarvatraUserProfile(sarvatraResponse.getResponse());
                workFlowTransBean.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(sarvatraResponse.getResponse()
                        .getResponse()));
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
        responseBean.setSarvatraUserProfile(workFlowTransBean.getSarvatraUserProfile());
        responseBean.setSarvatraVpa(workFlowTransBean.getSarvatraVpa());
        if (responseBean.getSarvatraUserProfile() != null) {
            responseBean.setMerchantUpiPushEnabled(workFlowTransBean.isMerchantUpiPushEnabled());
            responseBean.setMerchantUpiPushExpressEnabled(workFlowTransBean.isMerchantUpiPushExpressEnabled());
            responseBean.setAddUpiPushEnabled(workFlowTransBean.isAddUpiPushEnabled());
            responseBean.setAddUpiPushExpressEnabled(workFlowTransBean.isAddUpiPushExpressEnabled());
        }
        responseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        // responseBean.setPaytmVpaInfo(workFlowTransBean.getPaytmVpaInfo());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "DefaultFlowUserLoggedIn",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }

}