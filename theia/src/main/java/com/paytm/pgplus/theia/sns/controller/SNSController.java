package com.paytm.pgplus.theia.sns.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.theia.sns.model.LocalizationRequestBody;
import com.paytm.pgplus.theia.sns.service.SNSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;

@RestController
public class SNSController {
    @Autowired
    @Qualifier("snsService")
    private SNSService snsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SNSController.class);

    // TODO : Remove after testing in prod
    @RequestMapping(value = "/sns/publish", method = { RequestMethod.POST })
    public void publish() throws JsonProcessingException {

        String apiName = "/theia/api/v1/fetchPaymentOptions";
        HashSet<String> unTranslatedText = new HashSet<>();
        unTranslatedText.add("The Punjab State Cooperative Bank");
        unTranslatedText.add(" The Punjab State Cooperative Bank Debit Card");
        unTranslatedText.add("PSCB");

        LocalizationRequestBody localizationRequestBody = new LocalizationRequestBody(apiName, unTranslatedText);
        LOGGER.info("SNS publish input params :: message:{} ", localizationRequestBody);
        String localizationRequest = new ObjectMapper().writeValueAsString(localizationRequestBody);
        snsService.publish(localizationRequest);
        LOGGER.info("Localization SNS request sent for translation : {}", localizationRequest);

    }

}
