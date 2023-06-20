/**
 * 
 */
package com.paytm.pgplus.biz.core.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("subscriptionS2SCCOnlyNormalValidator")
public class SubscriptionS2SCCOnlyNormalValidator implements IValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionS2SCCOnlyNormalValidator.class);

    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {

        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.info("Validation failed as request bean is null");
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getToken())) {
            LOGGER.info("Validation failed as Token is Blank");
            errorMessage = "InvalidToken";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (workFlowBean.getSubsTypes().equals(SubsTypes.CC_ONLY)
                || workFlowBean.getSubsTypes().equals(SubsTypes.NORMAL)) {
            if ((errorMessage == null) && !BizParamValidator.validateInputStringParam(workFlowBean.getSavedCardID())) {
                LOGGER.info("Validation failed as SavedCardID is Blank");
                errorMessage = "InvalidSavedCardID";
                responseConstant = ResponseConstants.INVALID_SAVED_CARD_ID;
            }
        }

        if ((errorMessage == null) && !BizParamValidator.validateCustId(workFlowBean)) {
            LOGGER.info("validation failed as invalid CustID is Passed");
            errorMessage = "InvalidCustID " + workFlowBean.getCustID();
            responseConstant = ResponseConstants.INVALID_CUST_ID;
        }

        if ((errorMessage == null) && !"0".equals(workFlowBean.getTxnAmount())) {
            LOGGER.info("Validation failed as Txn Amount is Not 0");
            errorMessage = "InvalidTxnAmount";
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if ((errorMessage == null) && StringUtils.isBlank(workFlowBean.getToken())) {
            LOGGER.info("Validation failed as Token is Blank");
            errorMessage = "InvalidSsoToken";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if ((errorMessage == null) && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())
                && workFlowBean.getRequestType().getType().equals("SUBSCRIBE")) {
            LOGGER.info("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType " + workFlowBean.getRequestType();
        }

        if ((errorMessage == null)
                && !BizParamValidator.validateSubscritpionFrequencyUnit(workFlowBean.getSubsFrequencyUnit())) {
            LOGGER.info("validation failed as invalid Subscription Frequency Type is Passed");
            errorMessage = "InvalidSubsFrequencyType " + workFlowBean.getSubsFrequencyUnit();
        }

        if ((errorMessage == null) && !BizParamValidator.validateSubscritpionFrequency(workFlowBean.getSubsFrequency())) {
            LOGGER.info("validation failed as invalid Subscription Frequency is Passed");
            errorMessage = "InvalidSubsFrequency " + workFlowBean.getSubsFrequency();
        }

        if ((errorMessage == null)
                && (!NumberUtils.isNumber(workFlowBean.getSubsFrequency()) || Integer.parseInt(workFlowBean
                        .getSubsFrequency()) <= 0)) {
            LOGGER.info("validation failed as invalid Subscription Frequency is Passed");
            errorMessage = "InvalidSubsFrequency " + workFlowBean.getSubsFrequency();
        }

        if ((errorMessage == null)
                && (!BizParamValidator.validateInputStringParam(workFlowBean.getCustID()) || !StringUtils
                        .isAlphanumeric(workFlowBean.getCustID()))) {
            LOGGER.info("validation failed as invalid CustID is Passed");
            errorMessage = "InvalidCustID " + workFlowBean.getCustID();
        }

        // Validate subscription grace days
        if ((errorMessage == null) && BizParamValidator.validateInputStringParam(workFlowBean.getSubsStartDate())
                && !BizParamValidator.validateInputStringParam(workFlowBean.getSubsGraceDays())) {
            LOGGER.info("validation failed as invalid grace days is Passed");
            errorMessage = "InvalidSubscriptionGraceDays " + workFlowBean.getSubsGraceDays();
        }

        // Validate start date
        if ((errorMessage == null) && BizParamValidator.validateInputStringParam(workFlowBean.getSubsGraceDays())
                && !BizParamValidator.validateSubscritpionStartDate(workFlowBean.getSubsStartDate())) {
            LOGGER.info("validation failed as invalid Subscription start date is Passed");
            errorMessage = "InvalidSubscriptionStartDate " + workFlowBean.getSubsStartDate();
        }

        // Validate orderId
        if ((errorMessage == null) && (!BizParamValidator.validateInputStringParam(workFlowBean.getOrderID()))) {
            LOGGER.info("validation failed as invalid OrderId is Passed");
            errorMessage = "InvalidOrderID " + workFlowBean.getOrderID();
        }

        /*
         * Validate txnAmount against maxAmount & amountType(FIXED & VARIABLE)
         */
        if ((errorMessage == null) && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.info("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount " + workFlowBean.getTxnAmount();
        }

        if ((errorMessage == null)
                && !BizParamValidator.validateSubscritpionAmountType(workFlowBean.getSubsAmountType())) {
            LOGGER.info("validation failed as invalid Subscription Amount Type is Passed");
            errorMessage = "InvalidSubsAmountType" + workFlowBean.getSubsAmountType();
        }

        // SUBS_MAX_AMOUNT is Mandatory if SUBS_AMOUNT_TYPE is "VARIABLE"
        if ((errorMessage == null) && workFlowBean.getSubsAmountType().equals(AmountType.VARIABLE.getName())
                && !BizParamValidator.validateInputNumberParam(workFlowBean.getSubsMaxAmount())) {
            LOGGER.info("validation failed as invalid maxAmount is Passed");
            errorMessage = "InvalidSubscriptionMaxAmount " + workFlowBean.getSubsMaxAmount();
        }

        // SUBS_MAX_AMOUNT is optional if SUBS_AMOUNT_TYPE is "FIX"
        if ((errorMessage == null) && workFlowBean.getSubsAmountType().equals(AmountType.FIX.getName())
                && !BizParamValidator.validateSubsMaxAmount(workFlowBean.getSubsMaxAmount())) {
            LOGGER.info("validation failed as invalid maxAmount is Passed");
            errorMessage = "InvalidSubscriptionMaxAmount " + workFlowBean.getSubsMaxAmount();
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
