package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdResponseBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequestBody;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponseBody;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.enums.CloseOrderStatus;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.CancelTransRequest;
import com.paytm.pgplus.theia.models.CancelTransResponse;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.services.ICloseOrderService;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.utils.RouterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author kartik
 * @date 06-07-2017
 */
@Service("closeOrderServiceImpl")
public class CloseOrderServiceImpl implements ICloseOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseOrderServiceImpl.class);

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("cancelTransactionFlow")
    private IWorkFlow cancelTransactionFlow;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private ICashier cashierImpl;

    @Autowired
    private FF4JHelper fF4JHelper;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RouterUtil routerUtil;

    @Override
    public CancelTransResponse processCancelOrderRequest(final CancelTransRequest cancelTransRequest,
            final EnvInfoRequestBean envInfo) {
        if (!validateCancelTransRequest(cancelTransRequest)) {
            return generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST);
        }

        MappingMerchantData merchantMapping = null;
        try {
            merchantMapping = merchantMappingService.getMappingMerchantData(cancelTransRequest.getMerchantId());
            if (merchantMapping == null) {
                return generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST);
            }
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("Exception occured while fetching merchant mapping {}", e);
            return generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST);
        }

        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        flowRequestBean.setOrderID(cancelTransRequest.getOrderId());
        flowRequestBean.setPaytmMID(cancelTransRequest.getMerchantId());
        flowRequestBean.setAlipayMID(merchantMapping.getAlipayId());
        flowRequestBean.setToken(cancelTransRequest.getUserToken());
        flowRequestBean.setOauthClientId(configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_ID)
                .getValue());
        flowRequestBean.setOauthSecretKey(configurationDataService.getPaytmProperty(
                ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue());
        flowRequestBean.setRoute(routerUtil.getRoute(cancelTransRequest.getMerchantId(),
                cancelTransRequest.getOrderId(), "CloseOrder"));

        WorkFlowTransactionBean flowTransBean = new WorkFlowTransactionBean();
        flowTransBean.setWorkFlowBean(flowRequestBean);

        if (StringUtils.isNotBlank(cancelTransRequest.getUserToken())) {
            GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(flowTransBean,
                    flowRequestBean.getToken(), false);

            if ((userDetails != null) && !userDetails.isSuccessfullyProcessed()) {
                LOGGER.error("Invalid sso token : {}", cancelTransRequest.getUserToken());
                return generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST);
            }
        }

        GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryMerchantTransIdResponse = workFlowHelper
                .queryByMerchantTransID(flowTransBean, true);

        if ((queryMerchantTransIdResponse != null) && queryMerchantTransIdResponse.isSuccessfullyProcessed()) {
            if (fF4JHelper.isFF4JFeatureForMidEnabled(ExtraConstants.EXEMPT_MID_LIST_FROM_CLOSE_ORDER,
                    cancelTransRequest.getMerchantId())) {
                return generateCloseOrderResponse(CloseOrderStatus.SUCCESS);
            }
            QueryByMerchantTransIDResponseBizBean responseBizBean = queryMerchantTransIdResponse.getResponse();
            boolean isMidExemptFromCloseOrder = fF4JHelper.isFF4JFeatureForMidEnabled(
                    ExtraConstants.EXEMPT_MID_LIST_FROM_CLOSE_ORDER_WITH_PENDING_STATUS,
                    cancelTransRequest.getMerchantId());
            if (!cancelTransRequest.isForceClose() || isMidExemptFromCloseOrder) {
                if (responseBizBean != null) {
                    if (responseBizBean.getStatusDetail() != null
                            && (responseBizBean.getStatusDetail().getAcquirementStatus() == AcquirementStatusType.SUCCESS || responseBizBean
                                    .getStatusDetail().getAcquirementStatus() == AcquirementStatusType.CLOSED)) {
                        return generateCloseOrderResponse(CloseOrderStatus.INVALID_ORDER_STATUS);
                    }
                    String txnToken = retryServiceHelper.getTxnToken(cancelTransRequest.getMerchantId(),
                            cancelTransRequest.getOrderId());
                    String cashierId = StringUtils.isNotBlank(txnToken) ? nativeSessionUtil
                            .getCashierRequestId(txnToken) : null;
                    if (cashierId == null) {
                        cashierId = redisUtil.getCashierIdForAcquirementId(responseBizBean.getAcquirementId());
                    }
                    if (cashierId != null) {
                        PayResultQueryResponse response = null;
                        try {
                            PayResultQueryRequest resultQueryRequest = new PayResultQueryRequest(
                                    RequestHeaderGenerator.getHeader(ApiFunctions.PAY_RESULT_QUERY),
                                    new PayResultQueryRequestBody(cashierId));
                            resultQueryRequest.getBody().setRoute(flowRequestBean.getRoute());
                            response = cashierImpl.payResultQuery(resultQueryRequest);
                            if (response != null && response.getBody() != null) {
                                PayResultQueryResponseBody responseBody = response.getBody();
                                if (responseBody.getPaymentStatus() != null
                                        && PaymentStatus.SUCCESS == responseBody.getPaymentStatus()) {
                                    return generateCloseOrderResponse(CloseOrderStatus.INVALID_ORDER_STATUS);
                                }
                                boolean isPending = checkPendingStatus(responseBody.getPaymentStatus());
                                if (isPending) {
                                    logPendingStatus(responseBizBean, cashierId, responseBody, isPending);
                                    if (isMidExemptFromCloseOrder) {
                                        return generateCloseOrderResponse(CloseOrderStatus.SUCCESS);
                                    }
                                    return generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR);
                                }
                            }
                        } catch (FacadeInvalidParameterException e) {
                            LOGGER.error(e.getMessage());
                        } catch (FacadeCheckedException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
            }
            // Acquiring order
            return closeAcquiringOrder(queryMerchantTransIdResponse.getResponse(), flowRequestBean);
        }

        // Fund Order
        flowRequestBean.setRoute(null);
        flowRequestBean.setTransType(ETransType.TOP_UP);
        return closeFundOrder(flowRequestBean, flowTransBean, envInfo);
    }

    private void logPendingStatus(QueryByMerchantTransIDResponseBizBean responseBizBean, String cashierId,
            PayResultQueryResponseBody responseBody, boolean isPending) {
        long timeDifference = new Date().getTime()
                - (responseBizBean.getTimeDetail() != null ? responseBizBean.getTimeDetail().getCreatedTime() != null ? responseBizBean
                        .getTimeDetail().getCreatedTime().getTime()
                        : 0
                        : 0);
        String payMethod1 = responseBody.getPayOptionInfos() != null && responseBody.getPayOptionInfos().size() > 0 ? responseBody
                .getPayOptionInfos().get(0) != null ? responseBody.getPayOptionInfos().get(0).getPayMethod() != null ? responseBody
                .getPayOptionInfos().get(0).getPayMethod().getMethod()
                : StringUtils.EMPTY
                : StringUtils.EMPTY
                : StringUtils.EMPTY;
        String payMethod2 = responseBody.getPayOptionInfos() != null && responseBody.getPayOptionInfos().size() > 1 ? responseBody
                .getPayOptionInfos().get(1) != null ? responseBody.getPayOptionInfos().get(1).getPayMethod() != null ? responseBody
                .getPayOptionInfos().get(1).getPayMethod().getMethod()
                : StringUtils.EMPTY
                : StringUtils.EMPTY
                : StringUtils.EMPTY;
        LOGGER.info(
                "CloseOrderService : Pending status for Cashier Request {} is {} , Amount : {}, TimeDifference between today and created time : {}, payMethod1 : {}, payMethod2 : {}",
                cashierId, isPending, responseBizBean.getAmountDetail() != null ? responseBizBean.getAmountDetail()
                        .getOrderAmount() : "", timeDifference, payMethod1, payMethod2);
    }

    /**
     * Checks payment status if it is PEnding or not based on QueryPaymentStatus
     * from P+. In case of SUCCESS it returns true.
     *
     * @param paymentStatus
     * @return
     */
    private boolean checkPendingStatus(PaymentStatus paymentStatus) {
        boolean pendingStatus = false;
        if (paymentStatus != null) {
            if (PaymentStatus.FAIL == paymentStatus) {
                pendingStatus = false;
            } else {
                pendingStatus = true;
            }
        }
        return pendingStatus;
    }

    private void createCloseOrderRequest(WorkFlowRequestBean flowRequestBean,
            QueryByMerchantTransIDResponseBizBean response) {
        flowRequestBean.setTransID(response.getAcquirementId());
        flowRequestBean.setCloseReason("User drop");
        flowRequestBean.setTransType(ETransType.ACQUIRING);
    }

    private void createTopUpCloseOrderRequest(WorkFlowRequestBean flowRequestBean,
            QueryByMerchantRequestIdResponseBizBean response, EnvInfoRequestBean envInfo) {
        flowRequestBean.setTransID(response.getFundOrderId());
        flowRequestBean.setEnvInfoReqBean(envInfo);
        flowRequestBean.setTransType(ETransType.TOP_UP);
    }

    private boolean validateCancelTransRequest(CancelTransRequest cancelTransRequest) {

        if (cancelTransRequest == null) {
            return false;
        }
        if (StringUtils.isBlank(cancelTransRequest.getMerchantId())) {
            return false;
        }
        if (StringUtils.isBlank(cancelTransRequest.getOrderId())) {
            return false;
        }
        return true;
    }

    public CancelTransResponse generateCloseOrderResponse(CloseOrderStatus result) {
        CancelTransResponse cancelTransResponse = new CancelTransResponse();
        cancelTransResponse.setStatus(result.getStatus());
        cancelTransResponse.setStatusMessage(result.getStatusMessage());
        cancelTransResponse.setStatusCode(result.getStatusCode());
        return cancelTransResponse;
    }

    private CancelTransResponse closeAcquiringOrder(
            QueryByMerchantTransIDResponseBizBean queryByMerchantTransIDResponseBizBean,
            WorkFlowRequestBean flowRequestBean) {
        createCloseOrderRequest(flowRequestBean, queryByMerchantTransIDResponseBizBean);
        GenericCoreResponseBean<WorkFlowResponseBean> errorResponse = cancelTransactionFlow.process(flowRequestBean);

        if ((errorResponse != null) && !errorResponse.isSuccessfullyProcessed()) {
            String errorMessage = "Could not cancel acquiring transaction because :: "
                    + errorResponse.getFailureMessage();
            LOGGER.error(errorMessage);
            if (ResponseConstants.ORDER_IS_CLOSED.equals(errorResponse.getResponseConstant())) {
                return generateCloseOrderResponse(CloseOrderStatus.ORDER_ALREADY_CLOSED);
            }
            if (ResponseConstants.ORDER_STATUS_INVALID.equals(errorResponse.getResponseConstant())) {
                return generateCloseOrderResponse(CloseOrderStatus.INVALID_ORDER_STATUS);
            }
            return generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR);
        }

        return generateCloseOrderResponse(CloseOrderStatus.SUCCESS);
    }

    private CancelTransResponse closeFundOrder(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean flowTransBean, EnvInfoRequestBean envInfo) {
        GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> queryMerchantRequestIdResponse = workFlowHelper
                .queryByMerchantRequestId(flowTransBean);
        if (queryMerchantRequestIdResponse == null) {
            LOGGER.error("Null response received for query by merchant request id");
            return generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR);
        }
        if (!queryMerchantRequestIdResponse.isSuccessfullyProcessed()) {
            if (ResponseConstants.TARGET_NOT_FOUND.equals(queryMerchantRequestIdResponse.getResponseConstant())) {
                return generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST);
            }
            return generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR);
        }
        QueryByMerchantRequestIdResponseBizBean response = queryMerchantRequestIdResponse.getResponse();
        createTopUpCloseOrderRequest(flowRequestBean, response, envInfo);
        GenericCoreResponseBean<WorkFlowResponseBean> errorResponse = cancelTransactionFlow.process(flowRequestBean);
        if ((errorResponse != null) && !errorResponse.isSuccessfullyProcessed()) {
            String errorMessage = "Could not cancel fund transaction because :: " + errorResponse.getFailureMessage();
            LOGGER.error(errorMessage);
            if (ResponseConstants.SUCCESS_IDEMPOTENT_ERROR.equals(errorResponse.getResponseConstant())) {
                return generateCloseOrderResponse(CloseOrderStatus.ORDER_ALREADY_CLOSED);
            }
            if (ResponseConstants.FUND_ORDER_STATUS_INVALID.equals(errorResponse.getResponseConstant())) {
                return generateCloseOrderResponse(CloseOrderStatus.INVALID_ORDER_STATUS);
            }
            return generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR);
        }

        return generateCloseOrderResponse(CloseOrderStatus.SUCCESS);
    }
}
