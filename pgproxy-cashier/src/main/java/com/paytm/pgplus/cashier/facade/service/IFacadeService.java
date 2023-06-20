/**
 *
 */
package com.paytm.pgplus.cashier.facade.service;

import java.util.List;

import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fund.models.response.QueryByFundOrderIdResponse;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amit.dubey
 *
 */
public interface IFacadeService {
    CacheCardResponseBody getCacheCardTokenId(CompleteCardRequest cardRequest) throws CashierCheckedException,
            PaytmValidationException;

    String getCashierRequestId(PaymentRequest cashierPayRequestBody, List<CashierPayOptionBill> cashierPayOptionBills)
            throws CashierCheckedException, PaytmValidationException;

    boolean validatePaymentOTP(String code, String token, Long otp) throws CashierCheckedException;

    CashierTransactionStatus queryByAcquirementId(String merchantId, String acquirementId, boolean needFullInfo,
            boolean isFromAOAMerchant) throws CashierCheckedException;

    ConsultDetails totalChargeFeeAmount(CashierRequest cashierRequest, long additionalChargeFee)
            throws FacadeCheckedException, CashierCheckedException;

    boolean riskConsultResponse(CashierRequest cashierRequest, String cacheCardToken, String userId)
            throws FacadeCheckedException;

    @Deprecated
    PayResultQueryResponse fetchPayResultQueryResponse(PayResultQueryRequest payResultQueryRequest)
            throws FacadeCheckedException;

    PayResultQueryResponse fetchPayResultQueryResponse(final CashierRequest cashierRequest)
            throws FacadeCheckedException;

    void closeOrder(final String merchantId, final String acquirementId, final boolean fromAoaMerchant)
            throws FacadeCheckedException;

    void closeFundOrder(final String fundOrderId, final EnvInfoRequestBean envInfo) throws FacadeCheckedException;

    QueryByFundOrderIdResponse queryByFundOrderId(final String fundOrderId, String paytmMerchantId, Routes route)
            throws FacadeCheckedException;

    CashierTransactionStatus queryByAcquirementId(String merchantId, String acquirementId, boolean needFullInfo,
            boolean isFromAOAMerchant, String paytmMerchantId, Routes route) throws CashierCheckedException;

}