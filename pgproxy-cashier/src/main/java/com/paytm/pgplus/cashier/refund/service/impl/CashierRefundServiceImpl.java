/**
 *
 */
package com.paytm.pgplus.cashier.refund.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.refund.model.RefundRequest;
import com.paytm.pgplus.cashier.refund.model.RefundResponse;
import com.paytm.pgplus.cashier.refund.service.ICashierRefundService;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;

/**
 * @author amit.dubey
 *
 */
@Component("cashierRefundServiceImpl")
public class CashierRefundServiceImpl implements ICashierRefundService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashierRefundServiceImpl.class);
    private static final String REFUND_URL = "/refund";

    @Override
    public RefundResponse processRefund(final RefundRequest refundRequest) throws CashierCheckedException {

        final HttpRequestPayload<RefundRequest> requestPayload = new HttpRequestPayload<>();

        requestPayload.setTarget(REFUND_URL + refundRequest);

        RefundResponse refundResponse = null;
        try {
            refundResponse = JerseyHttpClient.sendHttpGetRequest(requestPayload, RefundResponse.class);
        } catch (HttpCommunicationException | IllegalPayloadException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if ((refundResponse != null) && refundResponse.getRespCode().equalsIgnoreCase("S")) {
            return refundResponse;
        }

        throw new CashierCheckedException("Failed to initiate refund");
    }
}
