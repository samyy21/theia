package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.utils.NativeRetryPaymentUtil;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("upiPushExpressFlowService")
public class UPIPushExpressFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(UPIPushExpressFlow.class);

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("nativeSeamlessflowservice")
    private NativeSeamlessFlowService nativeSeamlessflowservice;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    NativeRetryPaymentFlowService nativeRetryPaymentFlowService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();

        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;
        // Fetch User Details
        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            flowRequestBean.setUserDetailsBiz(userDetails.getResponse());
            workFlowHelper.setLinkPaymentsUserDetails(workFlowTransBean);
        }

        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            StringBuilder key = new StringBuilder(flowRequestBean.getRequestType().getType()).append(flowRequestBean
                    .getTxnToken());
            SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                    .toString());
            workFlowHelper.cacheUPIRecurringInfo(flowRequestBean);

            if (subscriptionResponse == null) {
                LOGGER.error("Exception occurred while fetching subscription details ");
                return new GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                        ResponseConstants.SYSTEM_ERROR);
            }
            if (SubsPaymentMode.UNKNOWN.equals(subscriptionResponse.getPaymentMode())) {
                subscriptionResponse.setPaymentMode(flowRequestBean.getSubsPayMode());
            }
            workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            // required at the time of activate subscription in ppp.
            workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
        }
        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        // Setting consult Fee Response
        if (flowRequestBean.isPostConvenience()) {
            GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, null);
            workFlowTransBean.setPostConvenienceFeeModel(true);
            workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
        }

        /*
         * if TransId/AcquirementId is present, this means order has been
         * created for this payment, so now we'll call Pay API
         */
        if (StringUtils.isNotBlank(flowRequestBean.getTransID())) {

            /*
             * call pay API
             */
            LOGGER.info("pay from UPIPushExpressFlow");
            final GenericCoreResponseBean<BizPayResponse> payResponse = nativeRetryPaymentFlowService.callPayAPI(
                    flowRequestBean, workFlowTransBean);
            GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = nativeRetryPaymentFlowService
                    .validatePayAPIResponse(payResponse, flowRequestBean, workFlowTransBean);
            /*
             * if validatePayResponse is not null, it means Pay API was not
             * processed successfully, so return the error received
             */
            if (validatePayResponse != null) {
                return validatePayResponse;
            }
            nativeRetryPaymentFlowService.setCashierRequestIdInWorkFlowTransBean(payResponse, flowRequestBean,
                    workFlowTransBean);

        } else {
            // Create Order And Pay
            LOGGER.info("createOrderAndPay called from UPIPushExpressFlow");
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);
            if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                        && createOrderAndPayResponse.getResponseConstant() != null
                        && ResponseConstants.RISK_REJECT.equals(createOrderAndPayResponse.getResponseConstant())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(NativePaymentFailureType.RISK_REJECT);
                    if (StringUtils.isNotBlank(createOrderAndPayResponse.getAcquirementId())) {
                        nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(),
                                createOrderAndPayResponse.getAcquirementId());
                    }
                }
                if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                    GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                            createOrderAndPayResponse.getFailureMessage(),
                            createOrderAndPayResponse.getResponseConstant(),
                            createOrderAndPayResponse.getRiskRejectUserMessage());
                    responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
                    return responseBean;
                }
                return new GenericCoreResponseBean<>(createOrderAndPayResponse.getFailureMessage(),
                        createOrderAndPayResponse.getResponseConstant());
            }
            workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
            if (createOrderAndPayResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(createOrderAndPayResponse.getResponse().getSecurityPolicyResult()
                        .getRiskResult());
            }
            workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
        }
        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
            nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(), workFlowTransBean.getTransID());
        }
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        // Fetch Bank Form using Query_PayResult API
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;

        // Check payment status (Query pay result)
        queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);

        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }

        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

        if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
            LOGGER.info("Payment Status For UPI PUSH Express transaction : {}", queryPayResultResponse.getResponse()
                    .getPaymentStatusValue());
            if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                // Closing order after checking if retry is allowed
                nativeSeamlessflowservice.closeOrder(workFlowTransBean);
            } else
                closeOrder(workFlowTransBean);
        } else {
            // Check txn status (Query by acquirement id)
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
            }
            if (queryByAcquirementIdResponse.getResponse() != null) {
                queryByAcquirementIdResponse.getResponse().setMerchantTransId(flowRequestBean.getOrderID());
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
            workFlowTransBean.setPaymentDone(true);
        }

        // Setting final data into response bean
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        workFlowResponseBean.setWorkFlowRequestBean(flowRequestBean);

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "UpiPushExpressFlowService",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }

}
