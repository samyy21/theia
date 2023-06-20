package com.paytm.pgplus.biz.core.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * Created by Naman on 03/07/17.
 */
@Service("stockTradingValidator")
public class StockTradingFlowValidator implements IValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(StockTradingFlowValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(WorkFlowRequestBean workFlowBean) {

        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.error("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.error("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType";
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.error("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount";
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.error("validation failed as invalid ChannelID is Passed");
            errorMessage = "InvalidChannelID";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        String paymentTypeId = workFlowBean.getPaymentTypeId();
        if (StringUtils.isBlank(errorMessage) && !(PaymentTypeIdEnum.NB.getValue().equals(paymentTypeId))) {
            LOGGER.error("validation failed as invalid PaymentTypeId is Passed. Only NB is supported");
            errorMessage = "InvalidPaymentTypeId";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Validating bank_code; required in case of NetBanking
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateBankCodeForNB(workFlowBean)) {
            LOGGER.error("validation failed as invalid bankCode value is Passed");
            errorMessage = "InvalidBankCode";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateSeamlessPaymentDetails(workFlowBean.getPaymentDetails())) {
            LOGGER.error("validation failed as invalid Payment Details is Passed");
            errorMessage = "InvalidBankCode";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        return errorMessage == null ? new GenericCoreResponseBean<>(Boolean.TRUE) : new GenericCoreResponseBean<>(
                errorMessage, responseConstant);
    }
}
