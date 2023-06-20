/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
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
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author manojpal
 *
 */
@Service("seamlessACSWorkflow")
public class SeamlessACSWorkflow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessACSWorkflow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("seamlessACSRequestValidator")
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

        // Cache the card info, and get back
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)) {
            final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, true);
            if (!createCacheCardResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(createCacheCardResponse.getFailureMessage(),
                        createCacheCardResponse.getResponseConstant());
            }
            workFlowTransBean.setCacheCardToken(createCacheCardResponse.getResponse().getTokenId());
        }
        LOGGER.info("createOrderAndPay called from SeamlessACSWorkflow");
        // Create Order And Pay
        final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                .createOrderAndPay(workFlowTransBean);
        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
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

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
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
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())
                    || StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                    closeOrder(workFlowTransBean);
                return new GenericCoreResponseBean<>(ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.getMessage(),
                        ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED, queryPayResultResponse.getRetryStatus());
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

        LOGGER.info("Returning Response Bean From SeamlessACSWorkflow, trans Id : {} ",
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