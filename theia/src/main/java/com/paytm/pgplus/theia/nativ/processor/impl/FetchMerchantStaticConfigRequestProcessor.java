package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IMerchantStaticConfigService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_STATIC_CONFIG_JWT_SECRET_KEY;

@Service("fetchMerchantStaticConfigRequestProcessor")
public class FetchMerchantStaticConfigRequestProcessor
        extends
        AbstractRequestProcessor<MerchantStaticConfigRequest, MerchantStaticConfigResponse, MerchantStaticConfigServiceRequest, MerchantStaticConfigResponse> {
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(FetchMerchantStaticConfigRequestProcessor.class);

    @Autowired
    private IMerchantStaticConfigService merchantStaticConfigService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private Environment environment;

    @Override
    protected MerchantStaticConfigServiceRequest preProcess(MerchantStaticConfigRequest request) throws Exception {
        validateRequest(request);
        switch (request.getHead().getTokenType()) {
        case SSO:
            tokenValidationHelper.validateSSOToken(request.getHead().getToken(), request.getBody().getMid());
            break;
        case TXN_TOKEN:
            nativeValidationService.validateTxnToken(request.getHead().getToken());
            break;
        case ACCESS:
            accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getHead().getRequestId(), request
                    .getHead().getToken());
            break;
        case JWT:
            validateJWTToken(request);
            break;
        default:
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE);
        }
        return getServiceRequest(request);
    }

    @Override
    protected MerchantStaticConfigResponse onProcess(MerchantStaticConfigRequest request,
            MerchantStaticConfigServiceRequest merchantStaticConfigServiceRequest) throws Exception {
        return merchantStaticConfigService.getMerchantStaticConfig(merchantStaticConfigServiceRequest);
    }

    @Override
    protected MerchantStaticConfigResponse postProcess(MerchantStaticConfigRequest request,
            MerchantStaticConfigServiceRequest merchantStaticConfigServiceRequest,
            MerchantStaticConfigResponse merchantStaticConfigResponse) throws Exception {
        return merchantStaticConfigResponse;
    }

    private void validateRequest(MerchantStaticConfigRequest request) {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if (StringUtils.isBlank(request.getBody().getMid())) {
            EXT_LOGGER.error("Received empty mid");
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private MerchantStaticConfigServiceRequest getServiceRequest(MerchantStaticConfigRequest request) {
        return new MerchantStaticConfigServiceRequest(request.getBody().getMid());
    }

    private void validateJWTToken(MerchantStaticConfigRequest request) {

        String clientId = request.getHead().getClientId();
        String mid = request.getBody().getMid();

        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("iss", clientId);
        jwtClaims.put("mid", mid);

        String jwtToken = request.getHead().getToken();
        String secretKey = environment.getProperty(MERCHANT_STATIC_CONFIG_JWT_SECRET_KEY + "." + clientId);

        if (!JWTWithHmacSHA256.verifyJsonWebToken(jwtClaims, jwtToken, secretKey, clientId)) {
            throw com.paytm.pgplus.theia.offline.exceptions.RequestValidationException
                    .getException(com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_JWT);
        }
    }
}
