package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponse;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IBalanceInfoService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class NativeFetchBalanceInfoRequestProcessor
        extends
        AbstractRequestProcessor<FetchBalanceInfoRequest, BalanceInfoResponse, BalanceInfoServiceRequest, BalanceInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchBalanceInfoRequestProcessor.class);

    @Autowired
    @Qualifier("balanceInfoService")
    private IBalanceInfoService<FetchBalanceInfoRequest, BalanceInfoServiceRequest, BalanceInfoResponse> balanceInfoService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Override
    protected BalanceInfoServiceRequest preProcess(FetchBalanceInfoRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
            return createServiceRequest(request);
        } else if (TokenType.GUEST.equals(request.getHead().getTokenType())) {
            validateWithGuestToken(request);
            String ssoToken = nativeSessionUtil.getSsoToken(request.getHead().getToken());
            if (StringUtils.isEmpty(ssoToken)) {
                throw RequestValidationException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
            nativeValidationService.validateMid(mid);
            return createGuestServiceRequest(request, ssoToken);
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateAccessToken(request);
            String ssoToken = nativeSessionUtil.getSsoToken(request.getHead().getToken());
            if (StringUtils.isEmpty(ssoToken)) {
                throw RequestValidationException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            String mid = request.getBody().getMid();
            nativeValidationService.validateMid(mid);
            return createAccessTokenServiceRequest(request, ssoToken);
        }

        InitiateTransactionRequestBody orderDetail = validate(request);
        BalanceInfoServiceRequest wrapper = getServiceRequest(request, orderDetail);
        return wrapper;
    }

    @Override
    protected BalanceInfoResponse onProcess(FetchBalanceInfoRequest request, BalanceInfoServiceRequest serviceRequest) {
        return balanceInfoService.fetchBalance(request, serviceRequest);
    }

    @Override
    protected BalanceInfoResponse postProcess(FetchBalanceInfoRequest request,
            BalanceInfoServiceRequest serviceRequest, BalanceInfoResponse serviceResponse) {
        return serviceResponse;
    }

    private ResultInfo getFetchBalanceErrorResultInfo(ResultCode resultCode) {

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultMsg(resultCode.getResultMsg());
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getResultCodeId());
        return resultInfo;
    }

    private InitiateTransactionRequestBody validate(FetchBalanceInfoRequest request) {
        validateRequestParam(request);
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        String paytmSsoToken = orderDetail.getPaytmSsoToken();
        if (StringUtils.isBlank(paytmSsoToken)) {
            throw PaymentRequestProcessingException.getException(ResultCode.OPERATAION_NOT_SUPPORTED);
        }
        return orderDetail;
    }

    private BalanceInfoServiceRequest getServiceRequest(FetchBalanceInfoRequest request,
            InitiateTransactionRequestBody orderDetail) {
        BalanceInfoServiceRequest wrapper = new BalanceInfoServiceRequest(orderDetail.getMid(),
                orderDetail.getPaytmSsoToken(), request.getBody().getPaymentMode(), request.getHead().getTxnToken());
        return wrapper;
    }

    private void validateRequestParam(FetchBalanceInfoRequest request) {
        if (!EPayMethod.BALANCE.getMethod().equals(request.getBody().getPaymentMode())
                && !EPayMethod.PPBL.getOldName().equals(request.getBody().getPaymentMode())
                && !EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(request.getBody().getPaymentMode())
                && !EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod().equals(request.getBody().getPaymentMode())
                && !EPayMethod.GIFT_VOUCHER.getMethod().equals(request.getBody().getPaymentMode())
                && !EPayMethod.LOYALTY_POINT.getMethod().equals(request.getBody().getPaymentMode())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void validateWithSsoToken(FetchBalanceInfoRequest request) {
        if (StringUtils.isBlank(request.getHead().getToken())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        validateRequestParam(request);
        nativeValidationService.validateMid(request.getBody().getMid());
    }

    private BalanceInfoServiceRequest createServiceRequest(FetchBalanceInfoRequest request) {
        String ssoToken = request.getHead().getToken();
        String mid = request.getBody().getMid();
        String txnToken = nativeSessionUtil.createTokenForMidSSOFlow(ssoToken, mid);
        request.getHead().setTxnToken(txnToken);
        BalanceInfoServiceRequest serviceRequest = new BalanceInfoServiceRequest(mid, ssoToken, request.getBody()
                .getPaymentMode(), txnToken);
        return serviceRequest;
    }

    private void validateWithGuestToken(FetchBalanceInfoRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
        request.getHead().setTxnToken(request.getHead().getToken());
    }

    private void validateAccessToken(FetchBalanceInfoRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(), request
                .getHead().getToken());
        request.getHead().setTxnToken(request.getHead().getToken());
    }

    private BalanceInfoServiceRequest createAccessTokenServiceRequest(FetchBalanceInfoRequest request, String ssoToken) {
        String mid = request.getBody().getMid();
        BalanceInfoServiceRequest serviceRequest = new BalanceInfoServiceRequest(mid, ssoToken, request.getBody()
                .getPaymentMode(), request.getHead().getTxnToken());
        return serviceRequest;
    }

    private BalanceInfoServiceRequest createGuestServiceRequest(FetchBalanceInfoRequest request, String ssoToken) {
        String mid = request.getBody().getMid();
        BalanceInfoServiceRequest serviceRequest = new BalanceInfoServiceRequest(mid, ssoToken, request.getBody()
                .getPaymentMode(), request.getHead().getTxnToken());
        return serviceRequest;
    }
}