/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.validator;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.*;
import com.paytm.pgplus.biz.exception.InvalidParameterException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.signature.utility.UtilityConstants;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author namanjain
 *
 */
public class BizParamValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizParamValidator.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BizParamValidator.class);
    private static final int DIGITAL_CREDIT_PAYMENT_DETAILS_ARRAY_LENGTH = 3;
    private static final int VPA_LENGTH = 2;

    /**
     * 
     * This method is used to validate any Input String Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank
     */
    public static boolean validateInputStringParam(final String inputParam) {
        if (StringUtils.isBlank(inputParam)) {
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate any Input Object Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null
     */
    public static boolean validateInputObjectParam(final Object inputParam) {
        if (inputParam == null) {
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate any Input List Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static boolean validateInputListParam(final List<?> inputParam) {
        if ((inputParam == null) || inputParam.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * This method is used to validate any Input Map Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static boolean validateInputMapParam(final Map<?, ?> inputParam) {
        if ((inputParam == null) || inputParam.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate an Input Number Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank, or not numeric
     */
    public static boolean validateInputNumberParam(final String inputParam) {
        if (!validateInputStringParam(inputParam) || !NumberUtils.isNumber(inputParam)) {
            return false;
        }
        return true;
    }

    public static boolean validateChannelID(final String inputParam) {
        LOGGER.debug("BeanParamValidator for ChannelParam::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !EnumUtils.isValidEnum(EChannelId.class, inputParam)) {
            LOGGER.error("ChannelParam is null or Blank or InvalidEnum ::{}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateRequestType(final String inputParam) {
        LOGGER.debug("BeanParamValidator for RequestTypeParam::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !EnumUtils.isValidEnum(ERequestType.class, inputParam)) {
            LOGGER.error("RequestTypeParam is null or Blank or InvalidEnum ::{}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateIndustryTypeID(final String inputParam) {
        LOGGER.debug("BeanParamValidator for IndustryTypeIDParam::{}", inputParam);
        if (!validateInputStringParam(inputParam)) {
            LOGGER.error("IndustryTypeIDParam is null or Blank ::{}", inputParam);
        }

        return true;
    }

    public static boolean validateWebsite(final String inputParam) {
        LOGGER.debug("BeanParamValidator for WebsiteParam::{}", inputParam);
        if (!validateInputStringParam(inputParam)) {
            LOGGER.error("WebsiteParam is null or Blank ::{}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateCallbackUrls(final ExtendedInfoRequestBean extendedInfoRequestBean) {
        LOGGER.debug(
                "BeanParamValidator for Callback URLs callbackURL : {} , successCallbackUrl : {} , failureCallbackUrl : {} , pendingCallbackUrl : {} ",
                extendedInfoRequestBean.getCallBackURL(), extendedInfoRequestBean.getSuccessCallBackURL(),
                extendedInfoRequestBean.getFailureCallBackURL(), extendedInfoRequestBean.getPendingCallBackURL());
        if (StringUtils.isBlank(extendedInfoRequestBean.getCallBackURL())) {
            if (StringUtils.isBlank(extendedInfoRequestBean.getSuccessCallBackURL())) {
                LOGGER.error("SuccessCallbackUrl is not present");
                return false;
            }
            if (StringUtils.isBlank(extendedInfoRequestBean.getPendingCallBackURL())) {
                LOGGER.error("PendingCallbackUrl is not present");
                return false;
            }
            if (StringUtils.isBlank(extendedInfoRequestBean.getFailureCallBackURL())) {
                LOGGER.error("FailureCallbackUrl is not present");
                return false;
            }
        }
        return true;
    }

    public static boolean validateSeamlessPaymentDetails(final String inputParam) {
        LOGGER.debug("BeanParamValidator for PaymentDetails::{}", inputParam);
        if (!validateInputStringParam(inputParam)) {
            LOGGER.error("PaymentDetails is null or Blank");
            return false;
        }

        return true;
    }

    public static boolean validateAuthMode(final String inputParam) {
        LOGGER.debug("BeanParamValidator for AuthMode::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !validateForAuthModeEnum(inputParam)) {
            LOGGER.error("AuthMode is null or Blank or InvalidEnum : {}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateForAuthModeEnum(final String inputParam) {
        for (AuthModeEnum enumValue : AuthModeEnum.values()) {
            if (enumValue.getValue().equalsIgnoreCase(inputParam)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validatePaymentTypeId(final String inputParam) {
        LOGGER.debug("BeanParamValidator for PaymentTypeId::{}", inputParam);
        if (!validateInputStringParam(inputParam) && !EnumUtils.isValidEnum(PaymentTypeIdEnum.class, inputParam)) {
            LOGGER.error("PaymentTypeId is null or Blank");
            return false;
        }

        return true;
    }

    public static boolean validateSubscritpionAmountType(final String inputParam) {
        LOGGER.debug("BeanParamValidator for SubscritpionAmountType::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !EnumUtils.isValidEnum(AmountType.class, inputParam)) {
            LOGGER.error("SubscritpionAmountType is null or Blank or InvalidEnum : {}", inputParam);
            return false;
        }
        return true;
    }

    public static boolean validateSubscritpionFrequencyUnit(final String inputParam) {
        LOGGER.debug("BeanParamValidator for SubscritpionFrequencyUnit::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !EnumUtils.isValidEnum(FrequencyUnit.class, inputParam)) {
            LOGGER.error("SubscritpionFrequencyUnit is null or Blank or InvalidEnum : {}", inputParam);
            return false;
        }

        return true;
    }

    // Validating Subscription frequency
    public static boolean validateSubscritpionFrequency(final String inputParam) {
        if (!validateInputStringParam(inputParam) || Integer.parseInt(inputParam) < 0) {
            LOGGER.error("SubscritpionFrequency is null or Blank or Invalid ::{}", inputParam);
            return false;
        }

        return true;
    }

    // Validating Subscription start date
    public static boolean validateSubscritpionStartDate(final String inputParam) {
        LOGGER.debug("BeanParamValidator for SubscritpionStartDate::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !validateSubsStartDate(inputParam)) {
            LOGGER.error("SubscritpionStartDate is null or Blank or Invalid ::{}", inputParam);
            return false;
        }
        return true;
    }

    private static boolean validateSubsStartDate(final String inputParam) {
        try {
            Date todayDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date subsDate = sdf.parse(inputParam);
            if (subsDate.before(sdf.parse(sdf.format(todayDate)))) {
                return false;
            }
        } catch (ParseException e) {
            LOGGER.error("exception while parsing subscription start date", e);
            return false;
        }

        return true;
    }

    // Validating txnAmount against MaxAmount & amountType
    public static boolean validateTxnAmtAgainstSubsMaxAmt(final String txnAmount, final String subsMaxAmount,
            final String subsAmountType) {
        if (subsAmountType.equals(AmountType.VARIABLE.getName())
                && (Double.parseDouble(txnAmount) > Double.parseDouble(subsMaxAmount))) {
            return false;
        }

        return true;
    }

    // Validating txnAmount against MaxAmount & amountType
    public static boolean validateTxnAmtAgainstSubsMaxAmt(final String txnAmount, final String subsMaxAmount,
            final String subsAmountType, final String payMode) {
        // Default check in either of Amount type Fix or Variable
        if (AmountType.VARIABLE.getName().equals(subsAmountType)
                && Double.parseDouble(txnAmount) > Double.parseDouble(subsMaxAmount)) {
            return false;
        }

        return true;
    }

    public static boolean validateSubMaxAmount(final String subsMaxAmount) {

        if (validateInputStringParam(subsMaxAmount) && Double.parseDouble(subsMaxAmount) == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateGraceDaysAgainstFrequencyUnit(String graceDays, String frequency, String frequencyUnit) {
        FrequencyUnit frequencyUnitbyName = FrequencyUnit.getFrequencyUnitbyName(frequencyUnit);
        int freq = Integer.parseInt(frequency);
        if (null != frequencyUnitbyName) {
            if (FrequencyUnit.DAY != frequencyUnitbyName
                    && Integer.parseInt(graceDays) <= (frequencyUnitbyName.getMultiplier() * freq))
                return Boolean.TRUE;
            else if (FrequencyUnit.DAY == frequencyUnitbyName && Integer.parseInt(graceDays) < freq)
                return Boolean.TRUE;
            else
                return Boolean.FALSE;
        }
        return Boolean.FALSE;

    }

    public static boolean validateSubsMaxAmount(String inputParam) {
        if (validateInputStringParam(inputParam) && !NumberUtils.isNumber(inputParam)) {
            return false;
        }
        return true;

    }

    // End

    // Validating Optional parameters of Seamless/Seamless_Native
    public static boolean validateCardType(final String inputParam) {
        LOGGER.debug("BeanParamValidator for CardType::{}", inputParam);
        if (validateInputStringParam(inputParam) && !EnumUtils.isValidEnum(CardScheme.class, inputParam)) {
            LOGGER.error("CardType is InvalidEnum : {}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateVerifiedBy(final String inputParam) {
        LOGGER.debug("BeanParamValidator for VerifiedBy::{}", inputParam);
        if (validateInputStringParam(inputParam) && !EnumUtils.isValidEnum(VerifiedByEnum.class, inputParam)) {
            LOGGER.error("VerifiedBy is InvalidEnum : {}", inputParam);
            return false;
        }

        return true;
    }

    public static boolean validateBankCodeForNB(final WorkFlowRequestBean workFlowBean) {
        if (validateInputStringParam(workFlowBean.getPaymentTypeId())) {
            if (workFlowBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)
                    && !validateInputStringParam(workFlowBean.getBankCode())) {
                LOGGER.error("BankCode is null or Blank  or InvalidEnum : {}", workFlowBean.getBankCode());
                return false;
            }
        }
        return true;
    }

    public static boolean validateStoreCard(final WorkFlowRequestBean workFlowBean) {

        String paymentTypeId = workFlowBean.getPaymentTypeId();
        String storeCardVal = workFlowBean.getStoreCard();

        if (PaymentTypeIdEnum.IMPS.getValue().equals(paymentTypeId)
                || PaymentTypeIdEnum.CC.getValue().equals(paymentTypeId)
                || PaymentTypeIdEnum.DC.getValue().equals(paymentTypeId)) {

            if (StringUtils.isBlank(storeCardVal)) {
                storeCardVal = "0";
            }

            if (!validateInputNumberParam(storeCardVal) || !validateForStoreCardEnum(storeCardVal)) {
                LOGGER.debug("StoreCard is null or Blank or InvalidEnum ::{}", workFlowBean.getStoreCard());
                return false;
            }
        }
        return true;
    }

    public static boolean validateForStoreCardEnum(final String inputParam) {
        for (StoreCardEnum enumValue : StoreCardEnum.values()) {
            if (enumValue.getValue().equalsIgnoreCase(inputParam)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateCustId(final WorkFlowRequestBean workFlowRequestBean) {
        if (StringUtils.isNotBlank(workFlowRequestBean.getCustID())) {
            if (workFlowRequestBean.getCustID().matches(BizConstant.CUST_ID_REGEX)) {
                return true;
            }
        }
        LOGGER.info("Cust ID : {} is not a valid value.", workFlowRequestBean.getCustID());
        return false;
    }

    public static boolean validateCustId(final String custId) {
        if (StringUtils.isNotBlank(custId)) {
            if (custId.matches(BizConstant.CUST_ID_REGEX)) {
                return true;
            }
        }
        LOGGER.info("Cust ID : {} is not a valid value.", custId);
        return false;
    }

    public static boolean validateDigitalCreditRequest(final WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean == null) {
            return false;
        }
        if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            if (workFlowRequestBean.getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY
                    || !isValidPaymentDetailsForDigitalCredit(workFlowRequestBean)) {
                LOGGER.info("Invalid digital credit request");
                return false;
            }
        }
        return true;
    }

    public static boolean validateDigitalCreditRequestAddnPay(final WorkFlowRequestBean workFlowRequestBean) {
        if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            if ((workFlowRequestBean.getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY && !workFlowRequestBean
                    .isPostPaidOnAddnPay()) || !isValidPaymentDetailsForDigitalCredit(workFlowRequestBean)) {
                LOGGER.info("Invalid digital credit request");
                return false;
            }
        }
        return true;
    }

    // ajay
    private static boolean isValidPaymentDetailsForDigitalCredit(WorkFlowRequestBean flowRequestBean) {
        boolean isValidPaymentDetails = false;
        if (StringUtils.isNotEmpty(flowRequestBean.getPaymentDetails())) {
            String[] paymentDetailsArr = flowRequestBean.getPaymentDetails().split(Pattern.quote("|"));

            if (paymentDetailsArr.length == 2 && StringUtils.isNotEmpty(paymentDetailsArr[0])
                    && StringUtils.isNotEmpty(paymentDetailsArr[1])) {
                isValidPaymentDetails = true;
            } else if (paymentDetailsArr.length == DIGITAL_CREDIT_PAYMENT_DETAILS_ARRAY_LENGTH
                    && StringUtils.isNotEmpty(paymentDetailsArr[0]) && StringUtils.isNotEmpty(paymentDetailsArr[1])
                    && StringUtils.isNotEmpty(paymentDetailsArr[2])) {
                isValidPaymentDetails = true;
            }

        }
        if (!isValidPaymentDetails) {
            LOGGER.info("Payment details are not valid in digital credit request");
        }
        return isValidPaymentDetails;
    }

    public static boolean isValidPaytmVPA(WorkFlowRequestBean flowRequestBean) {
        boolean isValidPaytmVPA = false;
        if (StringUtils.isNotEmpty(flowRequestBean.getVirtualPaymentAddress())) {
            String[] paymentDetailsArr = flowRequestBean.getPaymentDetails().split(Pattern.quote("@"));
            if (paymentDetailsArr.length == VPA_LENGTH && StringUtils.isNotEmpty(paymentDetailsArr[0])
                    && StringUtils.isNotEmpty(paymentDetailsArr[1])
                    && BizConstant.PAYTM_VPA_HANDLE_NAME.equals(paymentDetailsArr[1])) {
                return true;
            }
        }
        LOGGER.info("Invalid Paytm VPA obtained in UPI Push request");
        return isValidPaytmVPA;
    }

    public static void main(String[] args) {
        double d1 = 2.09;
        double d2 = 3.02;

    }

    public static boolean validateTransactionAmount(final String inputParam) {
        if (StringUtils.isNotBlank(inputParam) && !NumberUtils.isNumber(inputParam)) {
            return false;
        }
        return true;
    }

    public static boolean validateTipAmount(final WorkFlowRequestBean workFlowBean) {
        if (StringUtils.isNotBlank(workFlowBean.getTipAmount())) {
            if (!NumberUtils.isNumber(workFlowBean.getTipAmount())
                    || Double.parseDouble(workFlowBean.getTipAmount()) > Double.parseDouble(workFlowBean
                            .getOrderAmount())) {
                LOGGER.info("Tip Amount : {} is greater than Order Amount: {} ", workFlowBean.getTipAmount(),
                        workFlowBean.getTxnAmount());
                return false;
            }
        }
        return true;
    }

    public static boolean validateInputStringLength(String key, String data, int dataLength) {
        boolean ret = true;
        if (StringUtils.isNotBlank(data) && data.length() > dataLength) {
            ret = false;
            // LOGGER.info("Max Length constraint breach for key : {} , value : {} , Required length : {}",
            // key, data,
            // dataLength);
            EXT_LOGGER.customInfo("Max Length constraint breach for key : {} , value : {} , Required length : {}", key,
                    data, dataLength);
        }
        if (StringUtils.isNotBlank(data) && UtilityConstants.NULL_TEXT.equalsIgnoreCase(data)) {
            ret = false;
            // LOGGER.info("Null value for key : {} , value : {} , Required length : {}",
            // key, data,
            // dataLength);
            EXT_LOGGER.customInfo("Null value for key : {} , value : {} , Required length : {}", key, data, dataLength);
        }
        return ret;
    }

    public static boolean validateSubscriptionFlowCustId(final String custId) {
        if (StringUtils.isNotBlank(custId)) {
            if (custId.matches(BizConstant.SUBS_CUST_ID_REGEX)) {
                return true;
            }
        }
        LOGGER.info("Cust ID : {} is not a valid value.", custId);
        return false;
    }

}
