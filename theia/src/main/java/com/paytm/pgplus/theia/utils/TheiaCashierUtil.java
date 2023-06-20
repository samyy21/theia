/**
 *
 */
package com.paytm.pgplus.theia.utils;

import java.math.BigDecimal;
import java.util.Map;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;

/**
 * @author amit.dubey
 *
 */
@Service
public class TheiaCashierUtil {
    private final Logger LOGGER = LoggerFactory.getLogger(TheiaCashierUtil.class);

    /**
     * @param theiaPaymentRequest
     * @param walletBalance
     * @param txnConfig
     * @param txnInfo
     * @return
     */
    public PaymentInfo computeRequestTypeAndAmount(final TheiaPaymentRequest theiaPaymentRequest,
            final Double walletBalance, final TransactionConfig txnConfig, final TransactionInfo txnInfo)
            throws PaytmValidationException {

        final PaymentInfo paymentInfo = new PaymentInfo();
        final String walletAmountString = theiaPaymentRequest.getWalletAmount();
        final String totalAmountString = txnInfo.isZeroRupeesSubscription() ? ConfigurationUtil.getProperty(
                ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1") : txnInfo.getTxnAmount();
        Long walletAmount = 0L;
        Long totalAmount = 0L;
        Long subsMinAmount = Long.parseLong(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1")) * 100L;

        Long walletBalanceInPaise = getAmountInSmallestDenomination(walletBalance);

        if (StringUtils.isNotBlank(walletAmountString)) {
            try {
                walletAmount = getAmountInSmallestDenomination(Double.parseDouble(walletAmountString));
            } catch (final NumberFormatException ex) {
                LOGGER.error("Wallet Amount parsing error", ex);
            }
        }

        if (StringUtils.isNotBlank(totalAmountString)) {
            try {
                totalAmount = Long.parseLong(AmountUtils.getTransactionAmountInPaise(totalAmountString));
            } catch (final NumberFormatException ex) {
                LOGGER.error("Total Amount parsing error ", ex);
            }
        }

        if (txnInfo.getRequestType().equals(RequestTypes.SUBSCRIPTION)) {
            if (walletBalanceInPaise >= totalAmount) {
                if (StringUtils.isBlank(theiaPaymentRequest.getSavedCardId())
                        && SubsTypes.NORMAL.equals(txnConfig.getSubsTypes())) {
                    walletBalanceInPaise = totalAmount - subsMinAmount;
                    walletAmount = walletBalanceInPaise;
                }
            }
        }

        paymentInfo.setServiceAmount(totalAmount);
        paymentInfo.setWalletBalance(walletBalanceInPaise);
        final String addMoneyFlag = theiaPaymentRequest.getAddMoneyFlag();
        final boolean isAddAndPaySelected = ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(addMoneyFlag);
        if ((null != walletBalanceInPaise) && (walletBalanceInPaise >= walletAmount)
                && ((walletAmount > 0) || isAddAndPaySelected)) {
            if ((walletBalanceInPaise >= totalAmount)
                    && PayMethod.BALANCE.getOldName().equals(theiaPaymentRequest.getPaymentMode())) {

                paymentInfo.setPaymentType(PaymentType.ONLY_WALLET);
            } else {
                if (isAddAndPaySelected && txnConfig.isAddAndPayAllowed()) {

                    if (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())
                            && txnInfo.getRequestType().equals(RequestTypes.SUBSCRIPTION)
                            && isSufficientBalance(paymentInfo)) {

                        paymentInfo.setPaymentType(PaymentType.ONLY_WALLET);
                    } else {
                        paymentInfo.setPaymentType(PaymentType.ADDNPAY);
                        paymentInfo.setTopupAndPay(true);
                    }
                } else if (PayMethod.EMI.getOldName().equals(theiaPaymentRequest.getPaymentMode())
                        && txnConfig.isHybridAllowed()) {
                    if (CashierConstant.BAJAJFN.equals(theiaPaymentRequest.getBankCode())) {
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_BAJAJFN_EMI);
                    }
                    paymentInfo.setPaymentType(PaymentType.HYBRID);
                } else if (PayMethod.UPI.getOldName().equals(theiaPaymentRequest.getPaymentMode())
                        && txnConfig.isHybridAllowed()) {
                    paymentInfo.setPaymentType(PaymentType.HYBRID);
                } else {
                    if (PayMethod.MP_COD.getOldName().equals(theiaPaymentRequest.getPaymentMode())
                            && txnConfig.isCodHybridAllowed()) {
                        paymentInfo.setPaymentType(PaymentType.HYBRID_COD);
                    } else if (txnConfig.isHybridAllowed()) {
                        paymentInfo.setPaymentType(PaymentType.HYBRID);
                    }
                }
            }
        } else if ((walletAmount == 0) && PayMethod.MP_COD.getOldName().equals(theiaPaymentRequest.getPaymentMode())) {
            paymentInfo.setPaymentType(PaymentType.ONLY_COD);
        } else if (walletAmount == 0) {
            paymentInfo.setPaymentType(PaymentType.ONLY_PG);
        }

        // charge fee amount calculation
        paymentInfo.setChargeFeeAmount(getChargeFeeAmount(txnInfo, theiaPaymentRequest, paymentInfo, txnConfig));

        return paymentInfo;
    }

    /**
     * @param amount
     * @return
     */
    private Long getAmountInSmallestDenomination(final Double amount) {
        final Double amountInPaise = amount * 100;
        return amountInPaise.longValue();
    }

    /**
     * @param txnInfo
     * @param theiaPaymentRequest
     * @param txnConfig
     * @param paymentInfo
     * @return
     */
    private long getChargeFeeAmount(final TransactionInfo txnInfo, final TheiaPaymentRequest theiaPaymentRequest,
            final PaymentInfo paymentInfo, final TransactionConfig txnConfig) {

        final Map<String, ConsultDetails> paymentCharges = txnConfig.getPaymentCharges();

        BigDecimal fees = new BigDecimal("0");

        if (paymentCharges != null) {
            switch (paymentInfo.getPaymentType()) {
            case ADDNPAY:
                fees = paymentCharges.get(com.paytm.pgplus.common.enums.PayMethod.BALANCE.getOldName())
                        .getTotalTransactionAmount();
                fees = fees.subtract(paymentCharges.get(com.paytm.pgplus.common.enums.PayMethod.BALANCE.getOldName())
                        .getBaseTransactionAmount());
                break;
            case HYBRID:
            case HYBRID_COD:
                fees = paymentCharges.get(com.paytm.pgplus.common.enums.PayMethod.HYBRID_PAYMENT.getOldName())
                        .getTotalTransactionAmount();
                fees = fees
                        .subtract(paymentCharges.get(
                                com.paytm.pgplus.common.enums.PayMethod.HYBRID_PAYMENT.getOldName())
                                .getBaseTransactionAmount());
                break;
            case ONLY_COD:
                fees = paymentCharges.get(com.paytm.pgplus.common.enums.PayMethod.MP_COD.getOldName())
                        .getTotalTransactionAmount();
                fees = fees.subtract(paymentCharges.get(com.paytm.pgplus.common.enums.PayMethod.MP_COD.getOldName())
                        .getBaseTransactionAmount());
                break;
            case ONLY_PG:
            case ONLY_WALLET:
                final com.paytm.pgplus.common.enums.PayMethod payMethod = com.paytm.pgplus.common.enums.PayMethod
                        .getPayMethodByOldName(theiaPaymentRequest.getTxnMode());
                if (null == payMethod || null == payMethod.getOldName()
                        || null == paymentCharges.get(payMethod.getOldName())) {
                    LOGGER.error("Manadatory param is missing, PayMethod is : {}", payMethod);
                    break;
                }

                fees = paymentCharges.get(payMethod.getOldName()).getTotalTransactionAmount();
                fees = fees.subtract(paymentCharges.get(payMethod.getOldName()).getBaseTransactionAmount());
                break;
            case OTHER:
            default:
                break;
            }
        }
        fees = fees.multiply(new BigDecimal("100"));
        return fees.longValue();
    }

    public boolean isSufficientBalance(PaymentInfo paymentInfo) {
        return paymentInfo.getServiceAmount() <= paymentInfo.getWalletBalance();
    }
}