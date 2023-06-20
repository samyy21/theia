/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceValues;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.PaymentOTPService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * @author santosh
 */
@Controller
public class ResendOTPController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendOTPController.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private PaymentOTPService paymentOtpUtil;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @RequestMapping(value = "/resendPaymentOTP", method = { RequestMethod.GET, RequestMethod.POST })
    public void resendPaymentOTP(final HttpServletRequest request, final HttpServletResponse response,
            final HttpSession session, final Model model, final Locale locale) {
        final PaymentRequestBean paymentRequestData = new PaymentRequestBean(request);
        LOGGER.info("Request received for resend payment OTP : {}", paymentRequestData);
        String responseData = "1";

        try {
            if (!isLimitExceeded(paymentRequestData)) {
                paymentOtpUtil.resendPaymentOTP(paymentRequestData);
                responseData = "0";
            } else {
                LOGGER.error("Resend Count Breached!");
            }
        } catch (Exception e) {
            LOGGER.error("Exceptio occured : ", e);
        }
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("isValid", responseData);
            response.getWriter().print(jsonObject.toString());
        } catch (IOException e) {
            LOGGER.error("Cannot write in respone Payment Otp", e);
        }
        return;
    }

    private boolean isLimitExceeded(PaymentRequestBean requestData) {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());
        if (txnInfo.getPaymentOtpSendCount() < PreferenceValues.MAX_OTP_SEND_CONT) {
            txnInfo.setPaymentOtpSendCount(txnInfo.getPaymentOtpSendCount() + PreferenceValues.INCREMENT);
            return false;
        }
        return true;

    }
}