/**
 *
 */
package com.paytm.pgplus.cashier.payoption;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierMoney;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.common.enums.EnumCurrency;
import com.paytm.pgplus.facade.enums.PayMethod;

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillGeneratorHybrid implements IPayOptionBillGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayOptionBillGeneratorHybrid.class);

    private static final int MIN_PAY_METHODS_SIZE = 2;
    private static final boolean topupAndPay = false;

    @Override
    public List<CashierPayOptionBill> generatePayOptionBill(PayBillOptions payBillOptions)
            throws CashierCheckedException {

        validate(payBillOptions);

        List<CashierPayOptionBill> cashierPayOptionBills = new LinkedList<>();

        // pay method : balance
        Long balancePayMethodTransAmount;
        Long balancePayMethodChargeAmount;

        // pay method : other
        Long otherPayMethodTransAmount;
        Long otherPayMethodChargeAmount;

        if (payBillOptions.getWalletBalance() <= payBillOptions.getServiceAmount()) {
            // pay method : balance
            balancePayMethodTransAmount = payBillOptions.getWalletBalance();
            balancePayMethodChargeAmount = 0L;

            // pay method : other
            otherPayMethodTransAmount = payBillOptions.getServiceAmount() - payBillOptions.getWalletBalance();
            otherPayMethodChargeAmount = payBillOptions.getChargeFeeAmount();
        } else {
            // pay method : balance
            balancePayMethodTransAmount = payBillOptions.getServiceAmount();
            balancePayMethodChargeAmount = payBillOptions.getWalletBalance() - payBillOptions.getServiceAmount();

            // pay method : other
            otherPayMethodTransAmount = 0L;
            otherPayMethodChargeAmount = payBillOptions.getChargeFeeAmount() - balancePayMethodChargeAmount;
        }

        CashierMoney balanceTransAmount = new CashierMoney(EnumCurrency.INR.getType(),
                balancePayMethodTransAmount.toString());
        CashierMoney balanceChargeAmount = new CashierMoney(EnumCurrency.INR.getType(),
                balancePayMethodChargeAmount.toString());

        CashierMoney otherTransAmount = new CashierMoney(EnumCurrency.INR.getType(),
                otherPayMethodTransAmount.toString());
        CashierMoney otherChargeAmount = new CashierMoney(EnumCurrency.INR.getType(),
                otherPayMethodChargeAmount.toString());

        Map<String, String> extendedInfo = new HashMap<>();

        for (PayMethod payMethod : payBillOptions.getPayOptions().keySet()) {
            if (payMethod == PayMethod.BALANCE) {
                CashierPayOptionBill balancePayOptionBill = new CashierPayOptionBill(payBillOptions.getPayOptions()
                        .get(payMethod), payMethod, balanceTransAmount, balanceChargeAmount);

                balancePayOptionBill.setPayerAccountNo(payBillOptions.getPayerAccountNo());
                balancePayOptionBill.setCardCacheToken(payBillOptions.getCardCacheToken());
                balancePayOptionBill.setChannelInfo(payBillOptions.getChannelInfo());
                balancePayOptionBill.setIssuingCountry(payBillOptions.getIssuingCountry());

                extendedInfo.putAll(payBillOptions.getExtendInfo());
                extendedInfo.put("topupAndPay", String.valueOf(topupAndPay));
                if (StringUtils.isNotBlank(payBillOptions.getCardIndexNo())) {
                    extendedInfo.put(CashierConstant.CARD_INDEX_NO, payBillOptions.getCardIndexNo());
                }
                if (StringUtils.isNotBlank(payBillOptions.getMaskedCardNo())) {
                    extendedInfo.put(CashierConstant.MASKED_CARD_NO, payBillOptions.getMaskedCardNo());
                }
                balancePayOptionBill.setExtendInfo(extendedInfo);

                balancePayOptionBill.setSaveChannelInfoAfterPay(payBillOptions.isSaveChannelInfoAfterPay());
                balancePayOptionBill.setTopupAndPay(topupAndPay);

                cashierPayOptionBills.add(balancePayOptionBill);
            } else {
                CashierPayOptionBill otherPayOptionBill = new CashierPayOptionBill(payBillOptions.getPayOptions().get(
                        payMethod), payMethod, otherTransAmount, otherChargeAmount);

                otherPayOptionBill.setPayerAccountNo(payBillOptions.getPayerAccountNo());
                otherPayOptionBill.setCardCacheToken(payBillOptions.getCardCacheToken());
                otherPayOptionBill.setChannelInfo(payBillOptions.getChannelInfo());
                otherPayOptionBill.setIssuingCountry(payBillOptions.getIssuingCountry());
                if (StringUtils.isNotBlank(payBillOptions.getCardIndexNo())) {
                    extendedInfo.put(CashierConstant.CARD_INDEX_NO, payBillOptions.getCardIndexNo());
                }
                if (StringUtils.isNotBlank(payBillOptions.getMaskedCardNo())) {
                    extendedInfo.put(CashierConstant.MASKED_CARD_NO, payBillOptions.getMaskedCardNo());
                }
                extendedInfo.putAll(payBillOptions.getExtendInfo());
                extendedInfo.put("topupAndPay", String.valueOf(topupAndPay));
                otherPayOptionBill.setExtendInfo(extendedInfo);

                otherPayOptionBill.setSaveChannelInfoAfterPay(payBillOptions.isSaveChannelInfoAfterPay());
                otherPayOptionBill.setTopupAndPay(topupAndPay);

                cashierPayOptionBills.add(otherPayOptionBill);
            }
        }

        return cashierPayOptionBills;
    }

    private void validate(PayBillOptions payBillOptions) throws CashierCheckedException {
        if (null == payBillOptions) {
            LOGGER.warn("Process failed : payBillOptions can not be null");
            throw new CashierCheckedException("Process failed : payBillOptions can not be null");
        }

        if (null == payBillOptions.getWalletBalance()) {
            LOGGER.error("Process failed : wallet balance can not be null");
            throw new CashierCheckedException("Process failed : wallet balance can not be null");
        }

        if (payBillOptions.getWalletBalance() < CashierConstant.MIN_WALLET_BALANCE) {
            LOGGER.error("Process failed : Wallet balance can not be Negative or ZERO for hybrid payment");
            throw new CashierCheckedException(
                    "Process failed : Wallet balance can not be Negatie or ZERO for hybrid payment");
        }

        if (payBillOptions.getPayOptions().size() != MIN_PAY_METHODS_SIZE) {
            LOGGER.error("Process failed : For processing hybrid payment , at least provide two pay methods");
            throw new CashierCheckedException(
                    "Process failed : For processing hybrid payment , at least provide two pay methods");
        }

        if (payBillOptions.getWalletBalance() >= (payBillOptions.getChargeFeeAmount() + payBillOptions
                .getServiceAmount())) {
            LOGGER.error("Process failed :Hybrid payment not accepted as wallet balance as greater or equal to total amount");
            throw new CashierCheckedException(
                    " Process failed :Hybrid payment not accepted as wallet balance as greater or equal to total amount");
        }

        if (!payBillOptions.getPayOptions().keySet().contains(PayMethod.BALANCE)) {
            LOGGER.error("one pay method should be Balance for hybrid payment");
            throw new CashierCheckedException("one pay method should be Balance for hybrid payment");
        }

        if (StringUtils.isEmpty(payBillOptions.getPayerAccountNo())) {
            LOGGER.debug("Process failed : payer account number can not be null ");
            throw new CashierCheckedException("Process failed : payer account number can not be null");
        }

    }
}