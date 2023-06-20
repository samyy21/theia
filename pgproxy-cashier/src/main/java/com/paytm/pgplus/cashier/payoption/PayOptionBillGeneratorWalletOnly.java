/**
 *
 */
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

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillGeneratorWalletOnly implements IPayOptionBillGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayOptionBillGeneratorWalletOnly.class);

    private static final int MAX_PAY_METHOD_SIZE = 1;
    private static final boolean topupAndPay = false;

    @Override
    public List<CashierPayOptionBill> generatePayOptionBill(PayBillOptions payBillOptions)
            throws CashierCheckedException {

        validate(payBillOptions);

        List<CashierPayOptionBill> cashierPayOptionBills = new ArrayList<>();

        CashierMoney transAmount = new CashierMoney(EnumCurrency.INR.getType(), payBillOptions.getServiceAmount()
                .toString());
        CashierMoney chargeAmount = new CashierMoney(EnumCurrency.INR.getType(), payBillOptions.getChargeFeeAmount()
                .toString());
        Map<String, String> extendedInfo = new HashMap<>();

        for (PayMethod payMethod : payBillOptions.getPayOptions().keySet()) {
            CashierPayOptionBill cashierPayOptionBill = new CashierPayOptionBill(payBillOptions.getPayOptions().get(
                    payMethod), payMethod, transAmount, chargeAmount);

            cashierPayOptionBill.setPayerAccountNo(payBillOptions.getPayerAccountNo());
            cashierPayOptionBill.setCardCacheToken(payBillOptions.getCardCacheToken());
            cashierPayOptionBill.setChannelInfo(payBillOptions.getChannelInfo());
            if (StringUtils.isNotBlank(payBillOptions.getCardIndexNo())) {
                extendedInfo.put(CashierConstant.CARD_INDEX_NO, payBillOptions.getCardIndexNo());
            }
            if (StringUtils.isNotBlank(payBillOptions.getMaskedCardNo())) {
                extendedInfo.put(CashierConstant.MASKED_CARD_NO, payBillOptions.getMaskedCardNo());
            }
            extendedInfo.putAll(payBillOptions.getExtendInfo());
            extendedInfo.put("topupAndPay", String.valueOf(topupAndPay));

            cashierPayOptionBill.setExtendInfo(extendedInfo);
            cashierPayOptionBill.setSaveChannelInfoAfterPay(payBillOptions.isSaveChannelInfoAfterPay());
            cashierPayOptionBill.setTopupAndPay(topupAndPay);
            cashierPayOptionBills.add(cashierPayOptionBill);

            break;
        }

        return cashierPayOptionBills;
    }

    private void validate(PayBillOptions payBillOptions) throws CashierCheckedException {
        if (null == payBillOptions) {
            LOGGER.error("Process failed : pay bill options can not be null");
            throw new CashierCheckedException("Process failed : pay bill options can not be null");
        }

        if (null == payBillOptions.getWalletBalance()) {
            LOGGER.error("Process failed : wallet balance can not be null");
            throw new CashierCheckedException("Process failed : wallet balance can not be null");
        }

        if (payBillOptions.getWalletBalance() < CashierConstant.MIN_WALLET_BALANCE) {
            LOGGER.error("Process failed : Wallet balance can not be Negative or ZERO for wallet only payment");
            throw new CashierCheckedException(
                    "Process failed : Wallet balance can not be Negatie or ZERO for wallet only payment");
        }

        if (payBillOptions.getWalletBalance() < (payBillOptions.getChargeFeeAmount() + payBillOptions
                .getServiceAmount())) {
            LOGGER.error("Proces failed : wallet amount is less than total amount");
            throw new CashierCheckedException("Process failed : wallet amount is less than total amount ");
        }

        if (StringUtils.isEmpty(payBillOptions.getPayerAccountNo())) {
            LOGGER.debug("Process failed : payer account number can not be null ");
            throw new CashierCheckedException("Process failed : payer account number can not be null");
        }

        if (!payBillOptions.getPayOptions().containsKey(PayMethod.BALANCE)) {
            LOGGER.error("process failed : only pay method as BALANCE allowed");
            throw new CashierCheckedException("Process failed : only pay method as BALANCE allowed");
        }

        if (payBillOptions.getPayOptions().size() != MAX_PAY_METHOD_SIZE) {
            LOGGER.error("Process failed : pay options size should be one");
            throw new CashierCheckedException("Process failed : pay options size should be one");
        }
    }

}
