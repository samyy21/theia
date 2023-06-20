package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelResponse;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST;
import static java.util.stream.Collectors.toList;

@Service
public class NativeFetchNetBankingPayChannelRequestProcessor
        extends
        AbstractRequestProcessor<NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse, NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse> {

    private static final String ADD_MONEY = "ADD_MONEY";
    private static final String MERCHANT = "MERCHANT";

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private Ff4jUtils ff4JUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchNetBankingPayChannelRequestProcessor.class);

    @Override
    protected NativeFetchNBPayChannelRequest preProcess(NativeFetchNBPayChannelRequest request) {

        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
        } else if (TokenType.GUEST.equals(request.getHead().getTokenType())) {
            validateWithGuestToken(request);
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateWithAccessToken(request);
        } else if (request.getBody().isSuperGwApiHit()) {
            validateWithPaymentContext(request);
        } else {
            validate(request);
        }
        return request;
    }

    @Override
    protected NativeFetchNBPayChannelResponse onProcess(NativeFetchNBPayChannelRequest request,
            NativeFetchNBPayChannelRequest serviceRequest) throws Exception {
        NativeCashierInfoResponse cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead()
                .getTxnToken());
        // this is when FPO is already hit with access token(SSO mandatory)
        if (cacheResponse == null && TokenType.GUEST.equals(request.getHead().getTokenType())) {
            CreateAccessTokenServiceRequest cachedRequest = accessTokenUtils.getAccessTokenDetail(request.getHead()
                    .getToken());
            if (cachedRequest.getPaytmSsoToken() != null) {
                cacheResponse = nativeSessionUtil.getCashierInfoResponse(nativeSessionUtil.createTokenForMidSSOFlow(
                        cachedRequest.getPaytmSsoToken(), request.getBody().getMid()));
            }
        }
        if (cacheResponse == null) {
            if (TokenType.SSO.equals(request.getHead().getTokenType())) {
                nativePaymentUtil.fetchPaymentOptionsWithSsoToken(request.getHead(), request.getBody().getMid(), false,
                        null);
            } else if (TokenType.GUEST.equals(request.getHead().getTokenType())
                    || TokenType.ACCESS.equals(request.getHead().getTokenType())) {
                nativePaymentUtil.fetchPaymentOptionsForGuest(request.getHead(), request.getBody().getMid(), true,
                        request.getBody().getReferenceId(), null);
            } else if (request.getBody().isSuperGwApiHit()) {
                nativePaymentUtil.fetchPaymentOptionsWithPaymentContext(request.getHead(), request.getBody().getMid(),
                        request.getBody().getReferenceId(), null, null, request.getBody().getUserDetails(), request
                                .getBody().getCustId());
            } else {
                nativePaymentUtil.fetchPaymentOptions(request.getHead(), null, null);
            }
            cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
        }
        NativeFetchNBPayChannelResponseBody responseBody = new NativeFetchNBPayChannelResponseBody();
        PayMethod allowedNBPayMethod = null;
        if (MERCHANT.equals(request.getBody().getType())) {
            PayOption merchantPayOption = cacheResponse.getBody().getMerchantPayOption();
            if (null != merchantPayOption && null != merchantPayOption.getPayMethods()) {
                allowedNBPayMethod = merchantPayOption.getPayMethods().stream()
                        .filter(s -> EPayMethod.NET_BANKING.getMethod().equals(s.getPayMethod())).findAny()
                        .orElse(null);
            }
        } else if (ADD_MONEY.equals(request.getBody().getType())) {
            PayOption addMoneyPayOption = cacheResponse.getBody().getAddMoneyPayOption();
            if (null != addMoneyPayOption && null != addMoneyPayOption.getPayMethods()) {
                allowedNBPayMethod = addMoneyPayOption.getPayMethods().stream()
                        .filter(s -> EPayMethod.NET_BANKING.getMethod().equals(s.getPayMethod())).findAny()
                        .orElse(null);
            }
        }
        if (allowedNBPayMethod != null) {
            removeZestFromNBOptions(allowedNBPayMethod.getPayChannelOptions());
        }
        setCompleteIconUrl(allowedNBPayMethod, cacheResponse, request.getBody().getMid());
        responseBody.setPayMethod(allowedNBPayMethod);
        sortNBPayChannelOptions(responseBody);
        return new NativeFetchNBPayChannelResponse(new ResponseHeader(), responseBody);
    }

    private void setCompleteIconUrl(PayMethod allowedNBPayMethod, NativeCashierInfoResponse cacheResponse, String mid) {
        if (!ff4JUtils.isFeatureEnabledOnMid(mid, "theia.completeIconUrl", false)) {
            return;
        }
        String iconBaseUrl = cacheResponse.getBody().getIconBaseUrl();
        if (StringUtils.isBlank(iconBaseUrl) || allowedNBPayMethod == null) {
            return;
        }
        for (PayChannelBase payChannelBase : allowedNBPayMethod.getPayChannelOptions()) {
            if (StringUtils.isBlank(payChannelBase.getIconUrl()) || payChannelBase.getIconUrl().contains(iconBaseUrl)) {
                continue;
            }
            payChannelBase.setIconUrl(iconBaseUrl + payChannelBase.getIconUrl());
        }
    }

    private void removeZestFromNBOptions(List<PayChannelBase> payChannelOptions) {
        if (!CollectionUtils.isEmpty(payChannelOptions)) {
            Iterator iterator = payChannelOptions.iterator();
            while (iterator.hasNext()) {
                Bank bank = (Bank) iterator.next();
                if (bank.getInstId() != null && bank.getInstId().equals(ZEST)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    private void sortNBPayChannelOptions(NativeFetchNBPayChannelResponseBody responseBody) {
        if (responseBody.getPayMethod() != null && responseBody.getPayMethod().getPayChannelOptions() != null
                && !responseBody.getPayMethod().getPayChannelOptions().isEmpty()) {
            List<Bank> bankPayChannelOptions = responseBody.getPayMethod().getPayChannelOptions().stream()
                    .map(Bank.class::cast).collect(toList());
            bankPayChannelOptions.sort(Comparator.comparing(Bank::getInstId));
            responseBody.getPayMethod().setPayChannelOptions(
                    bankPayChannelOptions.stream().map(PayChannelBase.class::cast).collect(toList()));
        }
    }

    @Override
    protected NativeFetchNBPayChannelResponse postProcess(NativeFetchNBPayChannelRequest request,
            NativeFetchNBPayChannelRequest serviceRequest, NativeFetchNBPayChannelResponse serviceResponse) {
        serviceResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        return serviceResponse;
    }

    private void validate(NativeFetchNBPayChannelRequest request) {
        validateRequestParam(request);
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());

    }

    private void validateRequestParam(NativeFetchNBPayChannelRequest request) {
        if (!MERCHANT.equals(request.getBody().getType()) && !ADD_MONEY.equals(request.getBody().getType())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void validateWithSsoToken(NativeFetchNBPayChannelRequest request) {
        validateRequestParam(request);
        nativeValidationService.validateMid(request.getBody().getMid());
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));
    }

    private void validateWithGuestToken(NativeFetchNBPayChannelRequest request) {
        nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
        String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID);
        nativeValidationService.validateMid(mid);
        request.getHead().setTxnToken(request.getHead().getToken());
    }

    private void validateWithPaymentContext(NativeFetchNBPayChannelRequest request) {
        /*
         * Validation for superGw Apis
         */
        request.getHead().setTxnToken(
                nativeSessionUtil.getCacheKeyForSuperGw(request.getBody().getMid(), request.getBody().getReferenceId(),
                        ERequestType.NATIVE.getType()));
    }

    private void validateWithAccessToken(NativeFetchNBPayChannelRequest request) {
        validateRequestParam(request);
        String mid = request.getBody().getMid();
        String referenceId = request.getBody().getReferenceId();
        String token = request.getHead().getToken();
        nativeValidationService.validateMid(mid);
        accessTokenUtils.validateAccessToken(mid, referenceId, token);
        request.getHead().setTxnToken(token);
        request.getHead().setTokenType(TokenType.ACCESS);
    }

}