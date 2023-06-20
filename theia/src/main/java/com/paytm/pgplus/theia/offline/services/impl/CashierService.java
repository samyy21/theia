package com.paytm.pgplus.theia.offline.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.services.ICashierService;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;

/**
 * Created by rahulverma on 23/8/17.
 */
@Service("cashierService")
public class CashierService implements ICashierService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashierService.class);

    @Autowired
    private CashierServiceHelper cashierServiceHelper;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    public CashierInfoResponse fetchCashierInfo(final CashierInfoRequest cashierInfoRequest) {
        LOGGER.debug("fetchCashierInfo {}", cashierInfoRequest);

        cashierServiceHelper.validateRequestBean(cashierInfoRequest);
        PaymentRequestBean paymentRequestBean = cashierServiceHelper
                .cashierInfoRequestToPaymentRequestBean(cashierInfoRequest);
        CashierInfoResponse cashierInfoResponse = cashierServiceHelper.processPaymentRequestAndMapResponse(
                paymentRequestBean, cashierInfoRequest);
        merchantDataUtil.mapMerchantDataForMPINGeneration(paymentRequestBean, cashierInfoResponse);
        cashierServiceHelper.filterUpiPayOptions(cashierInfoResponse.getBody().getPayMethodViews()
                .getMerchantPayMethods());
        cashierServiceHelper.filterUpiPayOptions(cashierInfoResponse.getBody().getPayMethodViews()
                .getAddMoneyPayMethods());
        cashierServiceHelper.removeDigitalCreditIfBalanceInSufficient(cashierInfoResponse, paymentRequestBean);
        cashierServiceHelper.filterDisabledPayMethods(cashierInfoResponse);
        cashierServiceHelper.filterDisabledSavedInstruments(cashierInfoResponse);
        cashierServiceHelper.trimResponse(cashierInfoResponse, cashierInfoRequest);

        LOGGER.debug("cashierInfoResponse {}", cashierInfoResponse);
        return cashierInfoResponse;
    }

}
