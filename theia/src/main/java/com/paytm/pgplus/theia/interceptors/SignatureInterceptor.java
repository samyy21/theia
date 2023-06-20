package com.paytm.pgplus.theia.interceptors;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;

/**
 * @author santoshkumar
 *
 */
@Component
public class SignatureInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureInterceptor.class);

    public static final String TRANSACTION_STATUS = "transactionStatus";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.contains(TRANSACTION_STATUS)
                && MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
            return super.preHandle(request, response, handler);
        }
        String flag = ConfigurationUtil.getProperty("signature.required");

        if (!ExtraConstants.SIGNATURE_REQUIRED.equals(flag)) {
            return super.preHandle(request, response, handler);
        }
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put(ExtraConstants.MERCHANT_ID, request.getParameter(ExtraConstants.MERCHANT_ID));
        map.put(ExtraConstants.CASHIER_REQUEST_ID, request.getParameter(ExtraConstants.CASHIER_REQUEST_ID));
        map.put(ExtraConstants.TRANS_ID, request.getParameter(ExtraConstants.TRANS_ID));
        map.put(ExtraConstants.PAYMENT_MODE, request.getParameter(ExtraConstants.PAYMENT_MODE));
        String signatureString = getSignatureString(map);
        String signature = request.getParameter(ExtraConstants.SIGNATURE);
        if (StringUtils.isBlank(signature) || StringUtils.isBlank(signatureString)
                || !SignatureUtilWrapper.verifySignature(signatureString, signature)) {
            StatisticsLogger.logForXflush(request.getParameter(ExtraConstants.MERCHANT_ID), "theia",
                    request.getParameter(ExtraConstants.PAYMENT_MODE), "request",
                    "Signature received is invalid or null", null);
            LOGGER.error("Signature received is invalid or null : {} {} {}", signature, signatureString,
                    request.getParameter(ExtraConstants.TRANS_ID));
            response.sendRedirect(theiaViewResolverService.returnOOPSPage(request));
            return false;
        }

        return super.preHandle(request, response, handler);
    }

    private String getSignatureString(Map<String, String> paramMap) {

        StringBuilder signatureBuilder = new StringBuilder();
        for (Map.Entry<String, String> param : paramMap.entrySet()) {
            signatureBuilder.append(param.getValue()).append("|");
        }
        return signatureBuilder.toString();
    }

}
