package com.paytm.pgplus.cashier.validator;

import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.pay.model.BusinessLogicValidation;
import com.paytm.pgplus.cashier.util.InputValidator;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.pgproxycommon.utils.ValidationMessage;

/**
 * Utility class that provides all types of business validations for Cashier
 * module<br/>
 * Do not use as a singleton.
 *
 * @author Lalit Mehra
 * @since March 8, 2016
 *
 */
public class ValidationHelper {

    private static final String ONE = "1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHelper.class);

    private BusinessLogicValidation input;

    private final MultivaluedMap<ValidationMessage, String> errors;

    public ValidationHelper(BusinessLogicValidation input) {
        this.input = input;
        errors = new MultivaluedHashMap<ValidationMessage, String>();
    }

    public ValidationHelper validateTxnAmountValid() {
        if (!InputValidator.validateAmount(input.getAmount())) {
            errors.add(ValidationMessage.ERROR_MSG, "Invalid Amount");
            errors.add(ValidationMessage.INVALID_TXN_AMOUNT, "Invalid Amount");
        }
        return this;
    }

    public ValidationHelper validateBinNumber() {
        if ((input.getBinCard() != null) && !input.getBinCard().isActive()) {
            errors.add(ValidationMessage.ERROR_MSG, "Card not supported.Please use another card");
            errors.add(ValidationMessage.INVALID_BIN, "Card not supported. Please use another card");
        }
        isValidBin();
        return this;
    }

    public ValidationHelper validateSBIAddMoneyRequest() {
        String sbiBlockEnabled = input.getValidationRequest().getPsuLimitInfo().getSbiCardEnabled();
        boolean sbiBlock = ONE.equals(sbiBlockEnabled);
        if (sbiBlock) {
            if (((PaymentType.ADDNPAY.equals(input.getPaymentType()) && TransType.ACQUIRING
                    .equals(input.getTransType())) || TransType.TOP_UP.equals(input.getTransType()))
                    && "SBI".equalsIgnoreCase(input.getValidationRequest().getSelectedBank())) {
                errors.add(ValidationMessage.ERROR_MSG,
                        "Your bank has disabled this option. Kindly use another payment mode.");
                errors.add(ValidationMessage.SBI_ADD_MONEY,
                        "Your bank has disabled this option. Kindly use another payment mode.");
            }
        }
        return this;
    }

    /**
     * Validate if SBI Card is enabled for Transaction
     *
     * @return
     */
    public ValidationHelper validateIfSBICardEnabled() {
        String sbiBlockEnabled = input.getValidationRequest().getPsuLimitInfo().getSbiCardEnabled();
        boolean sbiBlock = ONE.equals(sbiBlockEnabled);
        if (sbiBlock) {
            if (null != input.getBinCard()) {
                String bankName = input.getBinCard().getBankName();
                if (!StringUtils.isBlank(bankName)) {
                    boolean shouldProceed = shouldTxnProceed(bankName);
                    if (!shouldProceed) {
                        errors.add(ValidationMessage.INVALID_CARD_NUMBER, "Transaction Declined");
                        errors.add(ValidationMessage.ERROR_MSG, "Transaction Declined");
                    }
                }
            }
        }

        return this;
    }

    public ValidationHelper validateCardMaxTxnAmountLimit() {
        if (isCardMaxAmountLimitEnabled()) {
            if (null != input.getBinCard()) {
                String bankName = input.getBinCard().getBankName();
                if (StringUtils.isNotBlank(bankName)) {
                    boolean shouldProceed = isCardAllowedForTxnAmount();
                    if (!shouldProceed) {
                        errors.add(ValidationMessage.INVALID_TXN_AMOUNT,
                                "Transaction declined, please use another card for making the payment");
                        errors.add(ValidationMessage.ERROR_MSG,
                                "Transaction declined, please use another card for making the payment.");
                    }
                }
            }
        }
        return this;
    }

    public ValidationHelper validateNBMaxTxnAmountLimit() {
        if (isNBMaxAmountLimitEnabled() && StringUtils.isNotBlank(input.getValidationRequest().getSelectedBank())) {
            boolean shouldProceed = isNBAllowedForTxnAmount();
            if (!shouldProceed) {
                errors.add(ValidationMessage.INVALID_TXN_AMOUNT,
                        "Transaction declined, please use another bank for making the payment");
                errors.add(ValidationMessage.ERROR_MSG,
                        "Transaction declined, please use another bank for making the payment.");
            }
        }
        return this;
    }

    private boolean isNBAllowedForTxnAmount() {
        boolean result = true;

        try {
            if (isMerchantApplicableForCappedAmount(input.getValidationRequest().getEntityId(), input
                    .getValidationRequest().getPsuLimitInfo().getCapMerchantList())
                    && isBankApplicableForCap(input.getValidationRequest().getBankId(), input.getValidationRequest()
                            .getPsuLimitInfo().getNbCapBankList())) {
                double maxAmount = Double.valueOf(input.getValidationRequest().getPsuLimitInfo().getNbCapMaxAmount());
                maxAmount *= 100;
                double txnAmount = Double.valueOf(input.getAmount());
                if ((maxAmount < txnAmount) && (maxAmount > 0)) {
                    result = false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception: Checking if the selected Bank is allowed for the transaction amount (Netbanking) "
                    + e);
        }
        return result;
    }

    private boolean isCardAllowedForTxnAmount() {
        boolean result = true;

        try {
            if (isMerchantApplicableForCappedAmount(input.getValidationRequest().getEntityId(), input
                    .getValidationRequest().getPsuLimitInfo().getCapMerchantList())
                    && isBankApplicableForCap(input.getValidationRequest().getBankId(), input.getValidationRequest()
                            .getPsuLimitInfo().getCardCapBankList())) {
                double txnAmount = Double.valueOf(input.getAmount());
                double maxAmount = Double.valueOf(input.getValidationRequest().getPsuLimitInfo().getCardCapMaxAmount());
                maxAmount *= 100;
                if ((maxAmount < txnAmount) && (maxAmount > 0)) {
                    result = false;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: Checking if the card is allowed for the transaction amount", ex);
        }
        return result;
    }

    private boolean isMerchantApplicableForCappedAmount(String entityId, List<String> capEntities) {
        return capEntities.contains(entityId);
    }

    private boolean isBankApplicableForCap(String bankId, List<String> capBankIds) {
        return capBankIds.contains(bankId);
    }

    private boolean isNBMaxAmountLimitEnabled() {
        String value = input.getValidationRequest().getPsuLimitInfo().getNbCapApplicable();
        return "yes".equalsIgnoreCase(value.trim()) ? true : false;
    }

    private boolean isCardMaxAmountLimitEnabled() {
        String value = input.getValidationRequest().getPsuLimitInfo().getCardCapApplicable();
        return "yes".equalsIgnoreCase(value.trim()) ? true : false;
    }

    private boolean shouldTxnProceed(String bankName) {
        if (!StringUtils.isBlank(bankName)) {
            bankName = bankName.toLowerCase();
            if (bankName.startsWith("state bank") || bankName.equalsIgnoreCase("sbi")) {
                return false;
            }
        }
        return true;
    }

    public void isValidBin() {
        if (input.getBinCard() == null) {
            return;
        }

        if (StringUtils.isBlank(input.getBinCard().getInstId())
                || StringUtils.isBlank(input.getBinCard().getInstNetworkCode())
                || StringUtils.isBlank(input.getBinCard().getBankName())) {
            errors.add(ValidationMessage.ERROR_MSG, "Invalid Card Number");
            errors.add(ValidationMessage.INVALID_BIN, "Invalid Card Number");
        }
    }

    public MultivaluedMap<ValidationMessage, String> getErrors() {
        return errors;
    }

}