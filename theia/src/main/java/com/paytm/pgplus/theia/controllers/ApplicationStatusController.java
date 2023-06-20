package com.paytm.pgplus.theia.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Karan Kapoor
 *
 */

@RestController
public class ApplicationStatusController {

    @ResponseBody
    @RequestMapping(value = "/applicationstatus", method = { RequestMethod.GET })
    public ResponseEntity checkApplicationStatus() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
