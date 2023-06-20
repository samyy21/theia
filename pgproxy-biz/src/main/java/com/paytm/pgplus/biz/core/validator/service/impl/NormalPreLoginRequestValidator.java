/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.validator.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("normalPreLoginValidator")
public class NormalPreLoginRequestValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(NormalPreLoginRequestValidator.class);

    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.info("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType";
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.info("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount";
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if ((errorMessage == null) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.info("validation failed as invalid ChannelID is Passed");
            errorMessage = "InvalidChannelID";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateIndustryTypeID(workFlowBean.getIndustryTypeID())) {
            LOGGER.info("validation failed as invalid IndustryTypeID is Passed");
            errorMessage = "InvalidIndustryTypeID";
            responseConstant = ResponseConstants.INVALID_INDUSTRY_TYPE_ID;
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
