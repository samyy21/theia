package com.paytm.pgplus.theia.supercashoffer.service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.supercashoffer.enums.SupercashResponseConstants;
import com.paytm.pgplus.theia.supercashoffer.exceptions.SupercashException;
import com.paytm.pgplus.theia.supercashoffer.helper.SuperCashServiceHelper;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferRequest;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("SuperCashOffersService")
public class SuperCashOffersService implements ISuperCashOffersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperCashOffersService.class);

    @Autowired
    @Qualifier("superCashServiceHelper")
    private SuperCashServiceHelper superCashServiceHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Override
    public SuperCashOfferResponse applySuperCash(SuperCashOfferRequest superCashOfferRequest, boolean offlineFlow) {
        try {
            validations(superCashOfferRequest, superCashOfferRequest.getHead().getRequestId());
            return superCashServiceHelper.fetchSuperCashOffers(superCashOfferRequest, offlineFlow);
        } catch (Exception e) {
            LOGGER.error("Exception in SuperCashOffersService.applySuperCash : {}", e);
            SuperCashOfferResponse response = new SuperCashOfferResponse();
            response.setStatus(false);
            response.setError(e.getMessage());
            return response;
        }
    }

    private void validations(SuperCashOfferRequest request, String referenceId)
            throws SupercashException.SupercashIllegalParamException, RequestValidationException {
        if (request == null
                || request.getBody() == null
                || request.getHead() == null
                || StringUtils.isBlank(request.getBody().getMid())
                || (StringUtils.isBlank(request.getHead().getToken())
                        && StringUtils.isBlank(request.getHead().getSsoToken()) && StringUtils.isBlank(request
                        .getHead().getTxnToken()))) {
            LOGGER.error("Mid in body and token in head is mandatory ! ");
            throw new SupercashException.SupercashIllegalParamException(
                    SupercashResponseConstants.ILLEGAL_PARAMS.getMessage());
        }
        // Set sso token between token and ssoToken fields
        if (request.getHead().getTokenType().equals(TokenType.SSO)) {
            if (StringUtils.isBlank(request.getHead().getSsoToken()))
                request.getHead().setSsoToken(request.getHead().getToken());
        } else if (StringUtils.isNotBlank(request.getHead().getSsoToken())) {
            request.getHead().setTokenType(TokenType.SSO);
            request.getHead().setToken(request.getHead().getSsoToken());
        }

        // Set txn token between token and txnToken fields
        if (request.getHead().getTokenType().equals(TokenType.TXN_TOKEN)) {
            if (StringUtils.isBlank(request.getHead().getTxnToken()))
                request.getHead().setTxnToken(request.getHead().getToken());
        } else if (StringUtils.isNotBlank(request.getHead().getTxnToken())) {
            request.getHead().setTokenType(TokenType.TXN_TOKEN);
            request.getHead().setToken(request.getHead().getTxnToken());
        }

        // Token Verification logics
        if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateSSOToken(request.getHead().getToken(),
                    request.getBody().getMid());
            if (StringUtils.isBlank(request.getBody().getUserId())) {
                request.getBody().setUserId(userDetailsBiz.getUserId());
            } else if (!request.getBody().getUserId().equals(userDetailsBiz.getUserId())) {
                throw RequestValidationException.getException("UserId doesn't belong to "
                        + request.getHead().getTokenType());
            }
        } else if (TokenType.ACCESS == request.getHead().getTokenType()) {
            CreateAccessTokenServiceRequest accessTokenServiceRequest = accessTokenUtils.validateAccessToken(request
                    .getBody().getMid(), referenceId, request.getHead().getToken());
            if (StringUtils.isNotBlank(accessTokenServiceRequest.getPaytmSsoToken())) {
                UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(accessTokenServiceRequest
                        .getPaytmSsoToken(), TokenType.SSO, request.getBody(), request.getBody().getMid());
                if (StringUtils.isBlank(request.getBody().getUserId())) {
                    request.getBody().setUserId(userDetailsBiz.getUserId());
                } else if (!request.getBody().getUserId().equals(userDetailsBiz.getUserId())) {
                    throw RequestValidationException.getException("UserId doesn't belong to "
                            + request.getHead().getTokenType());
                }

            }
            if (StringUtils.isBlank(request.getBody().getUserId())) {
                throw RequestValidationException.getException("UserId can't be blank for tokenType "
                        + request.getHead().getTokenType());
            }
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType())) {

            String txnToken = request.getHead().getToken();
            InitiateTransactionRequestBody orderDetails = nativeSessionUtil.getOrderDetail(txnToken);
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateSSOToken(orderDetails.getPaytmSsoToken(),
                    request.getBody().getMid());

            if (orderDetails == null) {
                throw RequestValidationException.getException("Session Expired for token type "
                        + request.getHead().getTokenType().toString());
            }
            if (StringUtils.isBlank(request.getBody().getUserId())) {
                request.getBody().setUserId(userDetailsBiz.getUserId());
            } else if (!request.getBody().getUserId().equals(userDetailsBiz.getUserId())) {
                throw RequestValidationException.getException("UserId doesn't belong to "
                        + request.getHead().getTokenType());
            }

            if (StringUtils.isBlank(request.getBody().getUserId())) {
                throw RequestValidationException.getException("UserId can't be blank for tokenType "
                        + request.getHead().getTokenType());
            }
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }
}
