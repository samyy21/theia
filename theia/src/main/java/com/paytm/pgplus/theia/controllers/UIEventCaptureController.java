package com.paytm.pgplus.theia.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paytm.pgplus.common.model.TxnStateLog;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.IUITrackService;

@RestController
@Component
public class UIEventCaptureController {

    @Autowired
    @Qualifier(value = "uiTrackService")
    private IUITrackService uiTrackService;

    @RequestMapping(value = "/trackUIEvents", method = RequestMethod.POST)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String logUIEvents(@RequestBody String requestPayload, final HttpServletRequest request) {
        ThreadLocalUtil.set(new TxnStateLog(StringUtils.EMPTY, request.getParameter(TheiaConstant.RequestParams.MID),
                request.getParameter(TheiaConstant.RequestParams.ORDER_ID), StringUtils.EMPTY));
        uiTrackService.logUIData(requestPayload);
        return "{\"RESULT\":\"SUCCESS\"}";
    }

    @RequestMapping(value = "/trackUIException", method = RequestMethod.POST)
    @Produces(MediaType.APPLICATION_JSON)
    public String logUIException(@RequestBody String requestPayload, final HttpServletRequest request) {
        ThreadLocalUtil.set(new TxnStateLog(StringUtils.EMPTY, request.getParameter(TheiaConstant.RequestParams.MID),
                request.getParameter(TheiaConstant.RequestParams.ORDER_ID), StringUtils.EMPTY));
        uiTrackService.logUIException(requestPayload);
        return "{\"RESULT\":\"SUCCESS\"}";
    }

}
