package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.paymode.LoyaltyPointAutoDebitPayMode;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author manojpal
 */
@Service("autoDebitFlow")
public class AutoDebitFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("loyaltyPointAutoDebitPayMode")
    LoyaltyPointAutoDebitPayMode loyaltyPointAutoDebitPayMode;

    @Autowired
    @Qualifier("autoDebitRequestValidator")
    private IValidator autoDebitRequestValidator;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, autoDebitRequestValidator);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setFastForwardRequest(true);
        // If merchant sent token in request then fetch userDetails from OAuth
        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(
                    workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(),
                        ResponseConstants.INVALID_OFFLINE_REQUEST);
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            workFlowHelper.setLinkPaymentsUserDetails(workFlowTransBean);

        } else {
            return new GenericCoreResponseBean<>("Invalid request data", ResponseConstants.INVALID_OFFLINE_REQUEST);
        }
        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        // fetch pcf details for payment payMethod.
        if ((ERequestType.OFFLINE == flowRequestBean.getRequestType()
                || ERequestType.LINK_BASED_PAYMENT == flowRequestBean.getRequestType() || ERequestType.LINK_BASED_PAYMENT_INVOICE == flowRequestBean
                .getRequestType()) && flowRequestBean.isPostConvenience()) {
            GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, null);
            workFlowTransBean.setPostConvenienceFeeModel(true);
            workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
        }
        // Call channelAccountQuery and set points info in extendedInfo in case
        // of LOYALTY_POINT paymode
        /*
         * GenericCoreResponseBean<ChannelAccountQueryResponseBizBean>
         * channelAccountQueryResponse = loyaltyPointAutoDebitPayMode
         * .addLoyaltyPointsInfoInExtendedInfo(flowRequestBean, workFlowHelper,
         * workFlowTransBean);
         * 
         * if (channelAccountQueryResponse != null &&
         * !channelAccountQueryResponse.isSuccessfullyProcessed()) { return new
         * GenericCoreResponseBean
         * <>(channelAccountQueryResponse.getFailureDescription(),
         * ResponseConstants.LOYALTY_POINT_NOT_CONFIGURED); }
         */
        // Setting exchange rate and pointNumber in extendInfo for loyalty point
        loyaltyPointAutoDebitPayMode.addLoyaltyPointsInfoInExtInfoWithoutChannelAccQuery(workFlowTransBean);
        // Create Order And Pay
        if (StringUtils.isBlank(workFlowTransBean.getWorkFlowBean().getTransID())) {
            workFlowTransBean.getWorkFlowBean().setWillUserChange(false);
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);

            if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                    GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                            createOrderAndPayResponse.getFailureMessage(), ResponseConstants.INVALID_OFFLINE_REQUEST,
                            createOrderAndPayResponse.getRiskRejectUserMessage());
                    responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
                    return responseBean;
                } else {
                    return new GenericCoreResponseBean<>(createOrderAndPayResponse.getFailureMessage(),
                            createOrderAndPayResponse.getResponseConstant());
                }
            }
            workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
            if (createOrderAndPayResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(createOrderAndPayResponse.getResponse().getSecurityPolicyResult()
                        .getRiskResult());
            }
            workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
        } else {
            // Call Only Pay
            workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
            LOGGER.info("Pay called from AutoDebitFlow");

            final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
            if (!payResponse.isSuccessfullyProcessed()) {
                if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
                    GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                            payResponse.getFailureMessage(), ResponseConstants.INVALID_OFFLINE_REQUEST,
                            payResponse.getRiskRejectUserMessage());
                    responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                    return responseBean;
                } else {
                    return new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                            payResponse.getResponseConstant());
                }
            }
            workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            workFlowTransBean.setTransID(workFlowTransBean.getWorkFlowBean().getTransID());
        }
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = workFlowHelper
                .fetchPaymentStatus(workFlowTransBean);
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }
        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

        // Need to close order in case paymentStatus is FAIL
        if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
            LOGGER.error("Payment FAILED");
            /*
             * final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder
             * = workFlowHelper .closeOrder(workFlowTransBean); if
             * (!cancelOrder.isSuccessfullyProcessed()) {
             * LOGGER.error("Close/Cancel order failed due to : {}",
             * cancelOrder.getFailureMessage()); }
             */
            // Not closing order as we hasve to retry payment
        } else if (PaymentStatus.PROCESSING.toString().equals(paymentStatus)
                && flowRequestBean.isOfflineFastForwardRequest()) {

            PaymentStatus paymentStatusInt = workFlowHelper.triggerCloseOrderPulses(workFlowTransBean);
            LOGGER.info("PaymentStatus after close order : {}", paymentStatusInt);

            queryPayResultResponse.getResponse().setPaymentStatusValue(paymentStatusInt.name());
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
            workFlowTransBean.setPaymentDone(true);
        } else {
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
            workFlowTransBean.setPaymentDone(true);

        }

        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setWorkFlowRequestBean(flowRequestBean);

        LOGGER.debug("Returning Response Bean From {}, trans Id : {} ", "AutoDebitFlow",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

}
