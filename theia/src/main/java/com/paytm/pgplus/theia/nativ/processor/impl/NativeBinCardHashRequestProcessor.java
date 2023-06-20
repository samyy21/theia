package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.*;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.paymentoffer.util.FetchCardDetailsUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_BIN_LENGTH_CHANGE_6_TO_9;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_PROMO;

@Service("nativeBinCardHashRequestProcessor")
public class NativeBinCardHashRequestProcessor
        extends
        AbstractRequestProcessor<NativeBinCardHashAPIRequest, NativeBinCardHashAPIResponse, NativeBinCardHashAPIServiceReq, NativeBinCardHashAPIServiceRes> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeBinCardHashRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    @Qualifier("fetchCardDetailsUtility")
    private FetchCardDetailsUtil fetchCardDetailsUtility;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardsService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private CoftTokenDataService coftTokenDataService;

    @Override
    protected NativeBinCardHashAPIServiceReq preProcess(NativeBinCardHashAPIRequest request) {
        if (ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(), ENABLE_GCIN_ON_COFT_PROMO, false)) {
            validateCoftRequest(request);
        } else {
            validateRequest(request);
        }
        validateSavedCardId(request);

        UserDetailsBiz userDetails = validateSsoToken(request);

        NativeBinCardHashAPIServiceReq serviceReq = new NativeBinCardHashAPIServiceReq();
        serviceReq.setUserDetailsBiz(userDetails);

        return serviceReq;
    }

    @Override
    protected NativeBinCardHashAPIServiceRes onProcess(NativeBinCardHashAPIRequest request,
            NativeBinCardHashAPIServiceReq serviceReq) throws Exception {
        NativeBinCardHashAPIServiceRes serviceRes = new NativeBinCardHashAPIServiceRes();
        if (ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(), ENABLE_GCIN_ON_COFT_PROMO, false)) {
            fetchCoftCardDetails(request, serviceReq, serviceRes);
        } else {
            fetchCardDetails(request, serviceReq, serviceRes);
        }
        return serviceRes;
    }

    private void fetchCoftCardDetails(NativeBinCardHashAPIRequest request, NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes) throws Exception {
        String mid = request.getBody().getMid();
        String merchantCoftConfig = coftTokenDataService.getMerchantConfig(mid);

        if (StringUtils.isNotEmpty(request.getBody().getSavedCardId())) {
            String savedCardId = request.getBody().getSavedCardId();
            if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                fetchCardDetailsUtility.processForCoftTokenCardId(request, serviceRes, mid, merchantCoftConfig);
            } else if (savedCardId.length() > 15) {
                fetchCardDetailsUtility.processForCoftSavedCardId(request, serviceRes, mid, merchantCoftConfig);
            }
        } else if (Objects.nonNull(request.getBody().getCardTokenInfo())) {
            fetchCardDetailsUtility.processForCoftToken(request, serviceRes, mid, merchantCoftConfig);
        } else if (StringUtils.isNotEmpty(request.getBody().getCardNumber())) {
            fetchCardDetailsUtility.processForCoftCardNumber(request, serviceRes, mid, merchantCoftConfig);
        }
    }

    private void fetchCardDetails(NativeBinCardHashAPIRequest request, NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes) throws Exception {
        if (StringUtils.isNotBlank(request.getBody().getCardNumber())) {
            if (ff4JUtil.isFeatureEnabledForPromo(request.getBody().getMid())) {
                fetchCardDetailsUtility.processForCardNumber(serviceReq, serviceRes, request);
            } else {
                processForCardNumber(serviceReq, serviceRes, request);
            }
        }

        if (StringUtils.isNotBlank(request.getBody().getSavedCardId())) {
            if (request.getBody().getSavedCardId().length() > 15
                    || ff4JUtil.isFeatureEnabledForPromo(request.getBody().getMid())) {
                fetchCardDetailsUtility.processForSavedCardId(serviceReq, serviceRes, request);
            } else {
                processForSavedCardId(serviceReq, serviceRes, request);
            }
        }
    }

    @Override
    protected NativeBinCardHashAPIResponse postProcess(NativeBinCardHashAPIRequest request,
            NativeBinCardHashAPIServiceReq serviceReq, NativeBinCardHashAPIServiceRes serviceRes) throws Exception {

        NativeBinCardHashAPIResponse nativeBinCardHashAPIResponse = new NativeBinCardHashAPIResponse();
        NativeBinCardHashAPIResponseBody nativeBinCardHashAPIResponseBody = new NativeBinCardHashAPIResponseBody();

        NativeBinDetailResponse binDetailResponse = serviceRes.getBinDetailResponse();
        binDetailResponse.setHead(null);
        binDetailResponse.getBody().setResultInfo(null);

        nativeBinCardHashAPIResponseBody.setCardHash(serviceRes.getCardHash());
        nativeBinCardHashAPIResponseBody.setCardDetails(binDetailResponse.getBody());

        nativeBinCardHashAPIResponse.setHead(new ResponseHeader());
        nativeBinCardHashAPIResponse.setBody(nativeBinCardHashAPIResponseBody);

        return nativeBinCardHashAPIResponse;
    }

    private void processForCardNumber(NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes, NativeBinCardHashAPIRequest request) throws Exception {

        NativeBinDetailRequest binRequest = getNativeBinDetailRequest(request, request.getBody().getCardNumber());

        NativeBinDetailResponse binDetailResponse = getBinDetails(binRequest);

        /*
         * get cardNumber hash
         */
        String cardHash = getCardHash(request.getBody().getCardNumber());

        serviceRes.setCardHash(cardHash);
        serviceRes.setBinDetailResponse(binDetailResponse);

        if (request.getBody().isEightDigitBinRequired()) {
            serviceRes.getBinDetailResponse().getBody().getBinDetail()
                    .setBin(StringUtils.substring(request.getBody().getCardNumber(), 0, 8));
        }
    }

    private void processForSavedCardId(NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes, NativeBinCardHashAPIRequest request) throws Exception {

        Long savedCardId = Long.parseLong(request.getBody().getSavedCardId());
        UserDetailsBiz userDetailsBiz = serviceReq.getUserDetailsBiz();
        String userId = userDetailsBiz.getUserId();

        String cardNumber = cashierUtilService.getCardNumer(savedCardId, userId);

        NativeBinDetailRequest binRequest = getNativeBinDetailRequest(request, cardNumber);
        NativeBinDetailResponse binDetailResponse = getBinDetails(binRequest);

        /*
         * get cardNumber hash
         */
        String cardHash = getCardHash(cardNumber);

        serviceRes.setCardHash(cardHash);
        serviceRes.setBinDetailResponse(binDetailResponse);

        if (request.getBody().isEightDigitBinRequired()) {
            serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(StringUtils.substring(cardNumber, 0, 8));
        }
    }

    @SuppressWarnings("unchecked")
    public NativeBinDetailResponse getBinDetails(NativeBinDetailRequest binDetailRequest) throws Exception {
        IRequestProcessor<NativeBinDetailRequest, NativeBinDetailResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_BIN_DETAIL_REQUEST);
        return requestProcessor.process(binDetailRequest);
    }

    public String getCardHash(String cardNumber) throws Exception {
        return SignatureUtilWrapper.signApiRequest(cardNumber);
    }

    public NativeBinDetailRequest getNativeBinDetailRequest(NativeBinCardHashAPIRequest request, String cardNumber) {
        NativeBinDetailRequest binRequest = new NativeBinDetailRequest();
        TokenRequestHeader requestHeader = request.getHead();

        NativeBinDetailRequestBody binDetailRequestBody = new NativeBinDetailRequestBody();
        binDetailRequestBody.setMid(request.getBody().getMid());
        if (cardNumber.length() >= 9
                && ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(), ENABLE_BIN_LENGTH_CHANGE_6_TO_9, false)) {
            binDetailRequestBody.setBin(StringUtils.substring(cardNumber, 0, 9));
        } else {
            binDetailRequestBody.setBin(StringUtils.substring(cardNumber, 0, 6));
        }
        binDetailRequestBody.setTxnType(request.getBody().getPaymentFlow());

        binRequest.setHead(requestHeader);
        binRequest.setBody(binDetailRequestBody);

        return binRequest;
    }

    private void validateRequest(NativeBinCardHashAPIRequest request) {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }
        if (StringUtils.isBlank(request.getBody().getSavedCardId())
                && StringUtils.isBlank(request.getBody().getCardNumber())) {
            LOGGER.error("received both savedCardId and cardNumber empty");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }

        if (StringUtils.isNotBlank(request.getBody().getCardNumber())
                && StringUtils.length(request.getBody().getCardNumber()) < 6) {
            LOGGER.error("cardNumber length < 6");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }
    }

    private void validateCoftRequest(NativeBinCardHashAPIRequest request) {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }

        if (StringUtils.isBlank(request.getBody().getSavedCardId())
                && StringUtils.isBlank(request.getBody().getCardNumber())
                && Objects.isNull(request.getBody().getCardTokenInfo())) {
            LOGGER.error("received all savedCardId and cardNumber and cardTokenInfo empty");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }

        if (StringUtils.isNotBlank(request.getBody().getCardNumber())
                && StringUtils.length(request.getBody().getCardNumber()) < 6) {
            LOGGER.error("cardNumber length < 6");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }

        CardTokenInfo cardTokenInfo = request.getBody().getCardTokenInfo();
        if (Objects.nonNull(cardTokenInfo)
                && (StringUtils.isBlank(cardTokenInfo.getCardToken()) || StringUtils.isBlank(cardTokenInfo
                        .getPanUniqueReference()))) {
            LOGGER.error("Card Token or PAR is missing");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
        }
    }

    private void validateSavedCardId(NativeBinCardHashAPIRequest request) {
        String cardId = request.getBody().getSavedCardId();
        if (StringUtils.isNotBlank(cardId)) {
            String ssoToken = request.getHead().getToken();
            /*
             * ssoToken must not be empty if savedCardId is sent in request
             */
            LOGGER.error("received savedCardId but ssoToken empty");
            if (StringUtils.isBlank(ssoToken)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
            }
        }
    }

    private UserDetailsBiz validateSsoToken(NativeBinCardHashAPIRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            if (StringUtils.isBlank(request.getHead().getToken())) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION).build();
            }
            return nativeValidationService.validateSSOToken(request.getHead().getToken(), request.getBody().getMid());
        }
        return null;
    }

    public String getCardNumber(String cardId) {
        String cardNumber = null;
        try {
            SavedCardResponse<SavedCardVO> savedCardResponse = savedCardsService.getSavedCardByCardId(Long
                    .parseLong(cardId));

            if (null != savedCardResponse && savedCardResponse.getStatus()) {
                cardNumber = savedCardResponse.getResponseData().getCardNumber();
            } else {
                throw new Exception("Unable to find savedCard corresponding to cardId");
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to find cardNumber for the cardId");
        }
        return cardNumber;
    }
}
