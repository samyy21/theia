package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.request.BizCancelFundOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kartik
 * @date 24-May-2018
 */
@Component
public class NativeRetryUtil {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderServiceImpl;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private RouterUtil routerUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRetryUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeRetryUtil.class);

    private static final String RISK_REJECT_ORDER_CLOSE_REASON = "RISK_REJECT";

    public boolean isRetryPossible(WorkFlowRequestBean workFlowRequestBean) {
        String token = workFlowRequestBean.getTxnToken();
        String mid = workFlowRequestBean.getPaytmMID();

        if (StringUtils.isBlank(token)) {
            token = mid + workFlowRequestBean.getOrderID();
        }

        int allowedRetryCountsForMerchant;

        // default perform retry on all scan and pay transactions
        if (workFlowRequestBean.isScanAndPayFlow()
                && Boolean.valueOf(ConfigurationUtil.getProperty(TheiaConstant.RetryConstants.OFFLINE_RETRY_ALL,
                        "false"))) {
            allowedRetryCountsForMerchant = Integer.parseInt(ConfigurationUtil
                    .getProperty(TheiaConstant.RetryConstants.MAX_RETRY_ALLOWED_ON_OFFLINE_MERCHANT));
            EXT_LOGGER.customInfo("Transaction is scan and pay, perform " + allowedRetryCountsForMerchant
                    + " max retries on static QR merchant");
        } else
            allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(mid);

        workFlowRequestBean.setMaxAllowedOnMerchant(allowedRetryCountsForMerchant);
        Integer currentRetryCountBankFails = nativeSessionUtil.getRetryPaymentCount(token);
        Integer totalPaymentCount = nativeSessionUtil.getTotalPaymenCount(token);
        if (totalPaymentCount != null)
            workFlowRequestBean.setNativeTotalPaymentCount(totalPaymentCount);

        if (currentRetryCountBankFails == null) {
            return true;
        }
        EXT_LOGGER.customInfo("Checking if retry possible, current retry count for bank fails : {}",
                currentRetryCountBankFails);
        workFlowRequestBean.setNativeRetryCount(currentRetryCountBankFails + 1);
        if (allowedRetryCountsForMerchant <= 0) {
            LOGGER.debug("Max number of payment retry is zero or less for merchantID : {}", mid);
            return false;
        }

        if (currentRetryCountBankFails < allowedRetryCountsForMerchant) {
            LOGGER.info("Merchant retry case, current retry count for merchant : {}", currentRetryCountBankFails);
            return true;
        }
        return false;
    }

    public boolean isRetryPossible(String token, String mid) {

        if (StringUtils.isBlank(token)) {
            return false;
        }

        int allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(mid);

        Integer currentRetryCountBankFails = nativeSessionUtil.getRetryPaymentCount(token);
        LOGGER.info("Checking if retry possible, current retry count for bank fails : {}", currentRetryCountBankFails);

        if (currentRetryCountBankFails == null) {
            return true;
        }
        if (allowedRetryCountsForMerchant <= 0) {
            LOGGER.debug("Max number of payment retry is zero or less for merchantID : {}", mid);
            return false;
        }

        if (currentRetryCountBankFails < allowedRetryCountsForMerchant) {
            LOGGER.info("Merchant retry case, current retry count for merchant : {}", currentRetryCountBankFails);
            return true;
        }
        return false;
    }

    public Integer increaseRetryCount(String txnToken, String mid, String orderId) {
        if (txnToken == null) {
            return null;
        }
        Integer currentRetryCount = nativeSessionUtil.getRetryPaymentCount(txnToken);
        // first payment attempt
        if (currentRetryCount == null) {
            currentRetryCount = 0;
            nativeSessionUtil.setRetryPaymentCount(txnToken, 0);
        } else {
            currentRetryCount = currentRetryCount + 1;
            LOGGER.info("Setting current retry count as  : {}", currentRetryCount);
            nativeSessionUtil.setRetryPaymentCount(txnToken, currentRetryCount);
            nativeSessionUtil.updateTokenExpiry(txnToken, mid, orderId);
        }
        return currentRetryCount;
    }

    private boolean isNativeEnhanceFlow(PaymentRequestBean requestData) {
        return ERequestType.NATIVE.name().equals(requestData.getRequestType())
                && requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"));
    }

    public void checkForNativeRetry(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean,
            WorkFlowResponseBean flowResponseBean, ResponseConstants responseConstant) {
        // skipping retry check for UNI_PAY (AOA) as part of PGP-35048
        if (!(ERequestType.isNativeOrNativeSubscriptionRequest(requestData.getRequestType()))) {
            return;
        }
        Map<String, String> userRetryMetaData = new LinkedHashMap<>();
        String requestType = requestData.getRequestType();
        if (isNativeEnhanceFlow(requestData)) {
            requestType = TheiaConstant.ExtraConstants.NATIVE_ENHANCED;
        }
        userRetryMetaData.put(TheiaConstant.ExtraConstants.REQUEST_TYPE, requestType);
        String paymentMode = requestData.getPaymentMode() != null ? requestData.getPaymentMode() : requestData
                .getPaymentTypeId();
        userRetryMetaData.put(TheiaConstant.ExtraConstants.PAYMENT_MODE, paymentMode);
        if (responseConstant != null
                && (ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.equals(responseConstant) || ResponseConstants.AMOUNT_EXCEEDS_LIMIT
                        .equals(responseConstant))) {
            requestData.setNativeRetryEnabled(false);
            requestData.setNativeRetryErrorMessage(responseConstant.getMessage());
        } else if (flowRequestBean.getPaymentFailureType() != null) {
            if (NativePaymentFailureType.RISK_REJECT.equals(flowRequestBean.getPaymentFailureType())
                    || NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED.equals(flowRequestBean
                            .getPaymentFailureType())) {
                requestData.setNativeRetryEnabled(true);
            } else {
                if (flowRequestBean.getNativeRetryCount() < flowRequestBean.getMaxAllowedOnMerchant()) {
                    EventUtils.pushTheiaEvents(EventNameEnum.USER_RETRY_INITIATED, userRetryMetaData);
                    requestData.setNativeRetryEnabled(true);
                } else {
                    requestData.setNativeRetryEnabled(false);
                }
            }
            requestData.setNativeRetryErrorMessage(flowRequestBean.getPaymentFailureType().getErrorMessage());
        } else if (responseConstant != null) {
            ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(responseConstant
                    .getSystemResponseCode());

            setRetryEnabledForNative(responseCodeDetails, flowRequestBean, requestData, userRetryMetaData);
        } else {
            requestData.setNativeRetryEnabled(false);
        }
    }

    public void invalidateSession(String txnToken, NativeRetryInfo nativeRetryInfo,
            InitiateTransactionRequestBody orderDetail, EnvInfoRequestBean envInfo, boolean isRiskReject) {
        if (nativeRetryInfo != null) {
            invalidateSession(txnToken, nativeRetryInfo.isRetryAllowed(), orderDetail, envInfo, false);
        }
    }

    public void invalidateSessionByRequestType(final PaymentRequestBean paymentRequestBean,
            final ERequestType... eRequestTypes) {
        if (eRequestTypes == null || paymentRequestBean == null) {
            LOGGER.warn("Not found eRequestTypes or paymentRequestBean!!");
            return;
        }
        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(paymentRequestBean.getRequest());
        for (ERequestType eRequestType : eRequestTypes) {
            if (eRequestType.getType().equals(paymentRequestBean.getRequestType())) {
                invalidateSession(paymentRequestBean.getTxnToken(), paymentRequestBean.isNativeRetryEnabled(), null,
                        envInfo, false);
                break;
            }
        }

    }

    public void invalidateSession(final String txnToken, final boolean isSessionValid,
            InitiateTransactionRequestBody orderDetail, EnvInfoRequestBean envInfo, boolean isRiskReject) {
        if (!isSessionValid) {
            if (txnToken != null) {
                String midOrderIDToken = null;

                try {
                    if (ObjectUtils.isEmpty(orderDetail))
                        orderDetail = nativeSessionUtil.getOrderDetail(txnToken);

                } catch (NullPointerException e) {
                    LOGGER.error("Session Invalidate Exception in case of deleting keys", e);
                    throw SessionExpiredException.getException();
                }
                if (!ObjectUtils.isEmpty(orderDetail)) {
                    LOGGER.debug("TxnId:{} token found in Redis", txnToken);
                    midOrderIDToken = nativeSessionUtil.getMidOrderIdKeyForRedis(orderDetail.getMid(),
                            orderDetail.getOrderId());
                    String transId = getTxnId(txnToken);
                    if (StringUtils.isNotBlank(transId)) {
                        if (orderDetail.isNativeAddMoney()) {
                            if (isRiskReject) {
                                LOGGER.info("Closing fund order due to risk reject");
                                closeFundOrderOnRiskReject(orderDetail, transId, envInfo);
                            } else {
                                LOGGER.info("Closing fund order to invalidate Session");
                                closeFundOrderOnInvalidateSession(orderDetail, transId, envInfo);
                            }
                        } else {
                            String paymentMid = nativeSessionUtil.getFieldValue(transId, "PaymentMid");

                            /**
                             * Stored the original mid in order to proceed with
                             * the transaction after closing order on dummy mid.
                             */

                            String originalMid = orderDetail.getMid();
                            if (StringUtils.isNotBlank(paymentMid)) {
                                orderDetail.setMid(paymentMid);
                            }
                            if (isRiskReject) {
                                LOGGER.info("Closing order acquiring order to risk reject");
                                closeOrderOnRiskReject(orderDetail, transId);
                            } else {
                                LOGGER.info("Closing Order to Invalidate Session");
                                closeOrderOnInvalidateSession(orderDetail, transId);
                            }

                            orderDetail.setMid(originalMid);
                        }
                    }

                }
                // stopping key delete
                /*
                 * try { // Assumption: If txnToken exists then midOrderIDToken
                 * and // vice-versa LOGGER.info("Deleting keys {} , {}",
                 * midOrderIDToken, txnToken);
                 * nativeSessionUtil.deleteKey(midOrderIDToken, txnToken); }
                 * catch (JedisDataException e) { LOGGER.error(
                 * "Session Invalidate Exception in case of deleting keys", e);
                 * throw SessionExpiredException.getException(); } LOGGER.debug(
                 * "Sucessfully deleted keys: txnId :{},midOrderIdToken: {}",
                 * txnToken, midOrderIDToken);
                 */
            }
        }
    }

    private void closeOrderOnInvalidateSession(InitiateTransactionRequestBody orderDetail, String transId) {

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(orderDetail.getMid());
        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            String alipayMid = merchantMappingResponse.getResponse().getAlipayId();
            boolean fromAoaMerchant = aoaUtils.isAOAMerchant(orderDetail.getMid());
            BizCancelOrderRequest bizCancelOrderRequest = new BizCancelOrderRequest(alipayMid, transId,
                    "Session Invalidation", fromAoaMerchant, orderDetail.getMid(), this.routerUtil.getRoute(
                            orderDetail.getMid(), orderDetail.getOrderId(), "closeOrderOnInvalidateSession"));
            final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = orderServiceImpl
                    .closeOrder(bizCancelOrderRequest);
            if (!cancelOrder.isSuccessfullyProcessed()) {
                LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
            }
        }

    }

    public void closeOrderOnRiskReject(InitiateTransactionRequestBody orderDetail, String transId) {

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(orderDetail.getMid());
        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            String alipayMid = merchantMappingResponse.getResponse().getAlipayId();
            boolean fromAoaMerchant = aoaUtils.isAOAMerchant(orderDetail.getMid());
            BizCancelOrderRequest bizCancelOrderRequest = new BizCancelOrderRequest(alipayMid, transId,
                    RISK_REJECT_ORDER_CLOSE_REASON, fromAoaMerchant, orderDetail.getMid(), this.routerUtil.getRoute(
                            orderDetail.getMid(), orderDetail.getOrderId(), "closeOrderOnRiskReject"));
            final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = orderServiceImpl
                    .closeOrder(bizCancelOrderRequest);
            if (!cancelOrder.isSuccessfullyProcessed()) {
                LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
            }
        }

    }

    private void closeFundOrderOnInvalidateSession(InitiateTransactionRequestBody orderDetail, String transId,
            EnvInfoRequestBean envInfo) {
        BizCancelFundOrderRequest bizCancelFundOrderRequest = new BizCancelFundOrderRequest(transId, envInfo);
        bizCancelFundOrderRequest.setPaytmMerchantId(orderDetail.getMid());
        bizCancelFundOrderRequest.setRoute(Routes.PG2);
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = orderServiceImpl
                .closeFundOrder(bizCancelFundOrderRequest);
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel fund order failed due to :: {}", cancelFundOrder.getFailureMessage());
        }
    }

    public void closeFundOrderOnRiskReject(InitiateTransactionRequestBody orderDetail, String transId,
            EnvInfoRequestBean envInfo) {
        BizCancelFundOrderRequest bizCancelFundOrderRequest = new BizCancelFundOrderRequest(transId, envInfo,
                RISK_REJECT_ORDER_CLOSE_REASON);
        bizCancelFundOrderRequest.setPaytmMerchantId(orderDetail.getMid());
        bizCancelFundOrderRequest.setRoute(Routes.PG2);
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = orderServiceImpl
                .closeFundOrder(bizCancelFundOrderRequest);
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel fund order failed due to :: {}", cancelFundOrder.getFailureMessage());
        }
    }

    public void invalidateSession(final String txnToken, final boolean sessionValidate, String mid, String orderID,
            EnvInfoRequestBean envInfo) {
        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(txnToken);
        if (ObjectUtils.isEmpty(orderDetail) && StringUtils.isNotEmpty(mid) && StringUtils.isNotEmpty(orderID)) {
            orderDetail = new InitiateTransactionRequestBody();
            orderDetail.setMid(mid);
            orderDetail.setOrderId(orderID);
        }
        invalidateSession(txnToken, sessionValidate, orderDetail, envInfo, false);

    }

    public String getTxnId(String token) {
        return nativeSessionUtil.getTxnId(token);
    }

    public Integer getRetryPaymentCount(String token) {
        return nativeSessionUtil.getRetryPaymentCount(token);
    }

    public void setCallbackUrl(String token, String callbackUrl) {
        InitiateTransactionRequestBody orderDetails = nativeSessionUtil.getOrderDetail(token);
        orderDetails.setCallbackUrl(callbackUrl);
        nativeSessionUtil.setOrderDetail(token, orderDetails);
    }

    public String getCallbackUrl(String token) {
        InitiateTransactionRequestBody orderDetails = nativeSessionUtil.getOrderDetail(token);
        return orderDetails.getCallbackUrl();
    }

    private void setRetryEnabledForNative(ResponseCodeDetails responseCodeDetails, WorkFlowRequestBean flowRequestBean,
            PaymentRequestBean requestData, Map<String, String> userRetryMetaData) {

        int currentRetryCount = increaseRetryCount(flowRequestBean.getTxnToken(), flowRequestBean.getPaytmMID(),
                flowRequestBean.getOrderID());
        if (currentRetryCount < flowRequestBean.getMaxAllowedOnMerchant()) {
            // LOGGER.info("Payment Retry Possible ,Setting retryEnabled true in Payment Request bean");
            EventUtils.pushTheiaEvents(EventNameEnum.USER_RETRY_INITIATED, userRetryMetaData);

            requestData.setNativeRetryEnabled(true);
        }

    }

    public int getAllowedRetryCountsOnMerchant(String mid) {
        int allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(mid);
        return allowedRetryCountsForMerchant;
    }

}
