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

@Service("motoValidator")
public class MotoRequestValidator implements IValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(MotoRequestValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {

        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (workFlowBean == null) {
            LOGGER.error("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        } else {
            if (!BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
                LOGGER.error("validation failed as invalid request Type is Passed");
                errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
                responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
            } else if (!BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
                LOGGER.error("validation failed as invalid txnAmount is Passed");
                errorMessage = "InvalidTxnAmount " + workFlowBean.getTxnAmount();
                responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
            } else if (!BizParamValidator.validateInputStringParam(workFlowBean.getSavedCardID())) {
                LOGGER.error("validation failed as invalid Saved Card ID is Passed");
                errorMessage = "InvalidSavedCardID " + workFlowBean.getSubsServiceID();
                responseConstant = ResponseConstants.INVALID_PARAM;
            } else if (!BizParamValidator.validateInputStringParam(workFlowBean.getOrderID())) {
                LOGGER.error("validation failed as invalid Order ID is Passed");
                errorMessage = "InvalidOrderID " + workFlowBean.getOrderID();
                responseConstant = ResponseConstants.INVALID_ORDER_ID;
            } else if (!BizParamValidator.validateInputStringParam(workFlowBean.getPaytmMID())) {
                LOGGER.error("validation failed as invalid MID is Passed");
                errorMessage = "InvalidMID " + workFlowBean.getPaytmMID();
                responseConstant = ResponseConstants.INVALID_MID;
            } else {
                LOGGER.debug("Validation Successful");
            }
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
