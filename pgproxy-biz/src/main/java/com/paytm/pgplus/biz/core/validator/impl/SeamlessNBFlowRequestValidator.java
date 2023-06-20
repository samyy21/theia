package com.paytm.pgplus.biz.core.validator.impl;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Kunal Maini
 *
 */

@Service("seamlessNBvalidator")
public class SeamlessNBFlowRequestValidator implements IValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessNBFlowRequestValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
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

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateWebsite(workFlowBean.getWebsite())) {
            LOGGER.error("validation failed as invalid website is Passed");
            errorMessage = "InvalidWebsite";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Validating bank_code; required in case of NetBanking
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateBankCodeForNB(workFlowBean)) {
            LOGGER.error("validation failed as invalid bankCode value is Passed");
            errorMessage = "InvalidBankCode";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }

}
