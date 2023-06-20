package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.core.model.request.BizCancelFundOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.model.RouteResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay;
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.RouterUtil;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PG2_FUND_ORDER_ROUTE_CONTEXT_PREFIX;

/**
 * @author amitdubey
 */
@Service
public class RetryServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryServiceHelper.class);
    private static final long timeOutCacheSec = 3600;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderServiceImpl;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private RouterUtil routerUtil;

    public PaymentRequestBean getRequestDataFromCache(String transId) {
        StringBuilder sb = new StringBuilder();
        sb.append(RetryConstants.RETRY_PAYMENT_).append(transId);
        return (PaymentRequestBean) theiaTransactionalRedisUtil.get(sb.toString());

    }

    public void setRequestDataInCache(String transId, PaymentRequestBean requestData) {
        StringBuilder sb = new StringBuilder();
        sb.append(RetryConstants.RETRY_PAYMENT_).append(transId);
        theiaTransactionalRedisUtil.set(sb.toString(), requestData, timeOutCacheSec);
    }

    public boolean checkForRequestDataInCache(final String transId) {
        if (StringUtils.isBlank(transId)) {
            return false;
        }

        // Need to verify if originalRequestData exist in cache
        PaymentRequestBean originalRequestData = null;
        try {
            originalRequestData = getRequestDataFromCache(transId);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Request data from cache.So retry will not happen.", e);
        }

        if (originalRequestData != null) {
            return true;
        }

        return false;
    }

    public boolean checkForRetryCount(String merchantId, GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        String orderId = getOrderIdFromCashierResponse(cashierResponse);
        String txnToken = null;
        if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(orderId))
            txnToken = getTxnToken(merchantId, orderId);

        int allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(merchantId);
        // checking if request is offline and not dynamic QR
        if (StringUtils.isNotBlank(txnToken) && nativeSessionUtil.getScanAndPayFlag(txnToken)) {
            // default perform retry on all scan and pay transactions
            if (Boolean.valueOf(ConfigurationUtil.getProperty(TheiaConstant.RetryConstants.OFFLINE_RETRY_ALL, "false"))) {
                allowedRetryCountsForMerchant = Integer.parseInt(ConfigurationUtil
                        .getProperty(TheiaConstant.RetryConstants.MAX_RETRY_ALLOWED_ON_OFFLINE_MERCHANT));
                LOGGER.info("Transaction is scan and pay, perform " + allowedRetryCountsForMerchant
                        + " max retries on static QR merchant");
            }

        }

        LOGGER.info("Allowed number of payment retries : {} for merchantid : {}", merchantId,
                allowedRetryCountsForMerchant);

        // Check if retry allowed for merchant
        if (allowedRetryCountsForMerchant <= 0) {
            LOGGER.debug("Max number of payment retry is zero or less for merchantID : {}", merchantId);
            return true;
        }

        String paymentRetryCount = cashierResponse.getResponse().getPaymentStatus().getExtendInfo()
                .get(ExtendedInfoPay.RETRY_COUNT);
        if (StringUtils.isBlank(paymentRetryCount)) {
            LOGGER.debug("Payment retry count not found for the merchantid : {}", merchantId);
            return true;
        }

        int currentPaymentRetryCount = Integer.parseInt(paymentRetryCount);
        LOGGER.info("Current payment retry count is : {}", currentPaymentRetryCount);

        // Checking if retryCount for merchant is not breaching
        if (currentPaymentRetryCount < allowedRetryCountsForMerchant) {
            return false;
        }

        return true;
    }

    public boolean checkPaymentRetry(String merchantId, String orderId,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse, String transId, boolean isFundOrder,
            EnvInfoRequestBean envInfo) {
        boolean maxRetryCountReached = checkForRetryCount(merchantId, cashierResponse);

        if (maxRetryCountReached
                && ((!isFundOrder && cashierResponse.getResponse().getTransactionStatus() != null) || (isFundOrder && cashierResponse
                        .getResponse().getFundOrderStatus() != null))) {
            try {

                if (cashierResponse != null
                        && cashierResponse.getResponse().getTransactionStatus() != null
                        && cashierResponse.getResponse().getTransactionStatus().getExtendInfo() != null
                        && cashierResponse
                                .getResponse()
                                .getTransactionStatus()
                                .getExtendInfo()
                                .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DUMMY_MERCHANT_ID) != null) {
                    merchantId = cashierResponse
                            .getResponse()
                            .getTransactionStatus()
                            .getExtendInfo()
                            .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DUMMY_MERCHANT_ID);
                }
                final MappingMerchantData theiaMerchantMappingResponse = merchantMappingService
                        .getMappingMerchantData(merchantId);

                if (isFundOrder) {
                    BizCancelFundOrderRequest cancelFundOrderRequest = new BizCancelFundOrderRequest(cashierResponse
                            .getResponse().getFundOrderStatus().getFundOrderId(), envInfo);
                    String routeFundOrderId = null;
                    if (StringUtils.isNotBlank(orderId))
                        routeFundOrderId = PG2_FUND_ORDER_ROUTE_CONTEXT_PREFIX + orderId;
                    RouteResponse routeResponse = routerUtil.getRouteResponse(merchantId, routeFundOrderId, transId,
                            null, "checkPaymentRetry");
                    cancelFundOrderRequest.setRoute(routeResponse.getName());
                    if (Routes.PG2.equals(routeResponse.getName()))
                        cancelFundOrderRequest.setPaytmMerchantId(routeResponse.getMid());
                    orderServiceImpl.closeFundOrder(cancelFundOrderRequest);
                } else {
                    BizCancelOrderRequest cancelAcquiringOrderRequest = new BizCancelOrderRequest(
                            theiaMerchantMappingResponse.getAlipayId(), transId,
                            "Max payment retry count limit reached");
                    RouteResponse routeResponse = routerUtil.getRouteResponse(merchantId, null, transId, null,
                            "checkPaymentRetry");
                    cancelAcquiringOrderRequest.setRoute(routeResponse.getName());
                    if (Routes.PG2.equals(routeResponse.getName()))
                        cancelAcquiringOrderRequest.setPaytmMerchantId(routeResponse.getMid());
                    orderServiceImpl.closeOrder(cancelAcquiringOrderRequest);
                }

                LOGGER.info("Order closed for transaction id : {}", transId);
            } catch (Exception e) {
                LOGGER.error(
                        "Exception occurred while fetching Merchant Mapping from Redis/Mapping service: for merchantId : {} ",
                        merchantId, e);
            }
        }
        return !maxRetryCountReached && checkForRequestDataInCache(transId);
    }

    public boolean checkNativePaymentRetry(String merchantId, String orderId) {
        String txnToken = getTxnToken(merchantId, orderId);
        if (StringUtils.isBlank(txnToken)) {
            return false;
        }
        Integer currentRetryCount = nativeSessionUtil.getRetryPaymentCount(txnToken);
        if (currentRetryCount == null)
            currentRetryCount = 0;
        int allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(merchantId);
        if (nativeSessionUtil.getScanAndPayFlag(txnToken)) {
            // default perform retry on all scan and pay transactions
            if (Boolean.valueOf(ConfigurationUtil.getProperty(TheiaConstant.RetryConstants.OFFLINE_RETRY_ALL, "false"))) {
                allowedRetryCountsForMerchant = Integer.parseInt(ConfigurationUtil
                        .getProperty(TheiaConstant.RetryConstants.MAX_RETRY_ALLOWED_ON_OFFLINE_MERCHANT));
                LOGGER.info("Transaction is scan and pay, perform " + allowedRetryCountsForMerchant
                        + " max retries on static QR merchant");
            }
        }
        if (allowedRetryCountsForMerchant <= 0) {
            LOGGER.debug("Max number of payment retry is zero or less for merchantID : {}", merchantId);
            return false;
        }
        LOGGER.info("Current payment retry count is : {}", currentRetryCount);
        if (currentRetryCount < allowedRetryCountsForMerchant) {
            return true;
        }
        return false;
    }

    public String getTxnToken(String mid, String orderId) {
        StringBuilder midOrderIdKey = new StringBuilder("NativeTxnInitiateRequest");
        midOrderIdKey.append(mid).append("_").append(orderId);
        return (String) nativeSessionUtil.getKey(midOrderIdKey.toString());
    }

    public String getOrderIdFromCashierResponse(GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        DoPaymentResponse doPaymentResponse = cashierResponse.getResponse();
        String orderId = null;
        if (doPaymentResponse != null) {
            CashierPaymentStatus cashierPaymentStatus = doPaymentResponse.getPaymentStatus();
            CashierTransactionStatus cashierTransactionStatus = doPaymentResponse.getTransactionStatus();
            CashierFundOrderStatus cashierFundOrderStatus = doPaymentResponse.getFundOrderStatus();
            if (cashierPaymentStatus != null) {
                Map<String, String> cashierExtendInfo = cashierPaymentStatus.getExtendInfo();
                if (cashierExtendInfo.size() > 0) {
                    orderId = cashierExtendInfo.get(ExtendedInfoPay.MERCHANT_TRANS_ID);
                }
            }
            if (StringUtils.isBlank(orderId)) {
                if (cashierTransactionStatus != null) {
                    orderId = cashierTransactionStatus.getMerchantTransId();
                }
            }
            if (StringUtils.isBlank(orderId)) {
                if (cashierFundOrderStatus != null) {
                    orderId = cashierFundOrderStatus.getRequestId();
                }
            }
        }
        return orderId;
    }

    public NativePaymentRequestBody getNativePaymentRequestBodyByRequest(PaymentRequestBean requestData)
            throws Exception {
        String nativeRequestData = IOUtils.toString(((ModifiableHttpServletRequest) requestData.getRequest())
                .getRequest().getInputStream(), Charsets.UTF_8.name());

        if (StringUtils.isNotBlank(nativeRequestData)) {
            NativePaymentRequest nativePaymentRequest = JsonMapper.mapJsonToObject(nativeRequestData,
                    NativePaymentRequest.class);
            NativePaymentRequestBody nativePaymentRequestBody = nativePaymentRequest.getBody();
            return nativePaymentRequestBody;
        }
        return null;
    }

    public void setNativeCheckOutJsPaymentsDataForRetry(PaymentRequestBean requestData,
            NativePaymentRequestBody nativePaymentRequestBody) throws Exception {

        // Modify cardInfo
        StringBuilder cardInfo = null;
        if (StringUtils.isNotBlank(nativePaymentRequestBody.getCardInfo())) {
            cardInfo = new StringBuilder(nativePaymentRequestBody.getCardInfo());
        }

        if (null != cardInfo) {
            String[] cardInfoArray = cardInfo.toString().split(Pattern.quote("|"));
            if (cardInfoArray[0].length() > 1) {
                // saved card id present
                nativePaymentRequestBody.setCardInfo(cardInfoArray[0]);
            } else {
                // saved card id not present
                nativePaymentRequestBody.setCardInfo("|" + cardInfoArray[1]);
            }
        }
        // set this updated data in cache
        String txnToken = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(requestData.getTxnToken())) {
            txnToken = requestData.getTxnToken();
        } else {
            txnToken = getTxnToken(requestData.getMid(), requestData.getOrderId());
        }
        setCheckOutJsRetryDataAndMerchantConfigInCache(txnToken, nativePaymentRequestBody);
    }

    private void setCheckOutJsRetryDataAndMerchantConfigInCache(String txnToken,
            NativePaymentRequestBody nativePaymentRequestBody) {
        LOGGER.info("setting CheckOutJsRetryDataAndMerchantConfigInCache {}", nativePaymentRequestBody);
        if (StringUtils.isNotBlank(txnToken) && nativePaymentRequestBody != null) {
            nativeSessionUtil.setRetryDataAndMerchantConfigForCheckOutJs(txnToken, nativePaymentRequestBody);
        }
    }

    public boolean checkNativePaymentRetryByTxnToken(String merchantId, String txnToken) {
        if (StringUtils.isBlank(txnToken)) {
            return false;
        }
        Integer currentRetryCount = nativeSessionUtil.getRetryPaymentCount(txnToken);
        if (currentRetryCount == null)
            currentRetryCount = 0;
        int allowedRetryCountsForMerchant = merchantExtendInfoUtils.getNumberOfRetries(merchantId);
        if (nativeSessionUtil.getScanAndPayFlag(txnToken)) {
            // default perform retry on all scan and pay transactions
            if (Boolean.valueOf(ConfigurationUtil.getProperty(TheiaConstant.RetryConstants.OFFLINE_RETRY_ALL, "false"))) {
                allowedRetryCountsForMerchant = Integer.parseInt(ConfigurationUtil
                        .getProperty(TheiaConstant.RetryConstants.MAX_RETRY_ALLOWED_ON_OFFLINE_MERCHANT));
                LOGGER.info("Transaction is scan and pay, perform " + allowedRetryCountsForMerchant
                        + " max retries on static QR merchant");
            }
        }
        if (allowedRetryCountsForMerchant <= 0) {
            LOGGER.debug("Max number of payment retry is zero or less for merchantID : {}", merchantId);
            return false;
        }
        LOGGER.info("Current payment retry count is : {}", currentRetryCount);
        if (currentRetryCount < allowedRetryCountsForMerchant) {
            return true;
        }
        return false;
    }

}