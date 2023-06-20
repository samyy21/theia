package com.paytm.pgplus.cashier.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

public interface IUPICollectSyncService {

    CashierPaymentStatus fetchPaymentStatus(final String acquirementId, final CashierRequest cashierRequest)
            throws CashierCheckedException;

    CashierFundOrderStatus fetchFundOrderStatus(final String fundOrderId, CashierRequest cashierRequest)
            throws CashierCheckedException;

    CashierTransactionStatus fetchTrasactionStatus(final String merchantId, final String acquirementId,
            final boolean needFullInfo, final boolean isFromAoaMerchant) throws CashierCheckedException;

    CashierTransactionStatus fetchTrasactionStatus(final String merchantId, final String acquirementId,
            final boolean needFullInfo, final boolean isFromAoaMerchant, final String paytmMerchantId,
            final Routes route) throws CashierCheckedException;

}
