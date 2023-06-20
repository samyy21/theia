package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailRequest;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailResponse;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.promo.IPromoHelper;
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
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;

@Service("nativePromoCodeDetailRequestProcessor")
public class NativePromoCodeDetailRequestProcessor
        extends
        AbstractRequestProcessor<NativePromoCodeDetailRequest, NativePromoCodeDetailResponse, NativePromoCodeDetailRequest, PromoCodeResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePromoCodeDetailRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("promoHelperImpl")
    private IPromoHelper nativePromoHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Override
    protected NativePromoCodeDetailRequest preProcess(NativePromoCodeDetailRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);

        } else {
            InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                    .getTxnToken());
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
            enrich(request, orderDetail);
            validate(request);
        }
        return request;
    }

    @Override
    protected PromoCodeResponse onProcess(NativePromoCodeDetailRequest request, NativePromoCodeDetailRequest serviceReq)
            throws Exception {

        PromoCodeResponse promoCodeResponse = nativePromoHelper.validatePromoCodePaymentMode(serviceReq);

        return promoCodeResponse;
    }

    @Override
    protected NativePromoCodeDetailResponse postProcess(NativePromoCodeDetailRequest request,
            NativePromoCodeDetailRequest serviceReq, PromoCodeResponse serviceRes) throws Exception {
        NativePromoCodeDetailResponse nativePromoCodeDetailResponse = transformResponse(serviceReq, serviceRes);
        return nativePromoCodeDetailResponse;
    }

    private void validate(NativePromoCodeDetailRequest request) {

        if (StringUtils.isEmpty(request.getBody().getPromoCode())) {
            throw RequestValidationException.getException(ResultCode.PROMO_CODE_INVALID);
        }
        if (StringUtils.isEmpty(request.getBody().getTxnType())) {
            throw RequestValidationException.getException(ResultCode.PROMO_CODE_VALIDATION_EXCEPTION);
        }

        if (((EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(request.getBody().getTxnType())) || (EPayMethod.DEBIT_CARD
                .getMethod().equalsIgnoreCase(request.getBody().getTxnType())))
                && StringUtils.isEmpty(request.getBody().getCardNumber())) {
            throw RequestValidationException.getException(ResultCode.PROMO_CODE_VALIDATION_EXCEPTION);
        }
        if (EPayMethod.NET_BANKING.getMethod().equalsIgnoreCase(request.getBody().getTxnType())
                && StringUtils.isEmpty(request.getBody().getBankCode())) {
            throw RequestValidationException.getException(ResultCode.PROMO_CODE_VALIDATION_EXCEPTION);
        }
    }

    private void enrich(NativePromoCodeDetailRequest request, InitiateTransactionRequestBody orderDetail) {
        request.getBody().setMid(orderDetail.getMid());
        request.getBody().setPromoCode(orderDetail.getPromoCode());
    }

    private NativePromoCodeDetailResponse transformResponse(NativePromoCodeDetailRequest serviceReq,
            PromoCodeResponse serviceRes) {

        NativePromoCodeDetailResponseBody body = new NativePromoCodeDetailResponseBody();
        boolean isEnhancedFlow = StringUtils.isNotBlank(serviceReq.getBody().getIsEnhancedFlow()) ? Boolean
                .valueOf(serviceReq.getBody().getIsEnhancedFlow()) : false;
        ResultInfo resultInfo = new ResultInfo();
        if (isEnhancedFlow) {
            if (serviceRes.getPromoResponseCode() != null
                    && ResponseCodeConstant.PROMO_SUCCESS.equals(serviceRes.getPromoResponseCode())) {
                resultInfo.setResultCode(ResultCode.SUCCESS.getResultCodeId());
                resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
            } else {
                resultInfo.setResultCode(ResultCode.PROMO_CODE_INVALID.getResultCodeId());
                resultInfo.setResultStatus(ResultCode.PROMO_CODE_INVALID.getResultStatus());
            }
            if (serviceRes.getPromoResponseCode() != null && serviceRes.getPromoCodeDetail() != null
                    && serviceRes.getPromoCodeDetail().getPromoMsg() != null) {
                resultInfo.setResultMsg(serviceRes.getPromoCodeDetail().getPromoMsg());
            } else {
                resultInfo.setResultMsg(ResultCode.PROMO_CODE_INVALID.getResultMsg());
            }
        } else {
            resultInfo.setResultCode(serviceRes.getPromoResponseCode());
            resultInfo.setResultMsg(serviceRes.getResultMsg());
            resultInfo.setResultStatus(serviceRes.getResultStatus());
        }

        body.setResultInfo(resultInfo);

        body.setCheckPromoValidityURL(serviceRes.getCheckPromoValidityURL());
        body.setPromoCodeDetail(serviceRes.getPromoCodeDetail());
        body.setNbBanks(serviceRes.getNbBanks());
        body.setPaymentModes(serviceRes.getPaymentModes());
        NativePromoCodeDetailResponse response = new NativePromoCodeDetailResponse(new ResponseHeader(), body);
        return response;

    }

    private void validateWithSsoToken(NativePromoCodeDetailRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        validate(request);
        request.getBody().setUserDetailsBiz(
                nativeValidationService.validateSSOToken(request.getHead().getToken(), request.getBody().getMid()));
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));
    }
}
