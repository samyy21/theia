package com.paytm.pgplus.biz.core.looper.service;

import com.paytm.pgplus.biz.exception.LooperServiceCheckedException;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequest;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponse;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;

/**
 * @author manojpal
 *
 */
public interface ILooperService {

    PayResultQueryResponse fetch3DBankForm(final PayResultQueryRequest requestData, String modifiedLooperTimeout,
            BankFormOptimizationParams bankFormOptimizationParams) throws LooperServiceCheckedException;

    PayResultQueryResponse fetchPaymentStatus(final PayResultQueryRequest requestData)
            throws LooperServiceCheckedException;

    QueryByAcquirementIdResponse fetchTransactionStatus(final QueryByAcquirementIdRequest requestData)
            throws LooperServiceCheckedException;
}
