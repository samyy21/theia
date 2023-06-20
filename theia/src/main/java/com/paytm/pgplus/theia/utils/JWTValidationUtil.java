package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;

@Component
public class JWTValidationUtil {

    public static final String TXN_AMOUNT = "txnAmount";
    public static final String VALUE = "value";

    @Autowired
    private Environment environment;

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTValidationUtil.class);

    public boolean validateJWT(JSONObject json) {
        try {
            JSONObject head = json.getJSONObject("head");
            JSONObject body = json.getJSONObject("body");
            JSONObject txnAmount = body.getJSONObject(TXN_AMOUNT);

            Map<String, String> jwtClaims = new HashMap<>();

            jwtClaims.put(MID, body.getString(MID));
            jwtClaims.put(ORDER_ID, body.getString(ORDER_ID));
            jwtClaims.put(TXN_AMOUNT, txnAmount.getString(VALUE));

            String jwtKey = environment.getProperty("jwt.initiate.secret." + head.getString("clientId"));
            String jwtToken = head.getString("token");

            return JWTWithHmacSHA256.verifyJsonWebToken(jwtClaims, jwtToken, jwtKey);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while validating JWT ", e);
            return false;
        }
    }
}
