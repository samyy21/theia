package com.paytm.pgplus.cashier.payoption;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillGeneratorAddNPay implements IPayOptionBillGenerator {
    public static final Logger LOGGER = LoggerFactory.getLogger(PayOptionBillGeneratorAddNPay.class);

    private static final int MIN_PAY_METHODS_SIZE = 2;
    private static final boolean topupAndPay = false;

    @Override
    public List<CashierPayOptionBill> generatePayOptionBill(PayBillOptions payBillOptions)
            throws CashierCheckedException {
        validate(payBillOptions);

        List<CashierPayOptionBill> cashierPayOptionBills = new ArrayList<>();

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
                String.valueOf(balancePayMethodTransAmount));
        CashierMoney balanceChargeAmount = new CashierMoney(EnumCurrency.INR.getType(),
                String.valueOf(balancePayMethodChargeAmount));

        CashierMoney otherTransAmount = new CashierMoney(EnumCurrency.INR.getType(),
                String.valueOf(otherPayMethodTransAmount));
        CashierMoney otherChargeAmount = new CashierMoney(EnumCurrency.INR.getType(),
                String.valueOf(otherPayMethodChargeAmount));

        Map<String, String> extendedInfo = new HashMap<>();

        for (PayMethod payMethod : payBillOptions.getPayOptions().keySet()) {
            if (payMethod == PayMethod.BALANCE) {
                if ((AmountUtils.getAmount(balanceChargeAmount.getAmount()) + AmountUtils.getAmount(balanceTransAmount
                        .getAmount())) > 0) {
                    CashierPayOptionBill balancePayOptionBill = new CashierPayOptionBill(payBillOptions.getPayOptions()
                            .get(payMethod), payMethod, balanceTransAmount, balanceChargeAmount);
                    balancePayOptionBill.setPayerAccountNo(payBillOptions.getPayerAccountNo());
                    balancePayOptionBill.setIssuingCountry(payBillOptions.getIssuingCountry());
                    balancePayOptionBill.setCardCacheToken(payBillOptions.getCardCacheToken());
                    balancePayOptionBill.setChannelInfo(payBillOptions.getChannelInfo());
                    extendedInfo.putAll(payBillOptions.getExtendInfo());
                    extendedInfo.put("topupAndPay", String.valueOf(topupAndPay));
                    balancePayOptionBill.setExtendInfo(extendedInfo);
                    balancePayOptionBill.setSaveChannelInfoAfterPay(payBillOptions.isSaveChannelInfoAfterPay());
                    balancePayOptionBill.setTopupAndPay(topupAndPay);
                    cashierPayOptionBills.add(balancePayOptionBill);
                }
            } else {
                CashierPayOptionBill otherPayOptionBill = new CashierPayOptionBill(payBillOptions.getPayOptions().get(
                        payMethod), payMethod, otherTransAmount, otherChargeAmount);

                otherPayOptionBill.setPayerAccountNo(payBillOptions.getPayerAccountNo());
                otherPayOptionBill.setCardCacheToken(payBillOptions.getCardCacheToken());
                otherPayOptionBill.setChannelInfo(payBillOptions.getChannelInfo());
                otherPayOptionBill.setIssuingCountry(payBillOptions.getIssuingCountry());

                extendedInfo.putAll(payBillOptions.getExtendInfo());
                extendedInfo.put("topupAndPay", String.valueOf(payBillOptions.isTopupAndPay()));
                if (StringUtils.isNotBlank(payBillOptions.getCardIndexNo())) {
                    extendedInfo.put(CashierConstant.CARD_INDEX_NO, payBillOptions.getCardIndexNo());
                }
                if (StringUtils.isNotBlank(payBillOptions.getMaskedCardNo())) {
                    extendedInfo.put(CashierConstant.MASKED_CARD_NO, payBillOptions.getMaskedCardNo());
                }
                otherPayOptionBill.setExtendInfo(extendedInfo);

                otherPayOptionBill.setSaveChannelInfoAfterPay(payBillOptions.isSaveChannelInfoAfterPay());
                otherPayOptionBill.setTopupAndPay(payBillOptions.isTopupAndPay());

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

        if (!payBillOptions.isTopupAndPay()) {
            LOGGER.debug("Add N Pay parameter should be true");
            throw new CashierCheckedException("Add N Pay parameter should be true");
        }

        if (payBillOptions.getPayOptions().size() != MIN_PAY_METHODS_SIZE) {
            LOGGER.debug("At least provide two pay methods");
            throw new CashierCheckedException("At least provide two pay methods");
        }

        if (!isSubscription(payBillOptions)) {
            if (payBillOptions.getWalletBalance() >= (payBillOptions.getChargeFeeAmount() + payBillOptions
                    .getServiceAmount())) {
                LOGGER.debug("ADD N PAY not allowed as user have sufficient wallet balance");
                throw new CashierCheckedException("ADD N PAY not allowed as user have sufficient wallet balance");
            }
        } else {
            LOGGER.debug("Skipping Wallet balance validation as Subscription Trans");
        }
        if (!payBillOptions.getPayOptions().containsKey(PayMethod.BALANCE)) {
            LOGGER.debug("BALANCE as pay method is mandatory for processing ADD N PAY");
            throw new CashierCheckedException("BALANCE as pay method is mandatory for processing ADD N PAY");
        }

        if (StringUtils.isEmpty(payBillOptions.getPayerAccountNo())) {
            LOGGER.debug("Process failed : payer account number can not be null ");
            throw new CashierCheckedException("Process failed : payer account number can not be null");
        }
    }

    private boolean isSubscription(PayBillOptions payBillOptions) {
        return payBillOptions.getExtendInfo().containsKey("subscriptionId");
    }

}
