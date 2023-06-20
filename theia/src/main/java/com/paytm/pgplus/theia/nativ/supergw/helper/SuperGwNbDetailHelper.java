package com.paytm.pgplus.theia.nativ.supergw.helper;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theiacommon.supergw.payview.nb.FetchNBPayChannelRequest;
import com.paytm.pgplus.theiacommon.supergw.payview.nb.FetchNBPayChannelRequestBody;
import com.paytm.pgplus.theiacommon.supergw.payview.nb.NativeFetchNBPayChannelRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REFERENCE_ID;

@Service("superGwNbDetailHelper")
public class SuperGwNbDetailHelper {

    @Autowired
    private Environment environment;

    private static final String MERCHANT = "MERCHANT";
    private static final String ADD_MONEY = "ADD_MONEY";

    public void validateRequestParam(NativeFetchNBPayChannelRequest request) {
        if (!MERCHANT.equals(request.getBody().getType()) && !ADD_MONEY.equals(request.getBody().getType())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    public FetchNBPayChannelRequest transformRequest(NativeFetchNBPayChannelRequest request) {
        FetchNBPayChannelRequestBody body = new FetchNBPayChannelRequestBody();
        body.setType(request.getBody().getType());
        body.setMid(request.getBody().getMid());
        return new FetchNBPayChannelRequest(null, body);
    }

    public com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest createFetchNBRequest(
            NativeFetchNBPayChannelRequest request) {

        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(request.getHead().getChannelId().toString());
        tokenRequestHeader.setVersion(MDC.get(VERSION));

        com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest nativeFetchNBPayChannelRequest = new com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest();
        com.paytm.pgplus.theia.nativ.model.payview.nb.FetchNBPayChannelRequestBody fetchNBPayChannelRequestBody = new com.paytm.pgplus.theia.nativ.model.payview.nb.FetchNBPayChannelRequestBody(
                request.getBody());
        fetchNBPayChannelRequestBody.setSuperGwApiHit(true);
        fetchNBPayChannelRequestBody.setReferenceId(MDC.get(REFERENCE_ID));
        fetchNBPayChannelRequestBody.setUserDetails(request.getBody().getUserDetails());
        if (request.getBody().getMerchantUserInfo() != null) {
            fetchNBPayChannelRequestBody.setCustId(request.getBody().getMerchantUserInfo().getCustId());
        }
        nativeFetchNBPayChannelRequest.setHead(tokenRequestHeader);
        nativeFetchNBPayChannelRequest.setBody(fetchNBPayChannelRequestBody);

        return nativeFetchNBPayChannelRequest;

    }

    public TokenRequestHeader getTokenRequestHeader(String channel) {

        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setVersion("v4");
        tokenRequestHeader.setRequestTimestamp(Long.toString(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(channel)) {
            tokenRequestHeader.setChannelId(EChannelId.valueOf(channel.toUpperCase()));
        } else {
            tokenRequestHeader.setChannelId(EChannelId.WEB);
        }

        return tokenRequestHeader;

    }

    public void validateJwt(NativeFetchNBPayChannelRequest request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(FacadeConstants.TYPE, request.getBody().getType());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }
}
