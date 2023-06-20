package com.paytm.pgplus.theia.emiSubvention.util;

import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class EmiSubventionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOfferUtils.class);
    private static final String REQUEST_HEADER_KEY = "REQUEST_HEADER_EMI_SUBVENTION";

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    public static HttpServletRequest gethttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void setRequestHeader(TokenRequestHeader requestHeader) {
        gethttpServletRequest().setAttribute(REQUEST_HEADER_KEY, requestHeader);
    }

    public static TokenRequestHeader getRequestHeader() {
        return (TokenRequestHeader) gethttpServletRequest().getAttribute(REQUEST_HEADER_KEY);
    }

    public void setParamsForBanksRequest(EmiBanksRequest request, String referenceId) {
        if (request.getBody() != null) {
            request.getBody().setReferenceId(referenceId);
        }
    }

    public void setParamsForTenureRequest(EmiTenuresRequest request, String referenceId) {
        if (request.getBody() != null) {
            request.getBody().setReferenceId(referenceId);
        }
    }

    public void setParamsForValidateRequest(ValidateEmiRequest request, String referenceId) {
        if (request.getBody() != null) {
            request.getBody().setReferenceId(referenceId);
        }
    }

    public void validateAccessToken(String mid, String referenceId, String token) {
        accessTokenUtils.validateAccessToken(mid, referenceId, token);
    }

    public static ResponseHeader createResponseHeader() {
        ResponseHeader responseHeader = new ResponseHeader();
        TokenRequestHeader requestHeader = getRequestHeader();
        if (requestHeader != null) {
            if (StringUtils.isNotBlank(requestHeader.getVersion())) {
                responseHeader.setVersion(requestHeader.getVersion());
            }
            responseHeader.setRequestId(requestHeader.getRequestId());
        }
        return responseHeader;
    }
}
