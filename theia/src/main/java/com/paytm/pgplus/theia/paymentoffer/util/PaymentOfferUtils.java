package com.paytm.pgplus.theia.paymentoffer.util;

import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.BaseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public class PaymentOfferUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOfferUtils.class);
    private static final String REQUEST_HEADER_KEY = "REQUEST_HEADER";

    public static String dateStringToMillis(String date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return String.valueOf(sdf.parse(date).getTime());
        } catch (ParseException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return null;
        }

    }

    public static <T> void validate(T bean) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        for (ConstraintViolation<T> violation : violations) {
            LOGGER.error("Invalid Request property {}, error {}", violation.getPropertyPath().toString(),
                    violation.getMessage());
        }
        if (!violations.isEmpty()) {
            throw RequestValidationException.getException();
        }
    }

    public static void validateChannelId(BaseHeader header) {
        if (header == null || header.getChannelId() == null) {
            LOGGER.error("Invalid Request property head.channelId, error null");
            throw RequestValidationException.getException();
        }
    }

    public static long getAmountInPaise(String amountInRupees) {
        return Long.parseLong(AmountUtils.getTransactionAmountInPaise(amountInRupees));
    }

    public static String getAmountInRupees(long amountInPaise) {
        return AmountUtils.getPaddedTransactionAmountInRupee(String.valueOf(amountInPaise));
    }

    public static String getSearchPaymentOffersCacheKey(String mid, Class cachedClass) {
        return cachedClass.getCanonicalName() + "_" + mid;
    }

    public static String getSearchPaymentOffersCacheKeyForDeals(String mid, Class cachedClass) {
        return cachedClass.getCanonicalName() + "_" + mid + "_" + TheiaConstant.RequestParams.Native.DEAL_FLOW;
    }

    public static String getSearchPaymentOffersCacheKey(String mid, List<String> itemIds, Class cachedClass) {
        return cachedClass.getCanonicalName() + "_" + mid + "_" + String.join("_", itemIds);
    }

    public static HttpServletRequest gethttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void setRequestHeader(TokenRequestHeader requestHeader) {
        gethttpServletRequest().setAttribute(REQUEST_HEADER_KEY, requestHeader);
    }

    public static TokenRequestHeader getRequestHeader() {
        return (TokenRequestHeader) gethttpServletRequest().getAttribute(REQUEST_HEADER_KEY);
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

    public static String getApplyPromoForCachedKey(String cardIndexNumber) {
        return cardIndexNumber;
    }

}
