package com.paytm.pgplus.cashier.constant;

import com.paytm.pgplus.cache.enums.TransactionType;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.cashier.enums.PaymentMode;

/**
 * Specifies Type of Cashier Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 28, 2016
 */
public enum CashierWorkflow {

    ISOCARD("ISOCARD"), IMPS("IMPS"), COD("COD"), ATM("ATM"), NB("NB"), SUBSCRIPTION("SUBSCRIPTION"), WALLET("WALLET"), UPI(
            "UPI"), ADD_MONEY_NB("ADD_MONEY_NB"), ADD_MONEY_ISOCARD("ADD_MONEY_ISOCARD"), ADD_MONEY_IMPS(
            "ADD_MONEY_IMPS"), ADD_MONEY_ATM("ADD_MONEY_ATM"), ADD_MONEY_UPI("ADD_MONEY_UPI"), DIRECT_BANK_CARD_PAYMENT(
            "DIRECT_BANK_CARD_PAYMENT"), RISK_POLICY_CONSULT("RISK_POLICY_CONSULT"), DIGITAL_CREDIT_PAYMENT(
            "DIGITAL_CREDIT_PAYMENT"), GENERIC_PAYMENT_WORKFLOW("GENERIC_PAYMENT_WORKFLOW");

    private String value;

    CashierWorkflow(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static CashierWorkflow getCashierWorkFlowByValue(String paymentMode, TransactionInfo transInfo) {
        boolean isFundOrder = true;

        if ((transInfo == null) || TransactionType.ACQUIRING.equals(transInfo.getTransactionType())) {
            isFundOrder = false;
        }

        if (PaymentMode.CC.getMode().equals(paymentMode) || PaymentMode.DC.getMode().equals(paymentMode)
                || PaymentMode.EMI.getMode().equals(paymentMode) || PaymentMode.EMI_DC.getMode().equals(paymentMode)) {
            return isFundOrder ? CashierWorkflow.ADD_MONEY_ISOCARD : CashierWorkflow.ISOCARD;
        }

        if (PaymentMode.NB.getMode().equals(paymentMode)) {
            return isFundOrder ? CashierWorkflow.ADD_MONEY_NB : CashierWorkflow.NB;
        }

        if (PaymentMode.ATM.getMode().equals(paymentMode)) {
            return isFundOrder ? CashierWorkflow.ADD_MONEY_ATM : CashierWorkflow.ATM;
        }

        if (PaymentMode.IMPS.getMode().equals(paymentMode)) {
            return isFundOrder ? CashierWorkflow.ADD_MONEY_IMPS : CashierWorkflow.IMPS;
        }

        if (PaymentMode.UPI.getMode().equals(paymentMode)) {
            return isFundOrder ? CashierWorkflow.ADD_MONEY_UPI : CashierWorkflow.UPI;
        }

        if (PaymentMode.PPI.getMode().equals(paymentMode)) {
            return CashierWorkflow.WALLET;
        }

        if (PaymentMode.COD.getMode().equals(paymentMode)) {
            return CashierWorkflow.COD;
        }

        if (PaymentMode.RISK.getMode().equals(paymentMode)) {
            return CashierWorkflow.RISK_POLICY_CONSULT;
        }

        for (CashierWorkflow cashierWorkflow : CashierWorkflow.values()) {
            if (cashierWorkflow.value.equals(paymentMode)) {
                return cashierWorkflow;
            }
        }
        return null;
    }

}