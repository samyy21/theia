package com.paytm.pgplus.theia.nativ.supergw.util;

import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.theia.nativ.supergw.exception.JwtValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.JWT_TOKEN;

public class SuperGwValidationUtil {

    public static void validateJwtToken(Map<String, String> claims, String clientId, String clientSecret) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String jwtToken = servletRequest.getHeader(JWT_TOKEN);
        if (StringUtils.isBlank(jwtToken)
                || !JWTWithHmacSHA256.verifyJsonWebToken(claims, jwtToken, clientSecret, clientId)) {
            throw new JwtValidationException("JWT token is invalid");
        }
    }
}
