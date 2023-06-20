/**
 *
 */
package com.paytm.pgplus.cashier.looper.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.GenericCallBack;

/**
 * @author Amit Dubey
 * @since March 9, 2016
 *
 */
public interface ILooperService {
    CashierPaymentStatus fetchBankForm(String cashierRequestId) throws CashierCheckedException;

    CashierPaymentStatus fetchPaymentStatus(String acquirementId, CashierRequest cashierRequest)
            throws CashierCheckedException;

    void fetchPaymentStatusAsync(String acquirementId, CashierRequest cashierRequest, GenericCallBack callBack)
            throws CashierCheckedException;

    CashierTransactionStatus fetchTrasactionStatusForAcquirementId(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAOAMerchant) throws CashierCheckedException;

    CashierFundOrderStatus fetchFundOrderStatus(String fundOrderId, String paytmMerchantId, Routes route)
            throws CashierCheckedException;

    CashierTransactionStatus fetchTrasactionStatusForAcquirementId(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAoaMerchant, String paytmMerchantId, Routes route)
            throws CashierCheckedException;
}