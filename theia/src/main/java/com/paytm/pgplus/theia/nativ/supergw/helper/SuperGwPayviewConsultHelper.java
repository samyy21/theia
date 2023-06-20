package com.paytm.pgplus.theia.nativ.supergw.helper;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoV4Request;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;

@Service("superGwPayviewConsultHelper")
public class SuperGwPayviewConsultHelper {

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Environment environment;

    public NativeCashierInfoContainerRequest createFetchPayOptionsRequest(NativeCashierInfoV4Request request, String mid)
            throws Exception {
        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(request.getHead().getChannelId().toString());
        MDC.put(VERSION, Version_V2);
        // PGP-41107 set order in mdc and workflow in tokenrequest header
        MDC.put(ORDER_ID, request.getBody().getOrderId());
        tokenRequestHeader.setVersion(MDC.get(VERSION));
        tokenRequestHeader.setWorkFlow(request.getHead().getWorkFlow());
        com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest nativeCashierInfoRequest = new com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest();
        com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setMid(mid);
        nativeCashierInfoRequestBody.setOrderAmount(request.getBody().getOrderAmount());
        if (request.getBody().getMerchantUserInfo() != null) {
            nativeCashierInfoRequestBody.setCustId(request.getBody().getMerchantUserInfo().getCustId());
        }
        copyEnableDisablePaymentMode(nativeCashierInfoRequestBody, request.getBody().getEnablePaymentMode(), request
                .getBody().getDisablePaymentMode());
        UserDetailsBiz userDetailsBiz = null;
        if (request.getBody().getUserDetails() != null) {
            userDetailsBiz = getUserDetailsBizForFPO(request.getBody().getUserDetails());
        }
        nativeCashierInfoRequestBody.setReferenceId(request.getBody().getReferenceId());
        nativeCashierInfoRequestBody.setRequestType(request.getBody().getRequestType());
        nativeCashierInfoRequestBody.setOriginChannel(request.getBody().getOriginChannel());
        nativeCashierInfoRequestBody.setDeviceId(request.getBody().getDeviceId());
        nativeCashierInfoRequestBody.setProductCode(request.getBody().getProductCode());
        nativeCashierInfoRequestBody.setEightDigitBinRequired(request.getBody().isEightDigitBinRequired());
        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(request.getBody().getRequestType())) {
            nativeCashierInfoRequestBody.setSubscriptionTransactionRequestBody(request.getBody()
                    .getSubscriptionTransactionRequestBody());
        }
        nativeCashierInfoRequest.setHead(tokenRequestHeader);
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        NativePersistData nativePersistData = new NativePersistData(userDetailsBiz);

        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest, nativePersistData);
        return nativeCashierInfoContainerRequest;
    }

    private void copyEnableDisablePaymentMode(
            com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody nativeCashierInfoRequestBody,
            List<PaymentMode> enablePaymentMode, List<PaymentMode> disablePaymentMode) {
        if (enablePaymentMode != null) {
            List<com.paytm.pgplus.models.PaymentMode> list = new ArrayList<>();
            for (PaymentMode paymentMode1 : enablePaymentMode) {
                com.paytm.pgplus.models.PaymentMode paymentMode2 = new com.paytm.pgplus.models.PaymentMode();
                paymentMode2.setBanks(paymentMode1.getBanks());
                paymentMode2.setChannels(paymentMode1.getChannels());
                paymentMode2.setEmiType(paymentMode1.getEmiType());
                paymentMode2.setMode(paymentMode1.getMode());
                list.add(paymentMode2);
            }
            nativeCashierInfoRequestBody.setEnablePaymentMode(list);
        }
        if (disablePaymentMode != null) {
            List<com.paytm.pgplus.models.PaymentMode> list = new ArrayList<>();
            for (PaymentMode paymentMode1 : disablePaymentMode) {
                com.paytm.pgplus.models.PaymentMode paymentMode2 = new com.paytm.pgplus.models.PaymentMode();
                paymentMode2.setBanks(paymentMode1.getBanks());
                paymentMode2.setChannels(paymentMode1.getChannels());
                paymentMode2.setEmiType(paymentMode1.getEmiType());
                paymentMode2.setMode(paymentMode1.getMode());
                list.add(paymentMode2);
            }
            nativeCashierInfoRequestBody.setDisablePaymentMode(list);
        }

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

    public UserDetailsBiz getUserDetailsBizForFPO(UserDetails userDetails) throws MappingServiceClientException {
        return mappingUtil.mapUserDetails(userDetails);
    }

    public void validateJwt(NativeCashierInfoV4Request request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

}
