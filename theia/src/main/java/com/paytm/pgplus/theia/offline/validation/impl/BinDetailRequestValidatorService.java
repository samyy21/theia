package com.paytm.pgplus.theia.offline.validation.impl;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.validation.IBinDetailRequestValidatorService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("binDetailRequestValidatorService")
public class BinDetailRequestValidatorService implements IBinDetailRequestValidatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinDetailRequestValidatorService.class);

    public String validate(BinDetailRequest requestData) {

        if (requestData.getHead() == null) {
            LOGGER.info("validation failed as invalid Request header is Passed");
            return "InvalidRequestHeader";
        }

        if (StringUtils.isBlank(requestData.getHead().getVersion())) {
            LOGGER.info("validation failed as invalid Version is Passed");
            return "InvalidVersion";
        }

        if (StringUtils.isBlank(requestData.getHead().getRequestTimestamp())) {
            LOGGER.info("validation failed as invalid Time stamp is Passed");
            return "InvalidTimeStamp";
        }

        if (StringUtils.isBlank(requestData.getHead().getRequestId())) {
            LOGGER.info("validation failed as invalid Request Id is Passed");
            return "InvalidRequesttId";
        }

        if (StringUtils.isBlank(requestData.getHead().getMid())) {
            LOGGER.info("validation failed as invalid MID  is Passed");
            return "InvalidMID";
        }

        if (StringUtils.isBlank(requestData.getHead().getToken())) {
            LOGGER.info("validation failed as invalid token is Passed");
            return "InvalidToken";
        }

        // Validating body params
        if (requestData.getBody() == null) {
            LOGGER.info("validation failed as invalid Request data is Passed");
            return "InvalidRequest";
        }

        if (null == requestData.getBody().getRequestType()) {
            LOGGER.info("validation failed as invalid request type is Passed");
            return "InvalidRequestType";
        }

        if ((requestData.getBody().getRequestType() == ERequestType.OFFLINE && TokenType.SSO != requestData.getHead()
                .getTokenType())
                || (ERequestType.isNativeOrUniRequest(requestData.getBody().getRequestType()) && TokenType.TXN_TOKEN != requestData
                        .getHead().getTokenType())) {
            LOGGER.info("validation failed as invalid Client Id is Passed");
            return "InvalidTokenType";
        }

        if (StringUtils.isBlank(requestData.getBody().getBin()) || (requestData.getBody().getBin().length() < 6)
                || !StringUtils.isNumeric(requestData.getBody().getBin())) {
            LOGGER.info("validation failed as invalid BIN  is Passed");
            return "InvalidBIN";
        }

        /*
         * if (StringUtils.isBlank(requestData.getBody().getDeviceId())) {
         * LOGGER.info("validation failed as invalid Device ID  is Passed");
         * return "InvalidDeviceId"; }
         */

        if (requestData.getBody().getChannelId() == null) {
            LOGGER.info("validation failed as invalid Channel ID  is Passed");
            return "InvalidChannelId";
        }

        if (StringUtils.isBlank(requestData.getBody().getOrderId())) {
            LOGGER.info("validation failed as invalid Order ID  is Passed");
            return "InvalidOrderId";
        }

        return null;

    }

}
