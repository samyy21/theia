/**
 * 
 */
package com.paytm.pgplus.biz.core.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */

@Service("subscriptionFreshCCOnlyPreValidator")
public class SubscriptionFreshCCOnlyPreValidator implements IValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyRequestValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())
                && workFlowBean.getRequestType().getType().equals("SUBSCRIBE")) {
            LOGGER.info("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if ((errorMessage == null)
                && !BizParamValidator.validateSubscritpionFrequencyUnit(workFlowBean.getSubsFrequencyUnit())) {
            LOGGER.info("validation failed as invalid Subscription Frequency Type is Passed");
            errorMessage = "InvalidSubsFrequencyType " + workFlowBean.getSubsFrequencyUnit();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateSubscritpionFrequency(workFlowBean.getSubsFrequency())) {
            LOGGER.info("validation failed as invalid Subscription Frequency is Passed");
            errorMessage = "InvalidSubsFrequency " + workFlowBean.getSubsFrequency();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateCustId(workFlowBean)) {
            LOGGER.info("validation failed as invalid CustID is Passed");
            errorMessage = "InvalidCustID " + workFlowBean.getCustID();
            responseConstant = ResponseConstants.INVALID_CUST_ID;
        }

        // Validate subscription grace days
        if ((errorMessage == null) && BizParamValidator.validateInputStringParam(workFlowBean.getSubsStartDate())
                && !BizParamValidator.validateInputStringParam(workFlowBean.getSubsGraceDays())) {
            LOGGER.info("validation failed as invalid grace days is Passed");
            errorMessage = "InvalidSubscriptionGraceDays " + workFlowBean.getSubsGraceDays();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Validate start date
        if ((errorMessage == null) && BizParamValidator.validateInputStringParam(workFlowBean.getSubsGraceDays())
                && !BizParamValidator.validateSubscritpionStartDate(workFlowBean.getSubsStartDate())) {
            LOGGER.info("validation failed as invalid Subscription start date is Passed");
            errorMessage = "InvalidSubscriptionStartDate " + workFlowBean.getSubsStartDate();
            responseConstant = ResponseConstants.INVALID_SUBS_START_DATE;
        }

        // Validate orderId
        if ((errorMessage == null) && (!BizParamValidator.validateInputStringParam(workFlowBean.getOrderID()))) {
            LOGGER.info("validation failed as invalid OrderId is Passed");
            errorMessage = "InvalidOrderID " + workFlowBean.getOrderID();
            responseConstant = ResponseConstants.INVALID_ORDER_ID;
        }

        /*
         * Validate txnAmount against maxAmount & amountType(FIXED & VARIABLE)
         */

        if ((errorMessage == null)
                && !BizParamValidator.validateSubscritpionAmountType(workFlowBean.getSubsAmountType())) {
            LOGGER.info("validation failed as invalid Subscription Amount Type is Passed");
            errorMessage = "InvalidSubsAmountType " + workFlowBean.getSubsAmountType();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // SUBS_MAX_AMOUNT is Mandatory if SUBS_AMOUNT_TYPE is "VARIABLE"
        if ((errorMessage == null) && workFlowBean.getSubsAmountType().equals(AmountType.VARIABLE.getName())
                && !BizParamValidator.validateInputNumberParam(workFlowBean.getSubsMaxAmount())) {
            LOGGER.info("validation failed as invalid maxAmount is Passed");
            errorMessage = "InvalidSubscriptionMaxAmount " + workFlowBean.getSubsMaxAmount();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // SUBS_MAX_AMOUNT is optional if SUBS_AMOUNT_TYPE is "FIX"
        if ((errorMessage == null) && workFlowBean.getSubsAmountType().equals(AmountType.FIX.getName())
                && !BizParamValidator.validateSubsMaxAmount(workFlowBean.getSubsMaxAmount())) {
            LOGGER.info("validation failed as invalid maxAmount is Passed");
            errorMessage = "InvalidSubscriptionMaxAmount " + workFlowBean.getSubsMaxAmount();
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Business logic check for grace days against frequencyUnit
        if ((errorMessage == null)
                && StringUtils.isNotBlank(workFlowBean.getSubsGraceDays())
                && !BizParamValidator.validateGraceDaysAgainstFrequencyUnit(workFlowBean.getSubsGraceDays(),
                        workFlowBean.getSubsFrequency(), workFlowBean.getSubsFrequencyUnit())) {
            LOGGER.info("validation failed as invalid grace days are Passed");
            errorMessage = "InvalidSubscriptionGraceDays " + workFlowBean.getSubsGraceDays();
        }

        return errorMessage == null ? new GenericCoreResponseBean<Boolean>(true)
                : new GenericCoreResponseBean<Boolean>(errorMessage, responseConstant);
    }
}
