package com.paytm.pgplus.theia.offline.controller;

import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.offline.annotations.OfflineControllerAdvice;
import com.paytm.pgplus.theia.offline.model.request.DigitalCreditCheckBalanceRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.response.DigitalCreditCheckBalanceResponse;
import com.paytm.pgplus.theia.offline.services.ICheckBalanceService;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by rahulverma on 17/4/18.
 */

@OfflineControllerAdvice
@RestController
public class CheckBalanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckBalanceController.class);
    @Autowired
    @Qualifier("checkBalanceService")
    private ICheckBalanceService checkBalanceService;

    @RequestMapping(value = "/balance/postpaid", method = { RequestMethod.POST })
    public DigitalCreditCheckBalanceResponse digitalCreditBalance(@RequestBody DigitalCreditCheckBalanceRequest request) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("digitalCreditBalance request = {}", request);
        try {
            if (request == null || request.getHead() == null) {
                throw new TheiaControllerException("DigitalCreditCheckBalanceRequest can't be null");
            }

            OfflinePaymentUtils.setMDC(request.getHead());
            DigitalCreditCheckBalanceResponse response = checkBalanceService.checkDigitalCreditBalance(request);
            LOGGER.info("returning response for digitalCreditBalance = {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken by digitalCreditBalance API : {}ms", System.currentTimeMillis() - startTime);
        }
    }

}
