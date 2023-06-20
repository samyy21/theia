package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.CacheCardType;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by Naman on 29/06/17.
 */
@Service("stockTradingFlow")
public class StockTradingFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(StockTradingFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("stockTradingValidator")
    private IValidator validatorService;

    @Autowired
    private RedisUtil redisUtil;

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

        // Cache NB Info
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
            final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.STOCK_TRADING);
            if (!createCacheCardResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(createCacheCardResponse.getFailureMessage(),
                        createCacheCardResponse.getResponseConstant());
            }
            workFlowTransBean.setCacheCardToken(createCacheCardResponse.getResponse().getTokenId());
        }

        // Create Order And Pay
        LOGGER.info("createOrderAndPay called from StockTradinFlow");
        final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                .createOrderAndPay(workFlowTransBean);
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

        // Fetch Bank Form using Query_PayResult
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;

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
            closeOrder(workFlowTransBean);
        } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
            closeOrder(workFlowTransBean);
        }

        // Setting data in workFlowResponse bean for processing
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SeamlessFlowService",
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
