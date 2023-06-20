/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_SUCCESS_CODE;

/*
 * This class Maps MID , fetch UserDetails to check if token is valid ,
 * CreateOrder , ConsultView
 */

@Service("subscriptionFlowUserNotLoggedIn")
public class SubscriptionFlowUserNotLoggedIn implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionFlowUserNotLoggedIn.class);

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

        final SubscriptionResponse freshSubscriptionResponse = workFlowHelper
                .processFreshSubscriptionContrat(workFlowTransBean);

        if (freshSubscriptionResponse == null || StringUtils.isBlank(freshSubscriptionResponse.getRespCode())) {
            LOGGER.error("Subscription Contract Creation failed due to null response from subscription service");
            return new GenericCoreResponseBean<>("Null Response", ResponseConstants.SYSTEM_ERROR);
        }

        if (!freshSubscriptionResponse.getRespCode().equals(SUBSCRIPTION_SUCCESS_CODE)) {
            LOGGER.error("Subscription Contract Creation failed due to ::{}", freshSubscriptionResponse.getRespMsg());
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

        final GenericCoreResponseBean<MidCustIdCardBizDetails> midCustIdCardDetails = workFlowHelper
                .fetchSavedCards(workFlowTransBean);

        if (midCustIdCardDetails.isSuccessfullyProcessed()) {
            workFlowTransBean.setMidCustIdCardBizDetails(midCustIdCardDetails.getResponse());
        }
        workFlowHelper.filterOperationsForUserNotLoggedIn(workFlowTransBean, false);

        workFlowHelper.filterSavedCardsUserNotLogged(workFlowTransBean);
        // Filtering saved cards specifically for those where subscription is
        // allowed
        workFlowHelper.filterSavedCardsForSubscriptionNotLoggedIn(workFlowTransBean);

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setSubscriptionID(workFlowTransBean.getSubscriptionServiceResponse().getSubscriptionId());
        responseBean.setSubsType(workFlowTransBean.getWorkFlowBean().getSubsTypes());
        if (workFlowTransBean.getMidCustIdCardBizDetails() != null) {
            responseBean.setmIdCustIdCardBizDetails(workFlowTransBean.getMidCustIdCardBizDetails());
        }
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SubscriptionFlowUserNotLoggedIn",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}