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

@Service("addMoneyExpressValidator")
public class AddMoneyExpressRequestValidator implements IValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyExpressRequestValidator.class);

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

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateIndustryTypeID(workFlowBean.getIndustryTypeID())) {
            LOGGER.error("validation failed as invalid IndustryTypeID is Passed");
            errorMessage = "InvalidIndustryTypeID";
            responseConstant = ResponseConstants.INVALID_INDUSTRY_TYPE_ID;
        }

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateWebsite(workFlowBean.getWebsite())) {
            LOGGER.error("validation failed as invalid website is Passed");
            errorMessage = "InvalidWebsite";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Validating store_card; supported values are 1(need to save) & 0(no
        // need)
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateStoreCard(workFlowBean)) {
            LOGGER.error("validation failed as invalid storeCard value is Passed");
            errorMessage = "InvalidStoreCard";
            responseConstant = ResponseConstants.INVALID_SAVED_CARD_ID;
        }

        // PaymentTypeId supported are CC,DC
        String paymentTypeId = workFlowBean.getPaymentTypeId();
        if (StringUtils.isBlank(errorMessage)
                && !(PaymentTypeIdEnum.CC.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.DC.getValue().equals(paymentTypeId) || PaymentTypeIdEnum.NB.getValue()
                        .equals(paymentTypeId))) {
            LOGGER.error("validation failed as invalid PaymentTypeId is Passed. Only CC,DC are supported");
            errorMessage = "InvalidPaymentTypeId";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (PaymentTypeIdEnum.CC.getValue().equals(paymentTypeId)
                || PaymentTypeIdEnum.DC.getValue().equals(paymentTypeId)) {

            if (StringUtils.isBlank(errorMessage)
                    && !BizParamValidator.validateInputStringParam(workFlowBean.getPaymentDetails())) {
                LOGGER.error("validation failed as invalid Payment Details are Passed");
                errorMessage = "InvalidPaymentDetails";
                responseConstant = ResponseConstants.INVALID_PARAM;
            }

        }

        if (PaymentTypeIdEnum.NB.getValue().equals(paymentTypeId)) {

            if (StringUtils.isBlank(errorMessage)
                    && !BizParamValidator.validateInputStringParam(workFlowBean.getAuthMode())) {
                LOGGER.error("validation failed as invalid auth code is Passed");
                errorMessage = "InvalidAuthMode";
                responseConstant = ResponseConstants.INVALID_PARAM;
            }

            if (StringUtils.isBlank(errorMessage)
                    && !BizParamValidator.validateInputStringParam(workFlowBean.getBankCode())) {
                LOGGER.error("validation failed as invalid bank name is Passed");
                errorMessage = "InvalidBankCode";
                responseConstant = ResponseConstants.INVALID_PARAM;
            }

            if (StringUtils.isBlank(errorMessage)
                    && !BizParamValidator.validateInputStringParam(workFlowBean.getPaymentTypeId())) {
                LOGGER.error("validation failed as invalid payment type id is Passed");
                errorMessage = "InvalidPaymentTypeId";
                responseConstant = ResponseConstants.INVALID_PARAM;
            }

        }

        return errorMessage == null ? new GenericCoreResponseBean<>(Boolean.TRUE) : new GenericCoreResponseBean<>(
                errorMessage, responseConstant);
    }

}
