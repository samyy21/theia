/**
 *
 */
package com.paytm.pgplus.cashier.pay.service.impl;

import java.util.List;
import java.util.Map;

import com.paytm.pgplus.cashier.redis.IPgProxyCashierTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.service.ICashierPayService;
import com.paytm.pgplus.cashier.payoption.IPayOptionBillGenerator;
import com.paytm.pgplus.cashier.payoption.PayOptionBillGeneratorFactory;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author amit.dubey
 *
 */
@Service("cashierPayServiceImpl")
public class CashierPayServiceImpl implements ICashierPayService {
    static final Logger LOGGER = LoggerFactory.getLogger(CashierPayServiceImpl.class);

    @Autowired
    IFacadeService facadeService;

    @Autowired
    private IPgProxyCashierTransactionalRedisUtil pgProxyCashierTransactionalRedisUtil;

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAYMENT_SUBMIT)
    public String submitPay(CashierRequest cashierRequest) throws CashierCheckedException, PaytmValidationException {
        validateSubmitPayRequest(cashierRequest);

        PaymentRequest paymentRequest = cashierRequest.getPaymentRequest();
        IPayOptionBillGenerator payOptionBillGenerator = generatePayOptionBill(cashierRequest);
        List<CashierPayOptionBill> cashierPayOptionBills = payOptionBillGenerator.generatePayOptionBill(paymentRequest
                .getPayBillOptions());
        populateExtendInfo(paymentRequest);

        String cashierRequestId = facadeService.getCashierRequestId(paymentRequest, cashierPayOptionBills);

        if (StringUtils.isEmpty(cashierRequestId)) {
            throw new CashierCheckedException("Process failed : Cashier request id received empty or null");
        }

        if (cashierRequest.isDirectBankCardPayRequest()) {
            SeamlessBankCardPayRequest seamlessBankCardPayRequest = new SeamlessBankCardPayRequest(
                    cashierRequest.getAcquirementId(), cashierRequestId, paymentRequest, cashierPayOptionBills);
            LOGGER.info("setting in seamlessBankCardPayRequest redis {}", seamlessBankCardPayRequest.getRedisKey());
            pgProxyCashierTransactionalRedisUtil.set(seamlessBankCardPayRequest.getRedisKey(),
                    seamlessBankCardPayRequest, 300);
        }

        return cashierRequestId;
    }

    /**
     * @param cashierRequest
     * @return
     */
    private IPayOptionBillGenerator generatePayOptionBill(CashierRequest cashierRequest) {
        PayOptionBillGeneratorFactory factory = new PayOptionBillGeneratorFactory();
        return factory.getPayOptionBillGenerator(cashierRequest.getPaymentRequest().getPaymentType());
    }

    /**
     * @param cashierRequest
     * @throws CashierCheckedException
     */
    private void validateSubmitPayRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierInvalidParameterException("Process failed : cashier request can not be null");
        }

        if (TransType.TOP_UP == cashierRequest.getPaymentRequest().getTransType()) {
            if (StringUtils.isEmpty(cashierRequest.getPaymentRequest().getPayerUserId())) {
                throw new CashierCheckedException("process failed : payer user id can not be null");
            }
        }
    }

    private void populateExtendInfo(PaymentRequest paymentRequest) {
        Map<String, String> extendInfo = paymentRequest.getExtendInfo();
        if (null != extendInfo && !extendInfo.isEmpty()) {
            if (StringUtils.isNotBlank(paymentRequest.getPayBillOptions().getCardIndexNo())) {
                extendInfo.put(CashierConstant.CARD_INDEX_NO, paymentRequest.getPayBillOptions().getCardIndexNo());
            }
            if (StringUtils.isNotBlank(paymentRequest.getPayBillOptions().getMaskedCardNo())) {
                extendInfo.put(CashierConstant.MASKED_CARD_NO, paymentRequest.getPayBillOptions().getMaskedCardNo());
            }
        }
    }
}