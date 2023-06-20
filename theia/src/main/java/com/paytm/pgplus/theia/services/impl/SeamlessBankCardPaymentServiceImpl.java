/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.service.IPaymentService;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ISeamlessBankCardService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author amitdubey
 * @date Dec 6, 2016
 */
@Service("directBankCardPaymentServiceImpl")
public class SeamlessBankCardPaymentServiceImpl implements ISeamlessBankCardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessBankCardPaymentServiceImpl.class);

    @Autowired
    IPaymentService paymentServiceImpl;

    @Autowired
    ICashierCacheService cashierCacheServiceImpl;

    @Autowired
    TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public String doSeamlessBankCardPayment(final String cashierRequestId, final String transactionId)
            throws PaytmValidationException {
        LOGGER.info("Request received for the direct bank card payment for transaction id : {}, cashierRequestId : {}",
                transactionId, cashierRequestId);

        SeamlessBankCardPayRequest seamlessBankCardPayRequest = new SeamlessBankCardPayRequest(cashierRequestId);
        seamlessBankCardPayRequest = (SeamlessBankCardPayRequest) theiaTransactionalRedisUtil
                .get(seamlessBankCardPayRequest.getRedisKey());

        if (seamlessBankCardPayRequest == null) {
            throw new TheiaServiceException("Unable to process the direct bank payment as no data dound in cache");
        }

        if (seamlessBankCardPayRequest.getPaymentRequest() != null
                && seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo() != null) {
            if (StringUtils.isNotBlank(seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                    .get(TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID))) {
                MDC.put(TheiaConstant.RequestParams.MID, seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                        .get(TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID));
            }
            if (StringUtils.isNotBlank(seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                    .get(TheiaConstant.ExtendedInfoPay.MERCHANT_TRANS_ID))) {
                MDC.put(TheiaConstant.RequestParams.ORDER_ID, seamlessBankCardPayRequest.getPaymentRequest()
                        .getExtendInfo().get(TheiaConstant.ExtendedInfoPay.MERCHANT_TRANS_ID));
            }
        }

        PaymentRequest paymentRequest = seamlessBankCardPayRequest.getPaymentRequest();
        paymentRequest.getPayBillOptions().getChannelInfo()
                .put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
        paymentRequest.setRequestId(RequestIdGenerator.generateRequestId());
        LOGGER.debug("Seamless bank card request generated : {}", seamlessBankCardPayRequest);

        GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse = paymentServiceImpl
                .seamlessBankCardPayment(seamlessBankCardPayRequest);
        LOGGER.debug("Response from after initiate payment is : {} ", initiatePaymentResponse);
        return theiaResponseGenerator.getBankPage(initiatePaymentResponse);
    }

    @Override
    public SeamlessBankCardPayRequest getSeamlessBankCardPayRequest(final String cashierRequestId) {
        LOGGER.info("Request received to fetch SeamlessBankCardPayRequest for cashierRequestId : {}", cashierRequestId);

        SeamlessBankCardPayRequest seamlessBankCardPayRequest = new SeamlessBankCardPayRequest(cashierRequestId);
        seamlessBankCardPayRequest = (SeamlessBankCardPayRequest) theiaTransactionalRedisUtil
                .get(seamlessBankCardPayRequest.getRedisKey());

        if (seamlessBankCardPayRequest == null) {
            throw new TheiaServiceException("Unable to process the direct bank payment as no data dound in cache");
        }

        return seamlessBankCardPayRequest;
    }
}