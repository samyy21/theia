package com.paytm.pgplus.theia.aoa.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequest;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ValidationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ValidationHelper.class);

    @Autowired
    private Environment environment;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private NativeValidationService nativeValidationService;

    public void validate(CreateOrderAndPaymentRequest request) {
        if (request.getBody().getPaymentDetails() == null) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        if (StringUtils.isBlank(request.getBody().getMid()) || StringUtils.isBlank(request.getBody().getOrderId())
                || StringUtils.isBlank(request.getHead().getClientId())
                || StringUtils.isBlank(request.getHead().getTokenType())
                || StringUtils.isBlank(request.getHead().getToken())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        if (!StringUtils.equals("JWT", request.getHead().getTokenType())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        final boolean isValid = validateJwtToken(request);
        if (!isValid) {
            throw RequestValidationException.getException(ResultCode.TOKEN_VALIDATION_EXCEPTION);
        }
    }

    public boolean validateJwtToken(CreateOrderAndPaymentRequest request) {
        if (ff4jUtils.isFeatureEnabled("theia.aoa.skip.jwt.validation.for.supergw.client", false)) {
            EXT_LOGGER.customInfo("skipping jwt validation");
            return true;
        }
        String clientId = request.getHead().getClientId();
        String signature = request.getHead().getToken();

        String mid = request.getBody().getMid();
        String orderId = request.getBody().getOrderId();
        String txnAmount = null;

        if (isSubscriptionRequest(request)) {
            txnAmount = request.getBody().getSubscriptionDetails().getTxnAmount().getValue();
        } else {
            txnAmount = request.getBody().getOrderDetails().getTxnAmount().getValue();
        }

        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("iss", clientId);
        jwtClaims.put("mid", mid);
        jwtClaims.put("orderId", orderId);
        jwtClaims.put("txnAmount", txnAmount);

        boolean isValid = false;
        try {
            String secretKey = environment.getProperty(clientId);
            if (StringUtils.isNotBlank(secretKey)) {
                isValid = JWTWithHmacSHA256.verifyJsonWebToken(jwtClaims, signature, secretKey, clientId);
            } else {
                LOGGER.error("Key not found in vault : {} ", secretKey);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while validating JWT token", e.getMessage());
        }

        if (isValid) {
            LOGGER.info("JWT validation successful");
        } else {
            LOGGER.error("JWT validation failed");
        }
        return isValid;
    }

    private boolean isSubscriptionRequest(CreateOrderAndPaymentRequest request) {
        return ERequestType.NATIVE_SUBSCRIPTION.getType().equals(request.getBody().getRequestType());
    }
}
