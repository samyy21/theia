package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.auth.*;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.DYNAMIC_QR_REQUIRED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay.SSO_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;

@Service("nativeLogoutUserProcessor")
public class NativeLogoutUserProcessor
        extends
        AbstractRequestProcessor<NativeUserLogoutRequest, NativeUserLogoutResponse, NativeUserLogoutServiceReqRes, NativeUserLogoutResponse> {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    private NativeValidationService nativeValidationService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Override
    protected NativeUserLogoutServiceReqRes preProcess(NativeUserLogoutRequest request) {

        CreateAccessTokenServiceRequest createTokenRequest = null;
        NativeUserLogoutServiceReqRes nativeUserLogoutServiceReqRes = new NativeUserLogoutServiceReqRes();

        if (request == null || request.getHead() == null) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }
        if (request.getBody() == null) {
            request.setBody(new NativeUserLogoutRequestBody());
        }
        request.getBody().setOriginalVersion(request.getHead().getVersion());
        if (TheiaConstant.RequestHeaders.Version_V3.equals(request.getHead().getVersion())) {
            request.getHead().setVersion(TheiaConstant.RequestHeaders.Version_V2);
        }

        if (TokenType.TXN_TOKEN == request.getHead().getTokenType()) {
            nativeValidationService.validateTxnToken(request.getHead().getTxnToken());
        }

        if (TokenType.ACCESS == request.getHead().getTokenType()) {
            String mid = request.getBody().getMid();
            nativeValidationService.validateMid(mid);
            createTokenRequest = accessTokenUtils.validateAccessToken(mid, request.getBody().getReferenceId(), request
                    .getHead().getToken());
            nativeUserLogoutServiceReqRes.setCreateAccessTokenServiceRequest(createTokenRequest);
        }

        if (TokenType.GUEST == request.getHead().getTokenType()) {
            nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
            String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
            nativeValidationService.validateMid(mid);
        }

        return nativeUserLogoutServiceReqRes;
    }

    @Override
    protected NativeUserLogoutResponse onProcess(NativeUserLogoutRequest request,
            NativeUserLogoutServiceReqRes serviceReqRes) throws Exception {

        nativeSessionUtil.deleteLoginCookie();
        nativeSessionUtil.deleteField(request.getHead().getToken(), SSO_TOKEN);
        nativeSessionUtil.deleteField(request.getHead().getToken(), "userDetails");

        if (TokenType.TXN_TOKEN == request.getHead().getTokenType()) {
            InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                    .getTxnToken());
            orderDetail.setPaytmSsoToken(null);
            nativeSessionUtil.setOrderDetail(request.getHead().getTxnToken(), orderDetail);

            if (StringUtils.equals(CHECKOUT, request.getHead().getWorkFlow())) {
                HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
                httpServletRequest.setAttribute(DYNAMIC_QR_REQUIRED, false);
            }
        }

        if (TokenType.ACCESS == request.getHead().getTokenType()) {
            serviceReqRes.getCreateAccessTokenServiceRequest().setPaytmSsoToken(null);
            nativeSessionUtil.setAccessTokenDetail(request.getHead().getToken(),
                    serviceReqRes.getCreateAccessTokenServiceRequest());
        }

        nativeSessionUtil.updateAuthenticatedFlagInCache(request.getHead().getTxnToken(), false);

        NativeCashierInfoResponse cashierData = getCashierData(request);

        serviceReqRes.setCashierInfoResponse(cashierData);

        return new NativeUserLogoutResponse();
    }

    @Override
    protected NativeUserLogoutResponse postProcess(NativeUserLogoutRequest request,
            NativeUserLogoutServiceReqRes serviceReqRes, NativeUserLogoutResponse response) throws Exception {

        NativeUserLogoutResponseBody nativeUserLogoutResponseBody = new NativeUserLogoutResponseBody();
        if (serviceReqRes.getCashierInfoResponse() != null) {
            nativeUserLogoutResponseBody.setCashierData(serviceReqRes.getCashierInfoResponse().getBody());
        }

        NativeUserLogoutResponse nativeUserLogoutResponse = new NativeUserLogoutResponse();
        nativeUserLogoutResponse.setBody(nativeUserLogoutResponseBody);
        nativeUserLogoutResponse.setHead(new ResponseHeader());
        if (TheiaConstant.RequestHeaders.Version_V2.equals(request.getBody().getOriginalVersion())
                || TheiaConstant.RequestHeaders.Version_V3.equals(request.getBody().getOriginalVersion())) {
            nativeUserLogoutResponse.getHead().setVersion(request.getBody().getOriginalVersion());
        }
        return nativeUserLogoutResponse;
    }

    private NativeCashierInfoResponse getCashierData(NativeUserLogoutRequest request) throws Exception {
        NativeCashierInfoResponse cashierData = nativePayviewConsultRequestProcessor
                .process(getNativePayviewConsultRequest(request));
        return cashierData;
    }

    private NativeCashierInfoContainerRequest getNativePayviewConsultRequest(NativeUserLogoutRequest request) {
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setMid(getMid(request));
        nativeCashierInfoRequestBody.setReferenceId(request.getBody().getReferenceId());
        if (TheiaConstant.RequestHeaders.Version_V3.equals(request.getBody().getOriginalVersion())) {
            nativeCashierInfoRequestBody.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        nativeCashierInfoRequest.setHead(request.getHead());

        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest);
        return nativeCashierInfoContainerRequest;
    }

    private String getMid(NativeUserLogoutRequest request) {
        if (TokenType.TXN_TOKEN == request.getHead().getTokenType()) {
            return nativeSessionUtil.getOrderDetail(request.getHead().getToken()).getMid();
        }

        if (TokenType.GUEST == request.getHead().getTokenType()) {
            return (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
        }

        if (TokenType.ACCESS == request.getHead().getTokenType()) {
            return request.getBody().getMid();
        }

        return null;
    }
}
