package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;

/**
 * @author kartik
 * @date 24-May-2018
 */
@Component
public class NativeRetryPaymentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRetryPaymentUtil.class);

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    public boolean canPaymentRetry(WorkFlowTransactionBean workFlowTransBean) {
        int maxRetriesAllowedOnMerchant = workFlowTransBean.getWorkFlowBean().getMaxAllowedOnMerchant();
        int currentRetryCount = workFlowTransBean.getWorkFlowBean().getNativeRetryCount();
        LOGGER.info("Checking if payment can be retried , currentRetryCount : {} , maxRetriesAllowedOnMerchant : {}",
                currentRetryCount, maxRetriesAllowedOnMerchant);
        if (maxRetriesAllowedOnMerchant <= 0) {
            return false;
        }
        return currentRetryCount < maxRetriesAllowedOnMerchant ? true : false;
    }

    public void setTransIdInCache(String txnToken, String transId) {
        theiaSessionRedisUtil.hsetIfExist(txnToken, "transactionId", transId);
    }

    public void setField(String key, String field, String value) {
        theiaSessionRedisUtil.hset(key, field, value, 900);
    }

}
