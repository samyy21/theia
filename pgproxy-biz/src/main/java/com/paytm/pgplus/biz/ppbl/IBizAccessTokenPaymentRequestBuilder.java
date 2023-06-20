package com.paytm.pgplus.biz.ppbl;

import com.paytm.pgplus.biz.core.model.request.BizPayOptionBill;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

import java.util.Map;

/**
 * Created by rahulverma on 5/10/17.
 */
public interface IBizAccessTokenPaymentRequestBuilder {

    void buildPPBPaymentRequestParameters(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) throws PaytmValidationException;

    /*
     * String validatePassCode(String passCode, String mobileNo, String
     * oAuthClientId, String oAuthSecretKey) throws PaytmValidationException,
     * FacadeCheckedException;
     */

    void buildDigitalCreditRequestParameters(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) throws PaytmValidationException;

    void buildUPIRequestParameters(WorkFlowTransactionBean workFlowTransactionBean, Map<String, String> channelInfo)
            throws PaytmValidationException;
}
