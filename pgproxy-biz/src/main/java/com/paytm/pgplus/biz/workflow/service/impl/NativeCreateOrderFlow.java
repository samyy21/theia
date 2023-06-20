package com.paytm.pgplus.biz.workflow.service.impl;

//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service("nativeCreateOrderFlow")
public class NativeCreateOrderFlow implements Serializable, IWorkFlow {

    private static final long serialVersionUID = 2001096074219635955L;

    public static final Logger LOGGER = LoggerFactory.getLogger(NativeCreateOrderFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("defaultUserLoggedInValidator")
    private IValidator defaultUserLoggedInValidator;

    @Autowired
    @Qualifier("defaultUserNotLoggedInValidator")
    private IValidator defaultUserNotLoggedInValidator;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    MappingUtil mapUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean requestBean) {

        LOGGER.info("Initiating WorkFlow {}", "nativeCreateOrderFlow");

        /*
         * WorkFlowRequestBean Validation based on if the user is loggedin or
         * not
         */
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(requestBean,
                fetchValidator(requestBean));
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setPaymentPromoCheckoutDataPromoCode(requestBean.getPaymentPromoCheckoutDataPromoCode());
        workFlowTransBean.setWorkFlowBean(requestBean);
        workFlowTransBean.setPostConvenienceFeeModel(requestBean.isPostConvenience());

        if (ERequestType.isSubscriptionCreationRequest(workFlowTransBean.getWorkFlowBean().getRequestType().getType())) {
            StringBuilder key = new StringBuilder(workFlowTransBean.getWorkFlowBean().getRequestType().getType())
                    .append(workFlowTransBean.getWorkFlowBean().getTxnToken());
            if (requestBean.isFromAoaMerchant()) {
                LOGGER.error("AOA subscription client call is being used");
                // AoaSubscriptionCreateResponse aoaSubscriptionCreateResponse =
                // (AoaSubscriptionCreateResponse)
                // theiaTransactionalRedisUtil.get(
                // key.toString());
                //
                // if (aoaSubscriptionCreateResponse == null) {
                // LOGGER.error("Exception occurred while fetching subscription details ");
                // return new
                // GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                // ResponseConstants.SESSION_EXPIRY);
                // }
                // SubscriptionResponse subscriptionResponse = new
                // SubscriptionResponse();
                // mapUtils.mapAOASubsResponseIntoSubsResponse(aoaSubscriptionCreateResponse,
                // subscriptionResponse);
                // workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            } else {
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                        .toString());

                if (subscriptionResponse == null) {
                    LOGGER.error("Exception occurred while fetching subscription details ");
                    return new GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                            ResponseConstants.SESSION_EXPIRY);
                }
                workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            }
            // required at the time of activate subscription in ppp.
            workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
        }

        /*
         * Get and Set UserDetails
         */
        GenericCoreResponseBean<WorkFlowResponseBean> userDetailsError = setUserDetails(workFlowTransBean, requestBean);
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

        /* Create Order */
        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Create Order Failed, {}", createOrderResponse.getFailureMessage());
            if (createOrderResponse.getResponseConstant().equals(ResponseConstants.SUCCESS_IDEMPOTENT_ERROR)) {
                // LOGGER.error("Success Idempotent Error in Create order reason : {}",
                // createOrderResponse.getFailureMessage());
                return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                        createOrderResponse.getAcquirementId(), createOrderResponse.getResponseConstant());
            }
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());

        LOGGER.info("Returning Response Bean From {}, transId : {} ", "nativeCreateOrderFlow",
                responseBean.getTransID());

        return new GenericCoreResponseBean<>(responseBean);
    }

    private IValidator fetchValidator(WorkFlowRequestBean workFlowRequestBean) {
        if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
            return defaultUserLoggedInValidator;
        }
        return defaultUserNotLoggedInValidator;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> setUserDetails(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getUserDetailsBiz() != null) {
            /*
             * This is done so that oauth is not called again, this happens for
             * enhancedNative
             */
            workFlowHelper.fetchSavedCardsForUserDetail(flowRequestBean, flowRequestBean.getUserDetailsBiz(), false);
            workFlowTransBean.setUserDetails(flowRequestBean.getUserDetailsBiz());
            workFlowHelper.updatePostpaidStatusAndCCEnabledFlag(flowRequestBean.getToken(),
                    flowRequestBean.isPostpaidOnboardingSupported(), flowRequestBean.getOauthClientId(),
                    flowRequestBean.getOauthSecretKey(), flowRequestBean.getUserDetailsBiz());
            return null;
        } else if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            /* fetch user details with fetchSavedCards=false */
            GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                    flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
        }

        return null;
    }
}
