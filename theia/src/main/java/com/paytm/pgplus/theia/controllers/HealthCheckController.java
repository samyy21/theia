package com.paytm.pgplus.theia.controllers;

import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.paytm.pgplus.common.util.NewRelicIgnoreTransaction;

@Controller
public class HealthCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);
    private static final String FILE_LOCATION = new StringBuffer(System.getProperty("catalina.base", null)).append(
            "/conf/healthcheck.txt").toString();

    @NewRelicIgnoreTransaction
    @RequestMapping(value = "/healthcheck", method = { RequestMethod.GET })
    public ResponseEntity<Object> checkHealth(final HttpServletRequest request, final HttpServletResponse response,
            final Model model, final Locale locale) {
        if (isFileExist()) {
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
    }

    public boolean isFileExist() {
        try {
            File file = new File(FILE_LOCATION);
            if (file.isFile()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured, FilePath empty: ", ex);
            return false;
        }

    }
}
