package com.paytm.pgplus.biz.core.validator.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author manojpal
 *
 */
@Service("autoDebitRequestValidator")
public class AutoDebitRequestValidator implements IValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitRequestValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (errorMessage == null && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.info("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if (errorMessage == null && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.info("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount " + workFlowBean.getTxnAmount();
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if ((errorMessage == null) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.info("validation failed as invalid ChannelID is Passed");
            errorMessage = "InvalidChannelID " + workFlowBean.getChannelID();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateIndustryTypeID(workFlowBean.getIndustryTypeID())) {
            LOGGER.info("validation failed as invalid IndustryTypeID is Passed");
            errorMessage = "InvalidIndustryTypeID " + workFlowBean.getIndustryTypeID();
            responseConstant = ResponseConstants.INVALID_INDUSTRY_TYPE_ID;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getToken())) {
            LOGGER.info("validation failed as invalid Token is Passed");
            errorMessage = "InvalidToken " + workFlowBean.getToken();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateCustId(workFlowBean)) {
            LOGGER.info("validation failed as invalid CustID is Passed");
            errorMessage = "InvalidCustID " + workFlowBean.getCustID();
            responseConstant = ResponseConstants.INVALID_CUST_ID;
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
