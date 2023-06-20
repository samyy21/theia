package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.theia.enums.PayMethod;
import com.paytm.pgplus.payloadvault.theia.request.PayOptionsRequest;
import com.paytm.pgplus.payloadvault.theia.response.*;
import com.paytm.pgplus.theia.services.IMerchantPayOptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitgupta on 10/8/17.
 */
@Controller
public class MerchantPayOptionsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantPayOptionsController.class);

    private static final String FB_MERCHANT_CONFIG = "facebookConfig";

    private static final String errorMessage = "{\"response_code\":\"101\",\"response_message\":\"Unknown Error\"}";

    @Autowired
    @Qualifier(value = "wrapperImpl")
    private IWrapperService wrapperService;

    @Autowired
    @Qualifier(value = "merchantPayOptionService")
    private IMerchantPayOptionService merchantPayOptionService;

    @RequestMapping(value = "/payment/getPayMethods", method = RequestMethod.POST)
    public void getPayMethods(final HttpServletRequest request, final HttpServletResponse response, final Model model)
            throws ServletException, IOException {
        LOGGER.info("Received request for getPayMethods");
        PayOptionsRequest requestBean = null;
        try {
            requestBean = wrapperService.wrapRequest(request, FB_MERCHANT_CONFIG, API.PAY_OPTIONS);
            requestBean.setRequest(request);
            PayOptionsResponse payOptionsResponse = merchantPayOptionService.processPayMethodsRequest(requestBean);
            FBPayOptionsResponse fbPayOptionsResponse = mapToFBResponse(payOptionsResponse, requestBean);
            String finalResponse = JsonMapper.mapObjectToJson(fbPayOptionsResponse);
            response.getOutputStream().write(finalResponse.getBytes());
            response.setContentType("application/json");
        } catch (Exception e) {
            LOGGER.error("Exception , ", e);
            response.getOutputStream().write(errorMessage.getBytes());
        }
    }

    private FBPayOptionsResponse mapToFBResponse(PayOptionsResponse payOptionsResponse, PayOptionsRequest requestBean) {
        FBPayOptionsResponse fbPayOptionsResponse = new FBPayOptionsResponse();
        // TODO: No corresponding value.
        // fbPayOptionsResponse.setReferenceId();
        fbPayOptionsResponse.setRequestId((String) requestBean.getExtraParamsMap().get("request_id"));
        fbPayOptionsResponse.setResponseCode(payOptionsResponse.getRespCode());
        fbPayOptionsResponse.setResponseMessage(payOptionsResponse.getRespMsg());

        if (payOptionsResponse.getPaymentMethods() != null) {
            List<FBPaymentMethod> fbPaymentMethods = new ArrayList<>();
            for (PaymentMethod paymentMethod : payOptionsResponse.getPaymentMethods()) {
                FBPaymentMethod fbPaymentMethod = new FBPaymentMethod();
                fbPaymentMethod.setInstrumentType(paymentMethod.getPaymentMethod());

                if (paymentMethod.getPaymentChannels() != null) {
                    List<Object> enabledInstruments = new ArrayList<>();
                    for (PaymentChannel paymentChannel : paymentMethod.getPaymentChannels()) {
                        if ("true".equals(paymentChannel.getEnableStatus().toLowerCase())) {
                            if (PayMethod.NET_BANKING.getValue().equals(paymentMethod.getPaymentMethod())) {
                                FBInstrumentDetail fbInstrumentDetail = new FBInstrumentDetail();
                                fbInstrumentDetail.setName(paymentChannel.getInstName());
                                fbInstrumentDetail.setLocalName(paymentChannel.getInstName());
                                fbInstrumentDetail.setCode(paymentChannel.getInstId());
                                fbInstrumentDetail.setImage(paymentChannel.getBankLogo());
                                enabledInstruments.add(fbInstrumentDetail);
                            } else {
                                enabledInstruments.add(paymentChannel.getInstId());
                            }
                        }
                    }
                    fbPaymentMethod.setEnabledInstruments(enabledInstruments);
                }
                fbPaymentMethods.add(fbPaymentMethod);
            }
            fbPayOptionsResponse.setEnabledPaymentMethods(fbPaymentMethods);
        }
        return fbPayOptionsResponse;
    }
}
