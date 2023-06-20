package com.paytm.pgplus.biz.core.validator.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("userNotLoggedInLitePayviewConsultValidator")
public class UserNotLoggedInLitePayviewConsultValidator implements IValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserNotLoggedInLitePayviewConsultValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.error("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (errorMessage == null && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.error("validation failed as invalid request Type is Passed {}", workFlowBean.getRequestType());
            errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if (errorMessage == null && !BizParamValidator.validateTransactionAmount(workFlowBean.getTxnAmount())) {
            LOGGER.info("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount " + workFlowBean.getTxnAmount();
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if ((errorMessage == null) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.error("validation failed as invalid ChannelID is Passed");
            errorMessage = "InvalidChannelID " + workFlowBean.getChannelID();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
