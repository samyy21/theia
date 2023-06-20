package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
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
import com.paytm.pgplus.facade.payment.models.response.UPIPushInitiateResponse;
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

/**
 * @author vivek kumar
 * @date 11-01-18
 *
 */
@Service("upiPushFlowService")
public class UPIPushFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(UPIPushFlow.class);

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

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

        // Setting consult Fee Response
        if (flowRequestBean.isPostConvenience()) {
            workFlowTransBean.setConsultFeeResponse(flowRequestBean.getConsultFeeResponse());
            workFlowTransBean.setPostConvenienceFeeModel(true);
        }
        // Hack for supporting UPI_PUSH_EXPRESS for ONLY_PG txn
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBizBean = workFlowHelper
                .getLitePayViewConsultResponse(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        if (litePayviewConsultResponseBizBean == null) {
            litePayviewConsultResponseBizBean = workFlowHelper.litePayviewConsult(workFlowTransBean, false);
            if (litePayviewConsultResponseBizBean != null
                    && !litePayviewConsultResponseBizBean.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(litePayviewConsultResponseBizBean.getFailureMessage(),
                        ResponseConstants.SYSTEM_ERROR);
            }
        }
        if (litePayviewConsultResponseBizBean != null) {
            LitePayviewConsultResponseBizBean litePayviewConsultResponse = litePayviewConsultResponseBizBean
                    .getResponse();
            if (workFlowHelper.checkUPIPushExpressEnabled(litePayviewConsultResponse.getPayMethodViews())) {
                LOGGER.info("UPI Push Express flow supported for this transaction, initiating the same");
                workFlowTransBean.getWorkFlowBean().setUpiPushExpressSupported(true);
            }
        }
        LOGGER.info("createOrderAndPay called from UPIPushFlow");
        // Create Order And Pay
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
                        createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant(),
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
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        // Fetch Bank Form using Query_PayResult API
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;

        if (!workFlowTransBean.getWorkFlowBean().isUpiPushExpressSupported()) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant());
                if (queryPayResultResponse.getResponse() != null) {
                    responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
                }
                return responseBean;
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                LOGGER.info("Closing order due to payment status : {}", queryPayResultResponse.getResponse()
                        .getPaymentStatusValue());
                closeOrder(workFlowTransBean);
            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.info("Closing order due to webFormContext is blank");
                closeOrder(workFlowTransBean);
            }

            // S2S Call to Instaproxy
            GenericCoreResponseBean<UPIPushInitiateResponse> upiPushInitiateResponse = workFlowHelper
                    .initiateUpiPushTransaction(workFlowTransBean);
            if (!upiPushInitiateResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(upiPushInitiateResponse.getFailureMessage(),
                        upiPushInitiateResponse.getResponseConstant());
            }
            LOGGER.info("Successfully made call to Bank Proxy for initiating UPI Push flow");
        }

        // Check payment status (Query pay result)
        queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }

        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

        if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
            closeOrder(workFlowTransBean);
        } else {
            // Check txn status (Query by acquirement id)
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
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

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "UpiPushFlowService",
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
