package com.paytm.pgplus.biz.core.looper.service.impl;

import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.looper.service.ILooperService;
import com.paytm.pgplus.biz.exception.LooperServiceCheckedException;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequest;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponse;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.looperclient.exception.LooperException;
import com.paytm.pgplus.looperclient.servicehandler.ILooperRequestHandler;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author manojpal
 *
 */
@Service("looperservice")
public class LooperServiceImpl implements ILooperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LooperServiceImpl.class);

    @Autowired
    @Qualifier("looperRequestHandler")
    private ILooperRequestHandler looperRequestHandler;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_3D_BANK_FORM)
    @Override
    public PayResultQueryResponse fetch3DBankForm(PayResultQueryRequest requestData, String modifiedLooperTimeout,
            BankFormOptimizationParams bankFormOptimizationParams) throws LooperServiceCheckedException {
        try {
            String cashierRequestId = requestData.getBody().getCashierRequestId();
            LOGGER.info("Request received : fetch the bank form : cashier request id : {} ", cashierRequestId);
            Map<String, String> metaData = new LinkedHashMap<>();
            metaData.put("cashierRequestId", cashierRequestId);
            metaData.put("eventMsg", "Request received : fetch the bank form : cashier request");
            EventUtils.pushTheiaEvents(EventNameEnum.REQUEST_BANK_FORM, metaData);
            PayResultQueryResponse response = looperRequestHandler.fetch3DBankForm(requestData, null,
                    modifiedLooperTimeout, bankFormOptimizationParams);
            if (null != response && null != response.getBody() && null != response.getBody().getResultInfo()
                    && "SUCCESS".equalsIgnoreCase(response.getBody().getResultInfo().getResultCode())
                    && StringUtils.isNotBlank(response.getBody().getWebFormContext())) {
                LOGGER.info("Response received : fetch the bank form : cashier request id : {} ", cashierRequestId);
                metaData.put("cashierRequestId", cashierRequestId);
                metaData.put("eventMsg", "Response received : fetch the bank form : cashier request");
                EventUtils.pushTheiaEvents(EventNameEnum.BANK_RESPONSE_RECEIVED, metaData);
            }
            return response;
        } catch (LooperException e) {
            LOGGER.error("Exception occurred while calling looper service to fetch 3D bank form", e);
            throw new LooperServiceCheckedException(
                    "Exception occurred while calling looper service to fetch 3D bank form");
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_PAYMENT_STATUS)
    @Override
    public PayResultQueryResponse fetchPaymentStatus(PayResultQueryRequest requestData)
            throws LooperServiceCheckedException {
        try {
            PayResultQueryResponse response = looperRequestHandler.fetchPaymentStatus(requestData, null);
            return response;
        } catch (LooperException e) {
            LOGGER.error("Exception occurred while calling looper service to fetch payment status", e);
            throw new LooperServiceCheckedException(
                    "Exception occurred while calling looper service to fetch payment status");
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_TRANSACTION_STATUS)
    @Override
    public QueryByAcquirementIdResponse fetchTransactionStatus(QueryByAcquirementIdRequest requestData)
            throws LooperServiceCheckedException {
        try {
            QueryByAcquirementIdResponse response = looperRequestHandler.fetchTransactionStatus(requestData, null);
            return response;
        } catch (LooperException e) {
            LOGGER.error("Exception occurred while calling looper service to fetch transaction status", e);
            throw new LooperServiceCheckedException(
                    "Exception occurred while calling looper service to fetch transaction status");
        }
    }

}
