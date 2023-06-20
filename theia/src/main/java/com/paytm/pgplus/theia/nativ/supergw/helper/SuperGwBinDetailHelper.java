package com.paytm.pgplus.theia.nativ.supergw.helper;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailV4Request;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REFERENCE_ID;

@Service("superGwBinDetailHelper")
public class SuperGwBinDetailHelper {

    @Autowired
    private Environment environment;

    public void validateBin(NativeBinDetailV4Request request) {
        if (null == request.getBody() || null == request.getBody().getBin() || request.getBody().getBin().length() < 6) {
            throw BinDetailException.getException(ResultCode.BIN_NUMBER_EXCEPTION);
        }
    }

    public NativeBinDetailRequest createBinDetailRequest(NativeBinDetailV4Request request) {

        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(request.getHead().getChannelId().toString());
        tokenRequestHeader.setVersion(MDC.get(VERSION));

        NativeBinDetailRequest nativeBinDetailRequest = new NativeBinDetailRequest();
        NativeBinDetailRequestBody nativeBinDetailRequestBody = new NativeBinDetailRequestBody(request.getBody());
        nativeBinDetailRequestBody.setisEMIDetail(String.valueOf(request.getBody().isEMIDetail()));
        nativeBinDetailRequestBody.setEnablePaymentMode(request.getBody().getEnablePaymentMode());
        nativeBinDetailRequestBody.setDisablePaymentMode(request.getBody().getDisablePaymentMode());
        nativeBinDetailRequestBody.setSuperGwApiHit(true);
        nativeBinDetailRequestBody.setReferenceId(MDC.get(REFERENCE_ID));
        nativeBinDetailRequestBody.setUserDetails(request.getBody().getUserDetails());
        if (request.getBody().getMerchantUserInfo() != null) {
            nativeBinDetailRequestBody.setCustId(request.getBody().getMerchantUserInfo().getCustId());
        }
        nativeBinDetailRequestBody.setPaymentMode(request.getBody().getPaymentMode());
        nativeBinDetailRequestBody.setRequestType(request.getBody().getRequestType());
        if (ERequestType.NATIVE_SUBSCRIPTION.equals(request.getBody().getRequestType())) {
            nativeBinDetailRequestBody.setSubscriptionTransactionRequestBody(request.getBody()
                    .getSubscriptionTransactionRequestBody());
        }
        nativeBinDetailRequest.setHead(tokenRequestHeader);
        nativeBinDetailRequest.setBody(nativeBinDetailRequestBody);

        return nativeBinDetailRequest;
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

    public void validateJwt(NativeBinDetailV4Request request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(FacadeConstants.BIN, request.getBody().getBin());
        jwtClaims.put(FacadeConstants.IS_EMI_DETAIL, String.valueOf(request.getBody().isEMIDetail()));
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

}
