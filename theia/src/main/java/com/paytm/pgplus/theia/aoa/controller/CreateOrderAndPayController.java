package com.paytm.pgplus.theia.aoa.controller;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.helper.CreateOrderAndPayHelper;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;

@NativeControllerAdvice
@RestController
@RequestMapping("api/v1")
public class CreateOrderAndPayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderAndPayController.class);

    @Autowired
    private CreateOrderAndPayHelper createOrderAndPayHelper;

    @RequestMapping(value = "/processOrderAndPay", method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public NativeJsonResponse internalProcessTransaction(HttpServletRequest request) {
        final long startTime = System.currentTimeMillis();
        setMDCDefault(request);
        LOGGER.info("Request received for API: /v1/processOrderAndPay is: {}", request);

        NativeJsonResponse response = createOrderAndPayHelper.processOrderAndPay(request);

        LOGGER.info("Total time taken for CreateOrderAndPayController is {} ms", System.currentTimeMillis() - startTime);
        return response;
    }

    private void setMDCDefault(HttpServletRequest request) {
        String mid = request.getParameter(MID);
        /** Hack to Support all merchant */
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID1);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID2);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(MERCHANT_ID);
        }

        String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
        if (StringUtils.isBlank(orderId)) {
            orderId = request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);
        }
        setMDCDefault(mid, orderId);
    }

    private void setMDCDefault(final String mid, final String orderId) {
        MDC.clear();
        if (!StringUtils.isBlank(mid)) {
            MDC.put(MID, mid);
        }

        if (!StringUtils.isBlank(orderId)) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, orderId);
        }
    }
}