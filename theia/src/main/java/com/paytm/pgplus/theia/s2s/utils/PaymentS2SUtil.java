package com.paytm.pgplus.theia.s2s.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequest;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestHeader;

public class PaymentS2SUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentS2SUtil.class);

    private static ThreadLocal<PaymentS2SRequestHeader> THREAD_LOCAL = new ThreadLocal<>();

    public static void setRequestHeader(PaymentS2SRequestHeader requestHeader) {
        THREAD_LOCAL.set(requestHeader);
    }

    public static PaymentS2SRequestHeader getRequestHeader() {
        return THREAD_LOCAL.get();
    }

    public static void setMDC(PaymentS2SRequest paymentRequest) {
        MDC.clear();
        if (paymentRequest != null && paymentRequest.getBody() != null
                && StringUtils.isNotBlank(paymentRequest.getBody().getMid())) {
            MDC.put(TheiaConstant.RequestParams.MID, paymentRequest.getBody().getMid());
        }
        if (paymentRequest != null && paymentRequest.getBody() != null
                && StringUtils.isNotBlank(paymentRequest.getBody().getOrderId())) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, paymentRequest.getBody().getOrderId());
        }
    }

}
