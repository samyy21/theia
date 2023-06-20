/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.utils.NativeRetryPaymentUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("nativeSeamlessflowservice")
public class NativeSeamlessFlowService extends SeamlessFlowService {
    public static final Logger LOGGER = LoggerFactory.getLogger(NativeSeamlessFlowService.class);

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Override
    public void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        boolean canRetry = nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean);
        if (!canRetry) {
            final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                    .closeOrder(workFlowTransBean);
            if (!cancelOrder.isSuccessfullyProcessed()) {
                LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
            }
        } else {
            LOGGER.error("We can retry payment, So not closing order , txnToken : {}", workFlowTransBean
                    .getWorkFlowBean().getTxnToken());
        }
    }
}