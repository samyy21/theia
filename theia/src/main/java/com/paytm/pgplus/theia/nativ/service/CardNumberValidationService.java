package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.enums.CardValidationCardType;
import com.paytm.pgplus.theia.enums.CardValidationStatus;
import com.paytm.pgplus.theia.enums.CybersourceResponseEnum;
import com.paytm.pgplus.theia.models.VisaCyberSourceRequest;
import com.paytm.pgplus.theia.models.VisaCyberSourceResponse;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponse;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponseBody;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequestBody;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
import com.paytm.pgplus.theia.services.IVisaCyberSourceService;
import com.paytm.pgplus.theia.utils.CardTypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.CyberSource.*;
import static com.paytm.pgplus.theia.enums.CardValidationCardType.EXPIRED;
import java.security.SecureRandom;

@Service(value = "cardNumberValidationService")
@Qualifier("cardNumberValidationService")
public class CardNumberValidationService implements ICardNumberValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardNumberValidationService.class);
    private static final String ALPHA_NUM = "9876543210ZYXWVUTSRQPONMLKJIHGFEDCBAabcdefghijklmnopqrstuvwxyz!@#$&_";

    @Autowired
    @Qualifier("binDetailService")
    private BinDetailService binDetailService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("visaCyberSourceServiceImpl")
    private IVisaCyberSourceService visaCyberSourceService;

    @Autowired
    private CardTypeUtil cardTypeUtil;

    @Override
    public CardNumberValidationResponse fetchCardNumberValidationDetail(
            CardNumberValidationRequest cardNumberValidationRequest) {

        String bin = cardNumberValidationRequest.getBody().getCardNumber().substring(0, 6);

        BinDetailRequestBody binDetailRequestBody = new BinDetailRequestBody();
        binDetailRequestBody.setBin(bin);
        binDetailRequestBody.setChannelId(cardNumberValidationRequest.getHead().getChannelId());
        BinDetailRequest binDetailRequest = new BinDetailRequest();
        binDetailRequest.setBody(binDetailRequestBody);
        LOGGER.info("Request of fetch bin detail in card validation API {}", binDetailRequest);
        BinDetailResponse binDetailResponse = binDetailService
                .fetchBinDetailsWithSuccessRateforThirdparty(binDetailRequest);
        LOGGER.info("Response of fetch bin detail in card calidation API {}", binDetailResponse);
        String expireDate = cardNumberValidationRequest.getBody().getExpireDate();
        VisaCyberSourceRequest visaCyberSourceRequest = new VisaCyberSourceRequest();
        if (expireDate != null && expireDate.length() == 6) {
            if (validateExpiryDate(expireDate.substring(0, 2), expireDate.substring(2, 6), bin)) {
                return getInvalidCardResponse(binDetailResponse, cardNumberValidationRequest.getBody().getRequestId(),
                        EXPIRED);
            }
            visaCyberSourceRequest.setExpiryMonth(expireDate.substring(0, 2));
            visaCyberSourceRequest.setExpiryYear(expireDate.substring(2, 6));
        }
        visaCyberSourceRequest.setCardNumber(cardNumberValidationRequest.getBody().getCardNumber());
        visaCyberSourceRequest.setAmount(ZERO);
        visaCyberSourceRequest.setCurrency(INDIAN_CURRENCY);
        visaCyberSourceRequest.setUniqueId(cardNumberValidationRequest.getBody().getRequestId());
        // CyberSource don't support Rupay card
        if (cardTypeUtil.checkForCardType(binDetailResponse, CardValidationCardType.RUPAY)) {
            return getInvalidCardResponse(binDetailResponse, cardNumberValidationRequest.getBody().getRequestId(),
                    CardValidationCardType.RUPAY);
        }
        VisaCyberSourceResponse visaCyberSourceResponse = visaCyberSourceService
                .getCardDetailFromVisaCyberSource(visaCyberSourceRequest);

        return prepareValidateCardResponse(binDetailResponse, visaCyberSourceResponse, cardNumberValidationRequest
                .getBody().getRequestId());
    }

    public String generateUniqueRandomString(int length) {
        String timeNano = String.valueOf(System.nanoTime());
        StringBuffer sb = new StringBuffer(length);
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int ndx = (int) (secureRandom.nextDouble() * ALPHA_NUM.length());
            sb.append(ALPHA_NUM.charAt(ndx));
        }
        return sb.toString() + timeNano;
    }

    private CardNumberValidationResponse getInvalidCardResponse(BinDetailResponse binDetailResponse, String requestId,
            CardValidationCardType cardType) {
        CardNumberValidationResponse cardNumberValidationResponse = new CardNumberValidationResponse();
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setRequestId(requestId);
        cardNumberValidationResponse.setHead(responseHeader);
        CardNumberValidationResponseBody cardNumberValidationResponseBody = new CardNumberValidationResponseBody();
        if (binDetailResponse != null && binDetailResponse.getBody() != null) {
            BinData binDetail = binDetailResponse.getBody().getBinDetail();
            if (binDetail != null) {
                cardNumberValidationResponseBody.setBankName(binDetail.getIssuingBank());
                cardNumberValidationResponseBody.setIconUrl(binDetailResponse.getBody().getIconUrl());
                cardNumberValidationResponseBody.setCardScheme(binDetail.getChannelName());
                cardNumberValidationResponseBody.setPaymentMode(binDetail.getPayMethod());
            }
        }
        if (cardType.getCardType().equalsIgnoreCase(EXPIRED.getCardType())) {
            cardNumberValidationResponseBody.setResultInfo(new com.paytm.pgplus.response.ResultInfo(
                    ResponseConstants.INVALID_EXPIRY_DATE.getCode(), ResponseConstants.INVALID_EXPIRY_DATE.getCode(),
                    EXPIRED_MESSAGE));
            cardNumberValidationResponseBody.setPerformanceStatus(FAILURE);
            cardNumberValidationResponseBody.setOperationCode(CybersourceResponseEnum.EXPIRED_CARD.getResponseCode());
            cardNumberValidationResponseBody.setReason(CybersourceResponseEnum.EXPIRED_CARD.getResponseMessage());
        } else {
            cardNumberValidationResponseBody.setPerformanceStatus(UNKNOWN);
            cardNumberValidationResponseBody.setOperationCode(CybersourceResponseEnum.getDefaultUnknownResponse()
                    .getResponseCode());
            cardNumberValidationResponseBody.setReason(CybersourceResponseEnum.getDefaultUnknownResponse()
                    .getResponseMessage());
        }
        cardNumberValidationResponse.setBody(cardNumberValidationResponseBody);
        LOGGER.info("Failed response {} ", cardNumberValidationResponse);
        return cardNumberValidationResponse;
    }

    public boolean validateExpiryDate(String expiryMonth, String expiryYear, String bin) {

        try {
            cardUtils.validateExpiryDate(expiryMonth, expiryYear, StringUtils.EMPTY, bin);
        } catch (PaytmValidationException e) {
            return true;
        }
        return false;
    }

    public CardNumberValidationResponse prepareValidateCardResponse(BinDetailResponse binDetailResponse,
            VisaCyberSourceResponse visaCyberSourceResponse, String requestId) {
        CardNumberValidationResponse cardNumberValidationResponse = new CardNumberValidationResponse();
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setRequestId(requestId);
        cardNumberValidationResponse.setHead(responseHeader);
        CardNumberValidationResponseBody cardNumberValidationResponseBody = new CardNumberValidationResponseBody();
        if (binDetailResponse != null && binDetailResponse.getBody() != null) {
            BinData binDetail = binDetailResponse.getBody().getBinDetail();
            if (binDetail != null) {
                cardNumberValidationResponseBody.setBankName(binDetail.getIssuingBank());
                cardNumberValidationResponseBody.setIconUrl(binDetailResponse.getBody().getIconUrl());
                cardNumberValidationResponseBody.setCardScheme(binDetail.getChannelName());
                cardNumberValidationResponseBody.setPaymentMode(binDetail.getPayMethod());
                if (binDetailResponse.getBody().isZeroSuccessRate()) {
                    cardNumberValidationResponseBody.setPerformanceStatus(FAILURE);
                    cardNumberValidationResponseBody.setOperationCode(CybersourceResponseEnum.ZERO_SUCCESS_RATE
                            .getResponseCode());
                    cardNumberValidationResponseBody.setReason(CybersourceResponseEnum.ZERO_SUCCESS_RATE
                            .getResponseMessage());
                    cardNumberValidationResponse.setBody(cardNumberValidationResponseBody);
                    LOGGER.info("SuccessRate is zero for cardValidationAPI");
                    return cardNumberValidationResponse;
                }
            }
        }
        getNewResultInfoResponse(visaCyberSourceResponse, cardNumberValidationResponseBody);
        cardNumberValidationResponse.setBody(cardNumberValidationResponseBody);
        return cardNumberValidationResponse;
    }

    private CardNumberValidationResponseBody getOldResultInfoResponse(VisaCyberSourceResponse visaCyberSourceResponse,
            CardNumberValidationResponseBody cardNumberValidationResponseBody) {
        if (visaCyberSourceResponse != null && visaCyberSourceResponse.getStatus() != null) {
            if (SUCCESSFUL_STATUS.equalsIgnoreCase(visaCyberSourceResponse.getStatus())) {
                cardNumberValidationResponseBody.setPerformanceStatus(SUCCESS);
            } else if (DECLINED.equalsIgnoreCase(visaCyberSourceResponse.getStatus())
                    || INVALID_REQUEST.equalsIgnoreCase(visaCyberSourceResponse.getStatus())) {
                cardNumberValidationResponseBody.setPerformanceStatus(FAILURE);
            } else {
                LOGGER.info("set Performance Status as UNKOWN");
                cardNumberValidationResponseBody.setPerformanceStatus(UNKNOWN);
            }
        } else {
            LOGGER.info("set Performance Status as UNKOWN because cyber source response is null");
            cardNumberValidationResponseBody.setPerformanceStatus(UNKNOWN);
        }
        LOGGER.info("Performance Status for card validation API is {}",
                cardNumberValidationResponseBody.getPerformanceStatus());
        return cardNumberValidationResponseBody;
    }

    private CardNumberValidationResponseBody getNewResultInfoResponse(VisaCyberSourceResponse visaCyberSourceResponse,
            CardNumberValidationResponseBody cardNumberValidationResponseBody) {
        CybersourceResponseEnum responseEnum = visaCyberSourceResponse.getResponseEnum() != null ? visaCyberSourceResponse
                .getResponseEnum() : CybersourceResponseEnum.getDefaultUnknownResponse();
        cardNumberValidationResponseBody.setOperationCode(responseEnum.getResponseCode());
        cardNumberValidationResponseBody.setReason(responseEnum.getResponseMessage());
        if (visaCyberSourceResponse != null && visaCyberSourceResponse.getStatus() != null) {
            CardValidationStatus cardValidationStatus = searchStatusInCardValidationEnum(visaCyberSourceResponse
                    .getStatus());
            cardNumberValidationResponseBody.setPerformanceStatus(cardValidationStatus.getPerformanceStatus());
            cardNumberValidationResponseBody.setResultInfo(new ResultInfo(cardValidationStatus.getResultStatus(),
                    cardValidationStatus.getResultCodeId(), cardValidationStatus.getResultMsg()));
        } else {
            LOGGER.info("set Performance Status as UNKOWN because cyber source response is null");
            cardNumberValidationResponseBody.setPerformanceStatus(UNKNOWN);
        }
        LOGGER.info("Performance Status for card validation API is {}",
                cardNumberValidationResponseBody.getPerformanceStatus());
        return cardNumberValidationResponseBody;
    }

    public static CardValidationStatus searchStatusInCardValidationEnum(String status) {
        for (CardValidationStatus cardValidationStatus : CardValidationStatus.values()) {
            if (cardValidationStatus.getCode().equalsIgnoreCase(status)) {
                return cardValidationStatus;
            }
        }
        return CardValidationStatus.UNKNOWN;
    }
}