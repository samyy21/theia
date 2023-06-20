package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.FetchMerchantUserInfoRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantUserInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantUserInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IMerchantUserInfoService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;

@Service
public class FetchMerchantUserInfoRequestProcessor
        extends
        AbstractRequestProcessor<FetchMerchantUserInfoRequest, MerchantUserInfoResponse, MerchantUserInfoServiceRequest, MerchantUserInfoResponse> {

    @Autowired
    @Qualifier("merchantUserInfoService")
    private IMerchantUserInfoService<MerchantUserInfoServiceRequest, MerchantUserInfoResponse> merchantUserInfoService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected MerchantUserInfoServiceRequest preProcess(FetchMerchantUserInfoRequest request) {
        MerchantUserInfoServiceRequest wrapper = null;
        if (request.getHead().getTokenType().equals(TokenType.SSO.name())) {
            validateRequestParam(request);
            nativeValidationService.validateSSOToken(request.getHead().getToken(), request.getBody().getMid());
            wrapper = getServiceRequestForSsoToken(request);
        } else if (request.getHead().getTokenType().equals(TokenType.TXN_TOKEN.name())) {
            InitiateTransactionRequestBody orderDetail = validate(request);
            wrapper = getServiceRequestForTxnToken(request, orderDetail);
        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }
        return wrapper;
    }

    @Override
    protected MerchantUserInfoResponse onProcess(FetchMerchantUserInfoRequest request,
            MerchantUserInfoServiceRequest serviceRequest) {
        return merchantUserInfoService.fetchMerchantUserInfo(serviceRequest);
    }

    @Override
    protected MerchantUserInfoResponse postProcess(FetchMerchantUserInfoRequest request,
            MerchantUserInfoServiceRequest serviceRequest, MerchantUserInfoResponse serviceResponse) {
        return serviceResponse;
    }

    private InitiateTransactionRequestBody validate(FetchMerchantUserInfoRequest request) {
        validateRequestParam(request);
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getToken());
        validateMidOrderId(orderDetail, request);
        // String paytmSsoToken = orderDetail.getPaytmSsoToken();
        // if (StringUtils.isBlank(paytmSsoToken)) {
        // throw
        // PaymentRequestProcessingException.getException(ResultCode.OPERATAION_NOT_SUPPORTED);
        // }
        return orderDetail;
    }

    private MerchantUserInfoServiceRequest getServiceRequestForTxnToken(FetchMerchantUserInfoRequest request,
            InitiateTransactionRequestBody orderDetail) {
        MerchantUserInfoServiceRequest wrapper = new MerchantUserInfoServiceRequest(orderDetail.getMid(),
                orderDetail.getOrderId(), request.getHead().getTokenType(), request.getHead().getToken(),
                orderDetail.getPaytmSsoToken());
        return wrapper;
    }

    private MerchantUserInfoServiceRequest getServiceRequestForSsoToken(FetchMerchantUserInfoRequest request) {
        MerchantUserInfoServiceRequest wrapper = new MerchantUserInfoServiceRequest(request.getBody().getMid(), request
                .getBody().getOrderId(), request.getHead().getTokenType(), request.getHead().getToken());
        return wrapper;
    }

    private void validateMidOrderId(InitiateTransactionRequestBody orderDetail, FetchMerchantUserInfoRequest request) {
        String requestMid = request.getBody().getMid();
        String requestOrderId = request.getBody().getOrderId();
        if (!requestMid.equals(orderDetail.getMid())) {
            throw MidDoesnotMatchException.getException();
        } else if (!requestOrderId.equals(orderDetail.getOrderId())) {
            throw OrderIdDoesnotMatchException.getException();
        }
    }

    private void validateRequestParam(FetchMerchantUserInfoRequest request) {
        if (request.getBody().getMid().isEmpty()) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }
}
