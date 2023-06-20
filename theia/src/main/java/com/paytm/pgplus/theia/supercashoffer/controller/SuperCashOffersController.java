package com.paytm.pgplus.theia.supercashoffer.controller;

import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferRequest;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferResponse;
import com.paytm.pgplus.theia.supercashoffer.service.ISuperCashOffersService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@NativeControllerAdvice
@RestController
@RequestMapping("api/v1")
public class SuperCashOffersController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuperCashOffersController.class);

    @Autowired
    private ISuperCashOffersService superCashOffersService;

    @ApiOperation(value = "applySupercashOffers", notes = "to apply/validate superCashOffers")
    @RequestMapping(value = "/applySupercashOffers", method = { RequestMethod.POST })
    public SuperCashOfferResponse searchSuperCashOffers(
            @ApiParam(required = true) @RequestBody SuperCashOfferRequest superCashOfferRequest,
            @RequestParam(value = "referenceId", required = false) String referenceId) {

        final long startTime = System.currentTimeMillis();
        LOGGER.info("applySupercashOffers request = {}", superCashOfferRequest);
        try {
            return superCashOffersService.applySuperCash(superCashOfferRequest, false);
        } finally {
            LOGGER.info("Total time taken by supercash API : {}ms", System.currentTimeMillis() - startTime);
        }
    }

}
