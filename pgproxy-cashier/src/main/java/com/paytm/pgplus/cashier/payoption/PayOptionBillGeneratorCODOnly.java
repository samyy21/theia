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
public class PayOptionBillGeneratorCODOnly implements IPayOptionBillGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayOptionBillGeneratorCODOnly.class);

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
            cashierPayOptionBill.setIssuingCountry(payBillOptions.getIssuingCountry());
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
            LOGGER.warn("Process failed : payBillOptions can not be null");
            throw new CashierCheckedException("Process failed : payBillOptions can not be null");
        }

        if (payBillOptions.getPayOptions().size() != MAX_PAY_METHOD_SIZE) {
            LOGGER.warn("Process failed : unexpected pay methods size");
            throw new CashierCheckedException("Process failed : unexpected pay methods size");
        }

        if (!payBillOptions.getPayOptions().containsKey(PayMethod.MP_COD)) {
            LOGGER.warn("Process failed : pay method must MP_COD");
            throw new CashierCheckedException("Process failed : pay method must MP_COD");
        }

    }

}
