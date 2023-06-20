package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.interceptors.TheiaInterceptor;
import com.paytm.pgplus.theia.models.PRNValidationRequest;
import com.paytm.pgplus.theia.models.PRNValidationResponse;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.impl.PRNValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RestController
public class PRNValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PRNValidationController.class);

    @Autowired
    @Qualifier("prnValidationHelper")
    private PRNValidationHelper prnValidationHelper;

    @RequestMapping(value = "/validatePRN", method = RequestMethod.POST)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PRNValidationResponse validatePRN(HttpServletRequest request,
            @RequestBody PRNValidationRequest prnValidationRequest) {

        PRNValidationResponse prnValidationResponse = prnValidationHelper.validatePRNWithPlatformPlus(request,
                prnValidationRequest);

        return prnValidationResponse;
    }

}
