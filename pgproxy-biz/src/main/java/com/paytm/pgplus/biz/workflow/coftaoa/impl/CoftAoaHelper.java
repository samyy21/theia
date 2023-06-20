package com.paytm.pgplus.biz.workflow.coftaoa.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JWTValidationUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static com.paytm.pgplus.dynamicwrapper.utils.JSONUtils.toJsonString;
import static com.paytm.pgplus.facade.constants.FacadeConstants.CoftAPIConstant.REQUEST_ID;
import static com.paytm.pgplus.facade.constants.FacadeConstants.ConsentAPIConstant.CONTENT_TYPE;
import static com.paytm.pgplus.facade.constants.FacadeConstants.ConsentAPIConstant.TIMESTAMP;
import static com.paytm.pgplus.facade.constants.FacadeConstants.ConsentAPIConstant.CLIENT;

public class CoftAoaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoftAoaHelper.class);

    public static String createJWToken(Map<String, String> jwtClaims, String secretKey) throws FacadeCheckedException {

        if (StringUtils.isBlank(secretKey)) {
            LOGGER.error("Secret Key is Blank.Can't create JWT Token CoftService");
            throw new FacadeCheckedException("Secret Key is Blank.Can't create JWT Token CoftService");
        }
        return JWTValidationUtil.createJWTToken(jwtClaims, "ts", secretKey);
    }

    public static MultivaluedMap<String, Object> prepareCommonHeaderMap() {
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headerMap.add(REQUEST_ID, UUID.randomUUID().toString());
        headerMap.add(TIMESTAMP, Long.toString(System.currentTimeMillis()));
        headerMap.add(CLIENT, "PG");
        return headerMap;
    }

    public static <Resp> Resp getResponse(Response response, Class<Resp> respClass) throws FacadeCheckedException {
        final String responseEntity = response.readEntity(String.class);
        final Resp responseObject = JsonMapper.mapJsonToObject(responseEntity, respClass);
        return responseObject;
    }
}
