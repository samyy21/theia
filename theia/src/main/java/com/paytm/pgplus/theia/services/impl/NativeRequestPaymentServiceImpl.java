/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author kesari
 * @createdOn 27-Mar-2016 Skipping checksum in case of native flow. As checksum
 *            is already validated during create token request
 */
@Service("nativeRequestPaymentServiceImpl")
public class NativeRequestPaymentServiceImpl extends SeamlessPaymentServiceImpl {

    private static final long serialVersionUID = -2514048658596141941L;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRequestPaymentServiceImpl.class);

    @Autowired
    @Qualifier("nativeSeamlessflowservice")
    private IWorkFlow seamlessflowservice;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    private IWorkFlow nativeRetryPaymentFlowService;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData) {
        return ValidationResults.VALIDATION_SUCCESS;
    }

    @Override
    public IWorkFlow getWorkFlowService() {
        return seamlessflowservice;
    }

    @Override
    public IWorkFlow getRetryWorkFlowService() {
        return nativeRetryPaymentFlowService;
    }

}
