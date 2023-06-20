/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.enums.TransactionType;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author vaishakhnair
 *
 */
@Service
public class TransactionCacheUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCacheUtils.class);

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    /**
     * @param transId
     * @param isFundOrder
     */
    public void putTransInfoInCache(String transId, String mid, String orderId, boolean isFundOrder) {
        String key = getTransTypeKey(transId);
        theiaTransactionalRedisUtil.set(key, getTransInfo(mid, orderId, isFundOrder),
                TheiaConstant.ExtraConstants.TRANSACTION_TYPE_KEY_EXPIRY);
    }

    public void putTransInfoInCache(String transId, String mid, String orderId, boolean isFundOrder, String... params) {
        String requestType = params[0];
        String isUpiIntentPayment = (params.length > 1) ? params[1] : "false";
        String key = getTransTypeKey(transId);
        theiaTransactionalRedisUtil.set(key, getTransInfo(mid, orderId, isFundOrder, requestType, isUpiIntentPayment),
                TheiaConstant.ExtraConstants.TRANSACTION_TYPE_KEY_EXPIRY);
    }

    /**
     * @param transId
     * @return
     */
    public TransactionInfo getTransInfoFromCache(String transId) {
        String key = getTransTypeKey(transId);
        TransactionInfo transactionInfo = (TransactionInfo) theiaTransactionalRedisUtil.get(key);
        return transactionInfo;
    }

    private String getTransTypeKey(String transId) {
        return Constants.TXN_TYPE_KEY_PREFIX + transId;
    }

    private TransactionInfo getTransInfo(String mid, String orderId, boolean isFundOrder) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setMid(mid);
        transactionInfo.setOrderId(orderId);
        if (isFundOrder) {
            transactionInfo.setTransactionType(TransactionType.FUND);
        } else {
            transactionInfo.setTransactionType(TransactionType.ACQUIRING);
        }
        if (ThreadLocalUtil.getForMockRequest()) {
            transactionInfo.setIsMockRequest(Boolean.TRUE.toString());
        }
        return transactionInfo;
    }

    private static TransactionInfo getTransInfo(String mid, String orderId, boolean isFundOrder, String... params) {
        String requestType = params[0];
        String isUpiIntentPayment = (params.length > 1) ? params[1] : "false";
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setMid(mid);
        transactionInfo.setOrderId(orderId);
        transactionInfo.setRequestType(requestType);
        transactionInfo.setUPIIntentPayment(Boolean.valueOf(isUpiIntentPayment));
        if (isFundOrder) {
            transactionInfo.setTransactionType(TransactionType.FUND);
        } else {
            transactionInfo.setTransactionType(TransactionType.ACQUIRING);
        }
        if (ThreadLocalUtil.getForMockRequest()) {
            transactionInfo.setIsMockRequest(Boolean.TRUE.toString());
        }
        return transactionInfo;
    }

    public void putTransInfoInCacheWrapper(String transId, String mid, String orderId, boolean isFundOrder,
            ERequestType requestType) {
        String key = getTransTypeKey(transId);
        theiaTransactionalRedisUtil.set(key, getTransInfoWrapper(mid, orderId, isFundOrder, requestType),
                TheiaConstant.ExtraConstants.TRANSACTION_TYPE_KEY_EXPIRY);
    }

    private TransactionInfo getTransInfoWrapper(String mid, String orderId, boolean isFundOrder,
            ERequestType requestType) {
        LOGGER.info("Putting transactionInfo in cache for request type : {}", requestType.getType());
        TransactionInfo transactionInfo = getTransInfo(mid, orderId, isFundOrder);
        transactionInfo.setRequestType(requestType.getType());
        return transactionInfo;
    }

}