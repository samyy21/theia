package com.paytm.pgplus.theia.controllers.async;

import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.interceptors.shadowtraffic.ShadowTrafficUtil;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.IS_MOCK_REQUEST;

@Provider
public class TransactionStatusFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusFilter.class);
    private static final String TRANSACTION_STATUS = "transactionStatus";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Inside Jersey filter");
        processForShadowTraffic();
        String flag = ConfigurationUtil.getProperty("signature.required");

        if (!TheiaConstant.ExtraConstants.SIGNATURE_REQUIRED.equals(flag)) {
            return;
        }

        List<String> list = new ArrayList<>(requestContext.getPropertyNames());
        TreeMap<String, String> map = new TreeMap<>();
        // map.put(TheiaConstant.ExtraConstants.MERCHANT_ID,
        // request.getParameter(TheiaConstant.ExtraConstants.MERCHANT_ID));
        map.put(TheiaConstant.ExtraConstants.MERCHANT_ID,
                (String) requestContext.getProperty(TheiaConstant.ExtraConstants.MERCHANT_ID));
        // map.put(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID,
        // request.getParameter(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID));
        map.put(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID,
                (String) requestContext.getProperty(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID));
        // map.put(TheiaConstant.ExtraConstants.TRANS_ID,
        // request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID));
        map.put(TheiaConstant.ExtraConstants.TRANS_ID,
                (String) requestContext.getProperty(TheiaConstant.ExtraConstants.TRANS_ID));
        // map.put(TheiaConstant.ExtraConstants.PAYMENT_MODE,
        // request.getParameter(TheiaConstant.ExtraConstants.PAYMENT_MODE));
        map.put(TheiaConstant.ExtraConstants.PAYMENT_MODE,
                (String) requestContext.getProperty(TheiaConstant.ExtraConstants.PAYMENT_MODE));
        String signatureString = getSignatureString(map);
        String signature = (String) requestContext.getProperty(TheiaConstant.ExtraConstants.SIGNATURE);
        if (StringUtils.isBlank(signature) || StringUtils.isBlank(signatureString)
                || !SignatureUtilWrapper.verifySignature(signatureString, signature)) {
            StatisticsLogger.logForXflush(
                    (String) requestContext.getProperty(TheiaConstant.ExtraConstants.MERCHANT_ID), "transactionStatus",
                    (String) requestContext.getProperty(TheiaConstant.ExtraConstants.PAYMENT_MODE), "request",
                    "Signature received is invalid or null", null);
            LOGGER.error("Signature received is invalid or null : {} {} {}", signature, signatureString,
                    request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID));
            requestContext.abortWith(Response.temporaryRedirect(
                    URI.create(theiaViewResolverService.returnOOPSPage(request))).build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        LOGGER.info("Unset Shadow Context");
        ShadowTrafficUtil.unsetShadowContext();
    }

    private String getSignatureString(Map<String, String> paramMap) {

        StringBuilder signatureBuilder = new StringBuilder();
        for (Map.Entry<String, String> param : paramMap.entrySet()) {
            signatureBuilder.append(param.getValue()).append("|");
        }
        return signatureBuilder.toString();
    }

    private void processForShadowTraffic() {
        String uri = request.getRequestURI();
        String isMockRequest = null;
        if (uri.contains(TRANSACTION_STATUS)) {
            String transId = request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID);
            TransactionInfo transInfo = transactionCacheUtils.getTransInfoFromCache(transId);
            if (transInfo != null) {
                isMockRequest = transInfo.getIsMockRequest();
            }
        } else {
            isMockRequest = (String) request.getSession().getAttribute(IS_MOCK_REQUEST);
        }
        if (Boolean.TRUE.toString().equals(isMockRequest)) {
            ShadowTrafficUtil.setAttributesForShadowContext();
        }
    }

}
