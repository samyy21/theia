/*
 * This File is the sole property of Paytm(One97 Communications Limited)
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

@Service("invoicePaymentRetryValidator")
public class InvoicePaymentRetryValidator implements IValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(InvoicePaymentRetryValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getOrderID())) {
            LOGGER.info("validation failed as invalid OrderID is Passed");
            errorMessage = "InvalidOrderID " + workFlowBean.getOrderID();
            responseConstant = ResponseConstants.INVALID_ORDER_ID;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getTransID())) {
            LOGGER.info("validation failed as invalid TransID is Passed");
            errorMessage = "InvalidTransID " + workFlowBean.getTransID();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
