/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.validator.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("subscriptionRenewalCCOnlyValidator")
public class SubscriptionRenewalCCOnlyValidator implements IValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRenewalCCOnlyValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {

        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (workFlowBean == null) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        } else {
            if (!BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
                LOGGER.info("validation failed as invalid request Type is Passed");
                errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
                responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
            }

            if ((errorMessage == null) && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
                LOGGER.info("validation failed as invalid txnAmount is Passed");
                errorMessage = "Transaction Amount is mandatory";
                responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
            }

            if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getSubscriptionID())) {
                LOGGER.info("validation failed as invalid Subscription Service ID is Passed");
                errorMessage = "Length Validation failed for Subscription ID";
                responseConstant = ResponseConstants.INVALID_PARAM;
            }

            if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getOrderID())) {
                LOGGER.info("validation failed as invalid Order ID is Passed");
                errorMessage = "InvalidOrderID " + workFlowBean.getOrderID();
                responseConstant = ResponseConstants.INVALID_ORDER_ID;
            }

            if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getPaytmMID())) {
                LOGGER.info("validation failed as invalid MID is Passed");
                errorMessage = "InvalidMID " + workFlowBean.getPaytmMID();
                responseConstant = ResponseConstants.INVALID_MID;
            }

        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
