package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
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
 * @author Kunal Maini
 */

@Service("seamlessNBflow")
public class SeamlessNBFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessNBFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("seamlessNBvalidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @param flowRequestBean
     * @return
     */
    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

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

        if (flowRequestBean.isPostConvenience()) {
            if (!workFlowHelper.isSimplifiedFlow(workFlowTransBean.getWorkFlowBean())) {
                GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, null);
                workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            }
            workFlowTransBean.setPostConvenienceFeeModel(true);
        }

        // Create Order And Pay
        LOGGER.info("createOrderAndPay called from SeamlessNBFlow");

        final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                .createOrderAndPay(workFlowTransBean);
        Long startTime = System.currentTimeMillis();
        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
            if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                        createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant(),
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
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        // Fetch Bank Form using Query_PayResult API
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);

            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant(),
                        queryPayResultResponse.getRetryStatus());
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
                if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                    closeOrder(workFlowTransBean);
            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.info("Closing order due to webFormContext is blank");
                if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                    closeOrder(workFlowTransBean);
            }
        }

        // Setting data in workFlowResponse bean for processing
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SeamlessFlowService",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean, queryPayResultResponse.getRetryStatus());
    }

    private void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }
}