package com.paytm.pgplus.theia.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * @createdOn 20-Aug-2020
 * @author vinod gaur
 */

@Controller
public class StaticFileRoutesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticFileRoutesController.class);

    @RequestMapping(value = "/sw.js", method = { RequestMethod.GET })
    public void processSw(HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            String serviceWorkerJs = com.paytm.pgplus.common.config.ConfigurationUtil.getServiceWorkerJs();
            response.setContentType("application/javascript");
            response.getOutputStream().write(serviceWorkerJs.getBytes(StandardCharsets.UTF_8));
            return;
        } catch (final Exception e) {
            LOGGER.error("Error in serving sw.js file : ", e);
            throw e;
        }
    }
}