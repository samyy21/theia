package com.paytm.pgplus.theia.offline.validation;

import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequest;

@Service("fastForwardrequestValidator")
public class FastForwardRequestValidator {

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(FastForwardRequestValidator.class);

    public String validate(FastForwardRequest requestData) {

        String errorMessage = null;

        if (errorMessage == null && requestData.getHead() == null) {
            LOGGER.info("validation failed as invalid Request header is Passed");
            errorMessage = "InvalidRequestHeader";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getHead().getVersion())) {
            LOGGER.info("validation failed as invalid Version is Passed");
            errorMessage = "InvalidVersion";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getHead().getRequestTimestamp())) {
            LOGGER.info("validation failed as invalid Time stamp is Passed");
            errorMessage = "InvalidTimeStamp";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getHead().getClientId())) {
            LOGGER.info("validation failed as invalid Client Id is Passed");
            errorMessage = "InvalidClientId";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getHead().getToken())) {
            LOGGER.info("validation failed as invalid token is Passed");
            errorMessage = "InvalidToken";
        }

        if (errorMessage == null
                && (requestData.getHead().getTokenType() == null || !TokenType.SSO.equals(requestData.getHead()
                        .getTokenType()))) {
            LOGGER.info("validation failed as invalid TokenType is Passed");
            errorMessage = "InvalidTokenType";
        }

        // Validating body params
        if (errorMessage == null && requestData.getBody() == null) {
            LOGGER.info("validation failed as invalid Request data is Passed");
            errorMessage = "InvalidRequest";
        }

        if (errorMessage == null
                && (StringUtils.isBlank(requestData.getBody().getReqType()) || (!RequestTypes.AUTO_DEBIT
                        .equals(requestData.getBody().getReqType())
                        && !RequestTypes.LINK_BASED_PAYMENT_INVOICE.equals(requestData.getBody().getReqType()) && !RequestTypes.LINK_BASED_PAYMENT
                            .equals(requestData.getBody().getReqType())))) {
            LOGGER.info("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getHead().getMid())) {
            LOGGER.info("validation failed as invalid MID  is Passed");
            errorMessage = "InvalidMID";
        }

        if (errorMessage == null
                && (StringUtils.isBlank(requestData.getBody().getTxnAmount()) || !NumberUtils.isNumber(requestData
                        .getBody().getTxnAmount()))) {
            LOGGER.info("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount";
        }

        if (errorMessage == null && StringUtils.isBlank(requestData.getBody().getCustomerId())) {
            LOGGER.info("validation failed as invalid CustomerId is Passed");
            errorMessage = "InvalidCustomerID";
        }

        if (errorMessage == null && merchantExtendInfoUtils.isMerchantActiveOrBlocked(requestData.getHead().getMid())) {
            LOGGER.info("validation failed as merchant is Blocked or Inactive.");
            errorMessage = "MERCHANT_BLOCKED";
        }

        return errorMessage;

    }

}
