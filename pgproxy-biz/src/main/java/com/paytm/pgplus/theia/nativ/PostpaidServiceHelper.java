/**
 * Alipay.com Inc. * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.postpaid.IPaytmDigitalCreditCheckBalanceService;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.SET_POSTPAID_BALANCE_TO_ZERO_FOR_INACTIVE_USER;

/** * @author Kushwanth Reddy **/

@Service
public class PostpaidServiceHelper {

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("paytmDigitalCreditCheckBalanceServiceService")
    private IPaytmDigitalCreditCheckBalanceService digitalCreditCheckBalanceService;

    public PaytmDigitalCreditResponse checkBalance(PaytmDigitalCreditRequest request, String ssoToken)
            throws FacadeCheckedException {

        PaytmDigitalCreditResponse response = digitalCreditCheckBalanceService.checkBalance(request, ssoToken);
        if (ff4jUtils.isFeatureEnabledOnMid(request.getPgmid(), SET_POSTPAID_BALANCE_TO_ZERO_FOR_INACTIVE_USER, false)) {
            updateAmountIfAccountNotActive(response);
        }
        return response;
    }

    private void updateAmountIfAccountNotActive(PaytmDigitalCreditResponse response) {
        if (response != null && CollectionUtils.isNotEmpty(response.getResponse())) {
            for (int i = 0; i < response.getResponse().size(); i++) {
                if (StringUtils.isNotBlank(response.getResponse().get(i).getAccountStatus())
                        && !StringUtils.equals("ACTIVE", response.getResponse().get(i).getAccountStatus()))
                    response.getResponse().get(i).setAmount(0.0);
            }
        }
    }

}