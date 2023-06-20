package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.controllers.ProcessTransactionController;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.SDK_PTC_FLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SDK_PROCESS_TRANSACTION;

//todo : To be updated once ptc code is moved to service

@NativeControllerAdvice
@Controller
public class ProcessTransactionSdkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionSdkController.class);

    @Autowired
    private ProcessTransactionController ptc;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @RequestMapping(value = "/sdk/processTransaction", method = { RequestMethod.POST })
    public void createTransaction(HttpServletRequest request, final HttpServletResponse response, final Model model,
            final Locale locale) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /sdk/processTransaction");
            Map<String, String> metadata = ptc.createEventLogMetadata(request);
            nativePaymentUtil.logNativeRequests(request.toString(), SDK_PROCESS_TRANSACTION, metadata);
            // This is to distuingish b/w ptc and sdk/ptc
            request.setAttribute(SDK_PTC_FLOW, true);
            ptc.processPaymentPage(request, response, model, locale);
            LOGGER.info("Native response returned for API: /sdk/processTransaction");
        } finally {
            LOGGER.info("Total time taken for Sdk ProcessTransaction is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

}
