/**
 * Subscription payment request without user token, Browser Call refer sequence
 * diagram - 42
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.IS_ORDER_ELIGIBLE_FOR_RETRY;

@Service("bankTransferWorkflow")
public class BankTransferWorkflow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(BankTransferWorkflow.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BankTransferWorkflow.class);

    @Autowired
    private IAcquiringOrder acquiringOrder;

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    NativeRetryPaymentFlowService nativeRetryPaymentFlowService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());

        boolean isOrderPresent = false;
        String cashierRequestId = null;
        String acquirementId = null;
        String paymentStatus = null;
        AcquirementStatusType acquirementStatus = null;

        if (flowRequestBean.getVanInfo() != null && flowRequestBean.getVanInfo().getCheckoutFlow() != null) {
            boolean isCreateOrderReqInCheckoutFlow = false;

            setCacheCardToken(workFlowTransBean);

            // consult fee
            if (flowRequestBean.isPostConvenience()) {
                GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, null);
                workFlowTransBean.setPostConvenienceFeeModel(true);
                workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            }

            // For TPV Failure
            if (flowRequestBean.getChannelInfo() != null
                    && (flowRequestBean.getChannelInfo().get("isTPVFailure") != null || flowRequestBean
                            .getChannelInfo().get("failure") != null)) {
                QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = null;
                try {
                    queryByMerchantTransIdResponse = queryByMerchantTransId(flowRequestBean);
                    if (isOrderPresent(queryByMerchantTransIdResponse)) {
                        workFlowTransBean.setTransID(queryByMerchantTransIdResponse.getBody().getAcquirementId());
                        workFlowTransBean.setCashierRequestId(getCashierReqId(queryByMerchantTransIdResponse));
                    } else {
                        isCreateOrderReqInCheckoutFlow = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred during queryByMerchantTransId ", e);
                    ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                            queryByMerchantTransIdResponse.getBody().getResultInfo().getResultCodeId());
                    return getQueryByMerchantTransIdFailureResponse(queryByMerchantTransIdResponse, responseConstants);
                }
            }
            if (isCreateOrderReqInCheckoutFlow) {
                LOGGER.info("calling createOrderAndPay for bank transfer checkout flow");
                GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
                GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
                /* COAP */
                GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse;
                try {
                    createOrderAndPayResponse = workFlowHelper.createOrderAndPay(workFlowTransBean);
                } catch (MerchantLimitBreachedException e) {
                    return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.MERCHANT_FAILURE_RESPONSE);
                }
                if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                    return getCreateOrderAndPayFailureResponse(createOrderAndPayResponse);
                }
                acquirementId = createOrderAndPayResponse.getResponse().getAcquirementId();
                cashierRequestId = createOrderAndPayResponse.getResponse().getCashierRequestId();
                updateTransBean(workFlowTransBean, cashierRequestId, acquirementId);

                /* PAY RESULT QUERY */
                queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
                if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                    return getPayResultQueryFailureResponse(queryPayResultResponse);
                }
                workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

                paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();
                if (!PaymentStatus.FAIL.equals(paymentStatus)) {
                    queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);

                    if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                        return getQueryByAcquirementIdFailureResponse(queryByAcquirementIdResponse);
                    }
                    workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                    workFlowTransBean.setPaymentDone(true);
                }
            } else {
                GenericCoreResponseBean<BizPayResponse> payResponse;
                try {
                    LOGGER.info("Pay called from ABankTransferWorkFlow");
                    payResponse = workFlowHelper.pay(workFlowTransBean);
                } catch (MerchantLimitBreachedException e) {
                    return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.MERCHANT_FAILURE_RESPONSE);
                }
                GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = nativeRetryPaymentFlowService
                        .validatePayAPIResponse(payResponse, flowRequestBean, workFlowTransBean);

                if (validatePayResponse != null) {
                    return validatePayResponse;
                }
                acquirementId = flowRequestBean.getTransID();
                if (payResponse != null && payResponse.getResponse() != null
                        && payResponse.getResponse().getCashierRequestID() != null) {
                    cashierRequestId = payResponse.getResponse().getCashierRequestID();
                } else {
                    cashierRequestId = (String) theiaSessionRedisUtil.hget(flowRequestBean.getTxnToken(),
                            "cashierRequestId");
                }
                workFlowTransBean.setCashierRequestId(cashierRequestId);

                GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = workFlowHelper
                        .fetchBankForm(workFlowTransBean);
                if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                    return getPayResultQueryFailureResponse(queryPayResultResponse);
                }
                workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

                updateTransBean(workFlowTransBean, cashierRequestId, acquirementId);

                if (workFlowTransBean.isPaymentDone()) {
                    GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
                    queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                    if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                        return getQueryByAcquirementIdFailureResponse(queryByAcquirementIdResponse);
                    }
                    workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                }
            }

            final WorkFlowResponseBean workFlowResponseBean = getWorkFlowResponseBean(workFlowTransBean);

            LOGGER.info("Returning Response Bean From BankTransferWorkFlow, trans Id : {} ",
                    workFlowResponseBean.getTransID());
            return new GenericCoreResponseBean<>(workFlowResponseBean);
        }

        /* QUERY BY MERCHANT TRANS ID */
        QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = null;
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        GenericCoreResponseBean<BizCancelOrderResponse> closeOrderResponse = null;
        try {
            queryByMerchantTransIdResponse = queryByMerchantTransId(flowRequestBean);
            if (isOrderPresent(queryByMerchantTransIdResponse)) {
                isOrderPresent = true;
                acquirementId = queryByMerchantTransIdResponse.getBody().getAcquirementId();
                cashierRequestId = getCashierReqId(queryByMerchantTransIdResponse);

                paymentStatus = getPaymentStatus(queryByMerchantTransIdResponse);
                acquirementStatus = queryByMerchantTransIdResponse.getBody().getStatusDetail().getAcquirementStatus();
                updateTransBean(workFlowTransBean, cashierRequestId, acquirementId);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred during queryByMerchantTransId ", e);
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    queryByMerchantTransIdResponse.getBody().getResultInfo().getResultCodeId());
            return getQueryByMerchantTransIdFailureResponse(queryByMerchantTransIdResponse, responseConstants);
        }

        if (isOrderPresent) {

            boolean isRetryEligible = isOrderEligibleForRetry(flowRequestBean.getOrderID());

            if ((PaymentStatus.FAIL.toString().equals(paymentStatus) || (isRetryEligible && null == paymentStatus))
                    && AcquirementStatusType.INIT.equals(acquirementStatus)) {

                /* CACHE CARD TOKEN */
                setCacheCardToken(workFlowTransBean);

                GenericCoreResponseBean<BizPayResponse> payResponse;

                /* PAY */
                try {
                    payResponse = workFlowHelper.pay(workFlowTransBean);
                } catch (MerchantLimitBreachedException e) {
                    return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.MERCHANT_FAILURE_RESPONSE);
                }
                GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = nativeRetryPaymentFlowService
                        .validatePayAPIResponse(payResponse, flowRequestBean, workFlowTransBean);

                if (validatePayResponse != null) {
                    return validatePayResponse;
                }

                cashierRequestId = payResponse.getResponse().getCashierRequestID();
                workFlowTransBean.setCashierRequestId(cashierRequestId);
            }
            /* PAY RESULT QUERY */
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return getPayResultQueryFailureResponse(queryPayResultResponse);
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            /* QUERY by ACQUIREMENT ID */
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return getQueryByAcquirementIdFailureResponse(queryByAcquirementIdResponse);
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
            workFlowTransBean.setPaymentDone(true);
        } else {
            /* CACHE CARD TOKEN */
            setCacheCardToken(workFlowTransBean);

            /* COAP */
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse;
            try {
                createOrderAndPayResponse = workFlowHelper.createOrderAndPay(workFlowTransBean);
            } catch (MerchantLimitBreachedException e) {
                return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.MERCHANT_FAILURE_RESPONSE);
            }
            if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                return getCreateOrderAndPayFailureResponse(createOrderAndPayResponse);
            }
            acquirementId = createOrderAndPayResponse.getResponse().getAcquirementId();
            cashierRequestId = createOrderAndPayResponse.getResponse().getCashierRequestId();
            updateTransBean(workFlowTransBean, cashierRequestId, acquirementId);

            /* PAY RESULT QUERY */
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return getPayResultQueryFailureResponse(queryPayResultResponse);
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

            /* QUERY BY ACQUIREMENT ID */
            if (PaymentStatus.FAIL.name().equals(paymentStatus)) {
                /* Closing Order in FAIL payment CASE */
                if (queryPayResultResponse.getResponse() != null
                        && TheiaConstant.ExtraConstants.DEFAULT_FALLBACK_INST_ERROR_CODE.equals(queryPayResultResponse
                                .getResponse().getInstErrorCode())) {
                    LOGGER.info("Closing order received error code:{}", queryPayResultResponse.getResponse()
                            .getInstErrorCode());
                    closeOrderResponse = workFlowHelper.closeOrder(workFlowTransBean);
                }
                if (closeOrderResponse != null && !closeOrderResponse.isSuccessfullyProcessed()) {
                    LOGGER.info("Error in closing order:{}", closeOrderResponse.getFailureMessage());
                }
                workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            } else {
                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return getQueryByAcquirementIdFailureResponse(queryByAcquirementIdResponse);
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }
        }
        updateTransBean(workFlowTransBean, cashierRequestId, acquirementId);

        final WorkFlowResponseBean workFlowResponseBean = getWorkFlowResponseBean(workFlowTransBean);

        LOGGER.info("Returning Response Bean From BankTransferWorkFlow, trans Id : {} ",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private boolean isOrderPresent(QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {
        return null != queryByMerchantTransIdResponse && null != queryByMerchantTransIdResponse.getBody()
                && "S".equalsIgnoreCase(queryByMerchantTransIdResponse.getBody().getResultInfo().getResultStatus());
    }

    private void updateTransBean(WorkFlowTransactionBean workFlowTransBean, String cashierRequestId,
            String acquirementId) {
        workFlowTransBean.setCashierRequestId(cashierRequestId);
        workFlowTransBean.setTransID(acquirementId);
    }

    private WorkFlowResponseBean getWorkFlowResponseBean(WorkFlowTransactionBean workFlowTransBean) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setIdempotent(workFlowTransBean.isIdempotent());
        return workFlowResponseBean;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> getCreateOrderAndPayFailureResponse(
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse) {
        LOGGER.error("CreateOrderAndPay failed due to : {}", createOrderAndPayResponse.getFailureMessage());

        if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
            GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                    createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant(),
                    createOrderAndPayResponse.getRiskRejectUserMessage());
            responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
            return responseBean;
        }
        GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant());
        return responseBean;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> getQueryByAcquirementIdFailureResponse(
            GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse) {
        return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                queryByAcquirementIdResponse.getResponseConstant());
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> getPayResultQueryFailureResponse(
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                queryPayResultResponse.getResponseConstant());
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> getQueryByMerchantTransIdFailureResponse(
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse, ResponseConstants responseConstants) {
        return new GenericCoreResponseBean<>(queryByMerchantTransIdResponse.getBody().getResultInfo().getResultMsg(),
                responseConstants);
    }

    private String getPaymentStatus(QueryByMerchantTransIdResponse response) {
        if (CollectionUtils.isEmpty(response.getBody().getPaymentViews()))
            return null;

        String paymentStatus = null;
        for (PaymentView paymentView : response.getBody().getPaymentViews()) {
            if (MapUtils.isNotEmpty(paymentView.getExtendInfo())) {
                paymentStatus = paymentView.getExtendInfo().get("paymentStatus");
            }
        }
        return paymentStatus;
    }

    private String getCashierReqId(QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {
        if (CollectionUtils.isEmpty(queryByMerchantTransIdResponse.getBody().getPaymentViews()))
            return null;

        Optional<PaymentView> paymentView = queryByMerchantTransIdResponse.getBody().getPaymentViews().stream()
                .filter(v -> StringUtils.isNotBlank(v.getCashierRequestId())).findFirst();
        return paymentView.isPresent() ? paymentView.get().getCashierRequestId() : null;
    }

    private QueryByMerchantTransIdResponse queryByMerchantTransId(WorkFlowRequestBean flowRequestBean)
            throws FacadeCheckedException {
        String alipayId = flowRequestBean.getAlipayMID();
        String orderId = flowRequestBean.getOrderID();
        QueryByMerchantTransIdRequestBody requestBody = new QueryByMerchantTransIdRequestBody(alipayId, orderId, true);
        QueryByMerchantTransIdRequest request = new QueryByMerchantTransIdRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), requestBody);
        Routes routes = workFlowRequestCreationHelper.getRoute(flowRequestBean,
                ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID.getApi());
        if (null != routes && Routes.PG2.equals(routes)) {
            request.getBody().setRoute(routes);
            request.getHead().setMerchantId(flowRequestBean.getPaytmMID());
        }
        QueryByMerchantTransIdResponse response = acquiringOrder.queryByMerchantTransId(request);
        return response;
    }

    private void setCacheCardToken(WorkFlowTransactionBean workFlowTransBean) {

        WorkFlowRequestBean workFlowRequestBean = workFlowTransBean.getWorkFlowBean();
        if (StringUtils.isBlank(workFlowRequestBean.getAccountNumber())
                || StringUtils.isBlank(workFlowRequestBean.getIfsc())) {
            LOGGER.info("CacheCard skipped as remitter details were absent");
            return;
        }

        try {
            GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.MANDATE);

            if (!cacheCardResponse.isSuccessfullyProcessed()) {
                LOGGER.error("CacheCard failed");
                return;
            }
            workFlowTransBean.setCacheCardToken(cacheCardResponse.getResponse().getTokenId());
        } catch (Exception e) {
            LOGGER.error("CacheCard failed");
        }
    }

    private boolean isOrderEligibleForRetry(String orderId) {
        try {
            String eligibleOrdersForRetry = ff4jUtils.getPropertyAsStringWithDefault(IS_ORDER_ELIGIBLE_FOR_RETRY,
                    StringUtils.EMPTY);
            if (null == orderId || null == eligibleOrdersForRetry)
                return false;
            if (eligibleOrdersForRetry.equalsIgnoreCase("ALL")) {
                return true;
            } else {
                List<String> eligibleOrdersList = new ArrayList<>(Arrays.asList(eligibleOrdersForRetry.split(",")));
                return eligibleOrdersList.contains(orderId);
            }
        } catch (Exception e) {
            EXT_LOGGER.error("error occurred while fetching ff4j property {} ", e);
            return false;
        }
    }
}
