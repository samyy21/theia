package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cashier.enums.PaymentMode;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.enums.AppInvokeType;
import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SubscriptionValidationException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.AOA_SUBSCRIPTION_PAYMODES;

/**
 * Created by chitrasinghal on 12/4/18. Updated by himanshu3.garg on 31/1/19
 */

@Service("subscriptionNativeValidationService")
public class SubscriptionNativeValidationService implements ISusbcriptionNativeValidationService {

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public void validate(SubscriptionTransactionRequest request) {
        if (request.getBody().getExtendInfo() == null) {
            request.getBody().setExtendInfo(new ExtendInfo());
        }
        validateFrequencyUnit(request);
        validateSubsAmountType(request);
        validateCustId(request);
        /*
         * Commented below to support 0 amount transaction in Mandate creation.
         * Below logic will be considered in PTC flow
         */
        // validateSubsMinAmount(request);
        validateAndSetSubsFrequency(request);
        validateSubsMaxAmount(request);
        /*
         * commented below code as this is to be validated only in cases other
         * than ONDEMAND subscription which is already handled below
         */
        // validateBusinessLogic(request);
        validateSubsPPIOnly(request);
        validateSubsPayMode(request);
        validatePPILimit(request);
        // vaildateTxnAmtForMandate(request);
        validateAppInvokeDevice(request);
        validateSubsRetryEnabled(request);
        validateSubsPayModeAndAmount(request);

        if (!FrequencyUnit.ONDEMAND.getName().equals(request.getBody().getSubscriptionFrequencyUnit())
                && !request.getBody().isFlexiSubscription()) {
            validateGraceDays(request);
            validateSubsStartDate(request);
            validateBusinessLogic(request);
            validateFrequency(request);
        }

        if (PaymentMode.UPI.getMode().equals(request.getBody().getSubscriptionPaymentMode())) {
            validateUpiLimit(request);

            if (!FrequencyUnit.ONDEMAND.getName().equals(request.getBody().getSubscriptionFrequencyUnit())
                    && !request.getBody().isFlexiSubscription()) {
                setTrialAmountCheckForUpi(request);
                validateUpifrequencyCycle(request);
                // to support trial in upi commenting this check
                // validateUpiSubsStartDate(request);
                validateUpiGraceDays(request);
                validateMonthlyUpiFrequency(request);
                validateUpiRetryCount(request);
                validateUpiSubsStartDate(request);
                validateUpiSubsFrequency(request);
            }
        }

    }

    @Override
    public void validateAOASubscriptionRequest(SubscriptionTransactionRequest request) {
        if (request.getBody().getExtendInfo() == null) {
            request.getBody().setExtendInfo(new ExtendInfo());
        }
        validateAOASubsPayMode(request);
        validateFrequencyUnit(request);
        validateSubsAmountType(request);
        validateCustId(request);
        validateAndSetSubsFrequency(request);
        validateSubsMaxAmount(request);
        validateAppInvokeDevice(request);
        validateSubsRetryEnabled(request);
        validateAOASubsForUPIPaymode(request);
    }

    private void validateAppInvokeDevice(SubscriptionTransactionRequest request) {
        if (StringUtils.isNotEmpty(request.getBody().getAppInvokeDevice())
                && AppInvokeType.getAppInvokeType(request.getBody().getAppInvokeDevice()) == null) {
            throw SubscriptionValidationException.getException("Invalid AppInvokeDevice");
        }
    }

    private void validatePPILimit(SubscriptionTransactionRequest request) {
        String subscriptionPaymentMode = request.getBody().getSubscriptionPaymentMode();
        String subscriptionMaxAmount = request.getBody().getSubscriptionMaxAmount();
        if (nativeSubscriptionHelper.subsPPIAmountLimitBreached(subscriptionPaymentMode, subscriptionMaxAmount, request
                .getBody().getMid())) {
            throw RequestValidationException.getException(ResultCode.SUBSCRIPTION_AMOUNT_LIMIT_FAILURE);
        }
    }

    private void validateBusinessLogic(SubscriptionTransactionRequest request) {

        // Business logic check for grace days against frequencyUnit
        if (StringUtils.isNotBlank(request.getBody().getSubscriptionGraceDays())
                && !BizParamValidator.validateGraceDaysAgainstFrequencyUnit(request.getBody()
                        .getSubscriptionGraceDays(), request.getBody().getSubscriptionFrequency(), request.getBody()
                        .getSubscriptionFrequencyUnit())) {
            throw SubscriptionValidationException
                    .getException("Grace days cannot be greater than the frequency set against the subscription");
        }
    }

    private void validateSubsAmountType(SubscriptionTransactionRequest request) {
        if (!BizParamValidator.validateSubscritpionAmountType(request.getBody().getSubscriptionAmountType())) {
            throw SubscriptionValidationException.getException("Invalid Subscription Amount Type");
        }
    }

    public void validateGraceDays(SubscriptionTransactionRequest request) {
        if (BizParamValidator.validateInputStringParam(request.getBody().getSubscriptionStartDate())
                && !BizParamValidator.validateInputStringParam(request.getBody().getSubscriptionGraceDays())) {
            throw SubscriptionValidationException.getException("Grace days value is mandatory");
        }
    }

    public void validateCustId(SubscriptionTransactionRequest request) {
        if (request.getBody().getUserInfo() == null
                || !BizParamValidator.validateSubscriptionFlowCustId(request.getBody().getUserInfo().getCustId())) {
            throw SubscriptionValidationException.getException("Invalid Customer Id");
        }
    }

    public void validateSubsStartDate(SubscriptionTransactionRequest request) {
        if (BizParamValidator.validateInputStringParam(request.getBody().getSubscriptionGraceDays())
                && !BizParamValidator.validateSubscritpionStartDate(request.getBody().getSubscriptionStartDate())) {
            throw SubscriptionValidationException.getException("Invalid Subscription start date");
        }
    }

    public void validateFrequencyUnit(SubscriptionTransactionRequest request) {
        if (!BizParamValidator.validateSubscritpionFrequencyUnit(request.getBody().getSubscriptionFrequencyUnit())) {
            throw SubscriptionValidationException.getException("Invalid Frequency Unit");
        }
    }

    public void validateFrequency(SubscriptionTransactionRequest request) {
        if (!BizParamValidator.validateSubscritpionFrequency(request.getBody().getSubscriptionFrequency())) {
            throw SubscriptionValidationException.getException("Invalid Frequency");
        }
    }

    public void validateSubsMinAmount(SubscriptionTransactionRequest initiateTransactionRequest) {

        BigDecimal subsMinAmount = new BigDecimal(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1"));
        BigDecimal txnAmount = new BigDecimal(initiateTransactionRequest.getBody().getTxnAmount().getValue());
        if (subsMinAmount.compareTo(txnAmount) > 0) {
            initiateTransactionRequest.getBody().setTxnAmount(new Money(subsMinAmount.toPlainString()));
            initiateTransactionRequest.getBody().getExtendInfo()
                    .setAmountToBeRefunded(subsMinAmount.subtract(txnAmount).toPlainString());
        } else {
            initiateTransactionRequest.getBody().setTxnAmount(new Money(txnAmount.toPlainString()));
        }
    }

    public void validateSubsMaxAmount(SubscriptionTransactionRequest request) {
        // SUBS_MAX_AMOUNT is Mandatory if SUBS_AMOUNT_TYPE is "VARIABLE"
        if (request.getBody().getSubscriptionAmountType().equals(AmountType.VARIABLE.getName())
                && !BizParamValidator.validateInputNumberParam(request.getBody().getSubscriptionMaxAmount())) {
            throw SubscriptionValidationException.getException("Invalid Max Amount");
        }

        // SUBS_MAX_AMOUNT is optional if SUBS_AMOUNT_TYPE is "FIX"
        if (request.getBody().getSubscriptionAmountType().equals(AmountType.FIX.getName())
                && !BizParamValidator.validateSubsMaxAmount(request.getBody().getSubscriptionMaxAmount())) {
            throw SubscriptionValidationException.getException("Invalid Max Amount");
        }

        if (!BizParamValidator.validateSubMaxAmount(request.getBody().getSubscriptionMaxAmount())) {

            throw RequestValidationException.getException(ResultCode.INVALID_SUBSCRIPTION_MAX_AMOUNT);
        }

        if (AmountType.FIX.getName().equals(request.getBody().getSubscriptionAmountType())
                && StringUtils.isBlank(request.getBody().getSubscriptionMaxAmount())) {
            request.getBody().setSubscriptionMaxAmount(request.getBody().getTxnAmount().getValue());
        }
    }

    public void validateSubsPayModeAndAmount(SubscriptionTransactionRequest request) {
        String subsPayMode = request.getBody().getSubscriptionPaymentMode();
        if (StringUtils.isNotBlank(subsPayMode)) {
            SubsPaymentMode subsPaymentMode = SubsPaymentMode.getSubsPaymentMode(subsPayMode);
            // PGP-27209 - Throw exception if Txn Amount is greater than Max
            // Amount or Renewal Amount, in case of UPI and Mandate.
            if (SubsPaymentMode.UPI.equals(subsPaymentMode) || SubsPaymentMode.BANK_MANDATE.equals(subsPaymentMode)) {
                if (isTxnAmountGreaterThanMaxOrRenewalAmt(request.getBody())) {
                    throw RequestValidationException
                            .getException(ResultCode.INVALID_PAYMODE_WHEN_TXN_AMOUNT_GT_RENEWAL_AMOUNT);
                }
                if (AmountType.getEnumByName(request.getBody().getSubscriptionAmountType()).equals(AmountType.FIX)
                        && !(SubsPaymentMode.BANK_MANDATE.equals(subsPaymentMode) && isTxnAmountZero(request.getBody()))
                        && isTxnAmountLessThanMaxOrRenewalAmt(request.getBody())) {
                    throw RequestValidationException.getException(ResultCode.INVALID_SUBS_PAY_MODE_GREATER_TXN_AMOUNT);
                }
            }
        }
    }

    public boolean isTxnAmountGreaterThanMaxOrRenewalAmt(SubscriptionTransactionRequestBody requestBody) {
        if (requestBody.getTxnAmount() != null && StringUtils.isNotBlank(requestBody.getTxnAmount().getValue())) {
            if (StringUtils.isNotBlank(requestBody.getSubscriptionMaxAmount())
                    && Double.valueOf(requestBody.getTxnAmount().getValue()) > Double.valueOf(requestBody
                            .getSubscriptionMaxAmount())) {
                return true;
            }
            if (StringUtils.isNotBlank(requestBody.getRenewalAmount())
                    && Double.valueOf(requestBody.getTxnAmount().getValue()) > Double.valueOf(requestBody
                            .getRenewalAmount())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTxnAmountZero(SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        if (subscriptionTransactionRequestBody.getTxnAmount() != null
                && StringUtils.isNotBlank(subscriptionTransactionRequestBody.getTxnAmount().getValue())) {
            if (Double.parseDouble(subscriptionTransactionRequestBody.getTxnAmount().getValue()) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isTxnAmountLessThanMaxOrRenewalAmt(SubscriptionTransactionRequestBody requestBody) {
        if (requestBody.getTxnAmount() != null && StringUtils.isNotBlank(requestBody.getTxnAmount().getValue())) {
            // max amount not considered when renewal amount is part of request
            if (StringUtils.isNotBlank(requestBody.getRenewalAmount())) {
                return Double.parseDouble(requestBody.getTxnAmount().getValue()) < Double.parseDouble(requestBody
                        .getRenewalAmount());
            } else if (StringUtils.isNotBlank(requestBody.getSubscriptionMaxAmount())) {
                return Double.parseDouble(requestBody.getTxnAmount().getValue()) < Double.parseDouble(requestBody
                        .getSubscriptionMaxAmount());
            }
        }
        return false;
    }

    public void validateSubsPayMode(SubscriptionTransactionRequest request) {
        String subsPayMode = request.getBody().getSubscriptionPaymentMode();
        if (StringUtils.isBlank(subsPayMode)) {
            request.getBody().setSubscriptionPaymentMode(SubsPaymentMode.UNKNOWN.name());
        } else if (SubsPaymentMode.getSubsPaymentMode(subsPayMode) == null) {
            throw RequestValidationException.getException(ResultCode.INVALID_SUBSCRIPTION_PAYMENT_MODE);
        } else if (merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(request.getBody().getMid())
                && SubsPaymentMode.NORMAL.equals(SubsPaymentMode.getSubsPaymentMode(subsPayMode))) {
            throw RequestValidationException.getException(ResultCode.SUBSCRIPTION_NOT_ALLOWED_FOR_NORMAL_PAYMODE);
        }
        if (SubsPaymentMode.CC.equals(SubsPaymentMode.getSubsPaymentMode(subsPayMode))
                || SubsPaymentMode.DC.equals(SubsPaymentMode.getSubsPaymentMode(subsPayMode))) {
            if (nativeSubscriptionHelper.invalidGraceDaysForCard(request.getBody().getSubscriptionGraceDays())) {
                throw RequestValidationException.getException(ResultCode.INVALID_SUBS_PAYMNT_MODE);
            }
        }
    }

    public void validateSubsPPIOnly(SubscriptionTransactionRequest request) {
        String subsPayMode = request.getBody().getSubscriptionPaymentMode();
        String subsPPIOnly = request.getBody().getSubsPPIOnly();
        if (StringUtils.isBlank(subsPayMode) && "Y".equalsIgnoreCase(subsPPIOnly)) {
            request.getBody().setSubscriptionPaymentMode(PaymentMode.PPI.getMode());
        } else if (StringUtils.isBlank(subsPayMode) && StringUtils.isNotBlank(subsPPIOnly)) {
            throw RequestValidationException.getException(ResultCode.INVALID_SUBS_PAYMNT_MODE);
        } else if (PaymentMode.PPI.getMode().equals(subsPayMode) && "N".equalsIgnoreCase(subsPPIOnly)) {
            request.getBody().setSubsPPIOnly("Y");
        }
    }

    private void setTrialAmountCheckForUpi(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        // check using converting to double with two precison decimal
        if (AmountUtils.formatNumberToTwoDecimalPlaces(
                subscriptionTransactionRequest.getBody().getTxnAmount().getValue()).equals("0.00")) {

            if (!merchantPreferenceService.isAutoRefundPreferenceEnabled(subscriptionTransactionRequest.getBody()
                    .getMid())) {
                subscriptionTransactionRequest.getBody().getTxnAmount().setValue("1");
            }
            subscriptionTransactionRequest.getBody().getTxnAmount().setCurrency(EnumCurrency.INR);
        }
    }

    private void validateUpiLimit(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        validateSubsMaxAmount(subscriptionTransactionRequest);
        if (nativeSubscriptionHelper.subsUPIAmountLimitBreached(subscriptionTransactionRequest.getBody()
                .getSubscriptionMaxAmount())) {
            throw SubscriptionValidationException.getException("Subscription Amount Limit For UPI Breached");
        }
    }

    private void validateUpifrequencyCycle(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        validateFrequencyUnit(subscriptionTransactionRequest);
        if (nativeSubscriptionHelper.invalidUpifrequencyCycle(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequencyUnit())) {
            throw SubscriptionValidationException.getException("Invalid subscriptionFrequencyUnit");
        }
    }

    public void validateUpiSubsStartDate(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        if (!BizParamValidator.validateSubscritpionStartDate(subscriptionTransactionRequest.getBody()
                .getSubscriptionStartDate())
                || nativeSubscriptionHelper.invalidUpiSubsStartDate(subscriptionTransactionRequest.getBody()
                        .getSubscriptionStartDate(), subscriptionTransactionRequest.getBody()
                        .getSubscriptionFrequencyUnit())) {
            throw SubscriptionValidationException.getException("Invalid subscription start date");
        }
    }

    private void validateUpiGraceDays(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        validateFrequencyUnit(subscriptionTransactionRequest);
        validateGraceDays(subscriptionTransactionRequest);
        if (nativeSubscriptionHelper.invalidUpiGraceDays(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequencyUnit(), subscriptionTransactionRequest.getBody().getSubscriptionGraceDays(),
                subscriptionTransactionRequest.getBody().getSubscriptionFrequency()))
            throw SubscriptionValidationException.getException("Invalid grace days");

    }

    private void validateMonthlyUpiFrequency(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        validateFrequencyUnit(subscriptionTransactionRequest);
        validateFrequency(subscriptionTransactionRequest);
        if (nativeSubscriptionHelper.subsUpiMonthlyFrequencyBreach(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequencyUnit(), subscriptionTransactionRequest.getBody().getSubscriptionFrequency())) {
            throw SubscriptionValidationException.getException("Invalid Subscription frequency");
        }
    }

    private void validateUpiRetryCount(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        if (nativeSubscriptionHelper.invalidSubsRetryCount(subscriptionTransactionRequest.getBody()
                .getSubscriptionEnableRetry(), subscriptionTransactionRequest.getBody().getSubscriptionRetryCount())) {
            throw SubscriptionValidationException.getException("Invalid Subscription retry count");
        }
    }

    private void validateUpiSubsFrequency(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        validateFrequency(subscriptionTransactionRequest);
        if (nativeSubscriptionHelper.invalidUpiSubsFrequency(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequency()))
            throw SubscriptionValidationException.getException("Invalid Subscription Frequency");
    }

    public void validateAndSetSubsFrequency(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        if (!(BizParamValidator.validateInputStringParam(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequency()) && StringUtils.isNumeric(subscriptionTransactionRequest.getBody()
                .getSubscriptionFrequency()))) {
            subscriptionTransactionRequest.getBody().setSubscriptionFrequency("1");
        }

    }

    public void validateSubsRetryEnabled(SubscriptionTransactionRequest subscriptionTransactionRequest) {
        if (StringUtils.isNumeric(subscriptionTransactionRequest.getBody().getSubscriptionEnableRetry())
                && Integer.parseInt(subscriptionTransactionRequest.getBody().getSubscriptionEnableRetry()) > 1) {
            subscriptionTransactionRequest.getBody().setSubscriptionEnableRetry("1");
        } else if (StringUtils.equalsIgnoreCase("true", subscriptionTransactionRequest.getBody()
                .getSubscriptionEnableRetry())) {
            subscriptionTransactionRequest.getBody().setSubscriptionEnableRetry("1");
        } else if (StringUtils.equalsIgnoreCase("false", subscriptionTransactionRequest.getBody()
                .getSubscriptionEnableRetry())) {
            subscriptionTransactionRequest.getBody().setSubscriptionEnableRetry("0");
        }
    }

    public boolean isDailySubscription(SubscriptionTransactionRequest request) {
        return FrequencyUnit.DAY.getName().equals(request.getBody().getSubscriptionFrequencyUnit());
    }

    public boolean isDailySubscription(SubscriptionTransactionRequestBody request) {
        return FrequencyUnit.DAY.getName().equals(request.getSubscriptionFrequencyUnit());
    }

    public boolean checkIfTxnAmountLessThanMinAmt(SubscriptionTransactionRequestBody requestBody) {
        Double subsMinAmount = new Double(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1"));

        if (requestBody.getTxnAmount() != null && StringUtils.isNotBlank(requestBody.getTxnAmount().getValue())) {
            if (Double.valueOf(requestBody.getTxnAmount().getValue()) < subsMinAmount
                    && Double.valueOf(requestBody.getTxnAmount().getValue()) != 0)
                return true;
        }
        return false;
    }

    public void validateAOASubsPayMode(SubscriptionTransactionRequest request) {
        String subsPayMode = request.getBody().getSubscriptionPaymentMode();
        String paymodeEligibleForAOASubscriptionFlow = ff4jUtils.getPropertyAsStringWithDefault(
                AOA_SUBSCRIPTION_PAYMODES, StringUtils.EMPTY);
        Set<String> paymodeEligibleForAOASubscription = new HashSet<>(
                Arrays.asList(paymodeEligibleForAOASubscriptionFlow.split(",")));
        if (SubsPaymentMode.getSubsPaymentMode(subsPayMode) == null
                || BooleanUtils.isFalse(paymodeEligibleForAOASubscription.contains(subsPayMode))) {
            throw RequestValidationException.getException(ResultCode.INVALID_SUBSCRIPTION_PAYMENT_MODE);
        }
    }

    public void validateAOASubsForUPIPaymode(SubscriptionTransactionRequest request) {
        if (PaymentMode.UPI.getMode().equals(request.getBody().getSubscriptionPaymentMode())) {
            validateUpiLimit(request);

            if (!FrequencyUnit.ONDEMAND.getName().equals(request.getBody().getSubscriptionFrequencyUnit())) {
                setTrialAmountCheckForUpi(request);
                validateUpifrequencyCycle(request);
                validateUpiGraceDays(request);
                validateMonthlyUpiFrequency(request);
                validateUpiRetryCount(request);
                validateUpiSubsStartDate(request);
                validateUpiSubsFrequency(request);
            }
        }
    }

}