package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.mapping.models.EMIValidBinsData;
import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.nativ.NativeConsultDetails;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.CardSchemeInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.CardSubType;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequest;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.offline.services.IBinDetailService;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.PrepaidCardValidationUtil;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIV_FETCH_BIN_DETAIL_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PrepaidCard.PREPAID_CARD_MAX_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CARD_PRE_AUTH_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_NOT_ALLOWD_ON_MERCHANT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_NOT_SUPPORTED_MESSAGE;

@Service("nativeBinDetailRequestProcessor")
public class NativeBinDetailRequestProcessor extends
        AbstractRequestProcessor<NativeBinDetailRequest, NativeBinDetailResponse, BinDetailRequest, BinDetailResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeBinDetailRequestProcessor.class);
    private static final String addMoney = "addMoney";

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("binDetailService")
    private IBinDetailService binDetailService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("hybridDisablingUtil")
    private HybridDisablingUtil hybridDisablingUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    NativeFetchPcfDetailsRequestProcessor nativeFetchPcfDetailsRequestProcessor;

    @Autowired
    @Qualifier("prepaidCardValidationUtil")
    private PrepaidCardValidationUtil prepaidCardValidationUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    @Qualifier("cardTransactionUtil")
    private CardTransactionUtil cardTransactionUtil;

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITH_ONLY_CARD_TYPE = "${cardType} card is not allowed for this payment. Please try paying using other cards/options.";

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE = "${bankCode} ${cardName} ${cardType} card is not allowed for this payment. Please try paying using other cards/options.";
    private static String CREDIT = "Credit";
    private static String DEBIT = "Debit";
    private static String PREPAID = "Prepaid";
    private static String CORPORATE = "Corporate";

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE = "${bankCode} ${cardName} ${cardType} card is not allowed for ${payMode} payment. Please try paying using other cards/options.";

    private static final String PAY_CHANNEL_NOT_AVAILABLE_DEFAULT_MSG = "The payment option is experiencing downtime, please try another payment source to complete the transaction";

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Override
    protected BinDetailRequest preProcess(NativeBinDetailRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
        } else if (TokenType.GUEST.equals(request.getHead().getTokenType())) {
            LOGGER.info("Received tokenType:{}", request.getHead().getTokenType().getType());
            validateWithGuestToken(request);
        } else if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())) {
            validateWithCheckSum(request);
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateWithAccessToken(request);
        } else if (request.getBody().isSuperGwApiHit() != null && request.getBody().isSuperGwApiHit()) {
            validateWithPaymentContext(request);
        } else {
            validate(request);
        }
        return transformRequest(request);
    }

    @Override
    protected BinDetailResponse onProcess(NativeBinDetailRequest request, BinDetailRequest serviceReq) throws Exception {
        setTrxnTokenInServiceReq(request, serviceReq);
        updateBodyWithMid(request, serviceReq);
        BinDetailResponse binDetailResponse = binDetailService.fetchBinDetails(serviceReq);
        updateTxnType(request);
        validatePayMethodAndChannelAllowed(request, binDetailResponse);
        return binDetailResponse;
    }

    private void updateBodyWithMid(NativeBinDetailRequest request, BinDetailRequest serviceReq) {
        String mid = serviceReq.getHead().getMid();
        if (StringUtils.isEmpty(mid) && request.getBody() != null && request.getBody().getMid() != null) {
            mid = request.getBody().getMid();
        }
        if (StringUtils.isEmpty(mid)) {
            if (!(TokenType.SSO.equals(request.getHead().getTokenType())
                    || TokenType.GUEST.equals(request.getHead().getTokenType()) || TokenType.CHECKSUM.equals(request
                    .getHead().getTokenType()))) {
                NativeInitiateRequest initiateRequest = nativeSessionUtil.getNativeInitiateRequest(request.getHead()
                        .getTxnToken());
                if (initiateRequest != null && initiateRequest.getInitiateTxnReq() != null
                        && initiateRequest.getInitiateTxnReq().getBody() != null) {
                    mid = initiateRequest.getInitiateTxnReq().getBody().getMid();
                }
            }
        }
        request.getBody().setMid(mid);
        serviceReq.getHead().setMid(mid);
    }

    private void setTrxnTokenInServiceReq(NativeBinDetailRequest request, BinDetailRequest serviceReq) {
        serviceReq.setHead(serviceReq.getHead() != null ? serviceReq.getHead() : new RequestHeader());
        serviceReq.getHead().setToken(request.getHead() != null ? request.getHead().getTxnToken() : null);
    }

    private void updateTxnType(NativeBinDetailRequest request) {
        if (request != null && request.getBody() != null) {
            String txnType = request.getBody().getTxnType();
            if (StringUtils.equals(txnType, EPayMode.ADDANDPAY.getValue())) {
                txnType = addMoney;
            } else {
                txnType = StringUtils.EMPTY;
            }
            request.getBody().setTxnType(txnType);
        }
    }

    private static String getErrorMessage(String txnType, String binPayMethod, String cardType, String payMode,
            Boolean prepaidCardSupported, String bankCode, MutableBoolean isBankDisabled,
            BinDetailResponseBody binDetailResponseBody, MutableBoolean isChannelDisabled, Boolean isEDCLinks) {
        try {
            if (isChannelDisabled.isTrue()) {
                return ConfigurationUtil.getProperty(
                        TheiaConstant.PaytmPropertyConstants.PAY_CHANNEL_NOT_AVAILABLE_MSG,
                        PAY_CHANNEL_NOT_AVAILABLE_DEFAULT_MSG);
            }
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("cardName", cardType);
            valuesMap.put("payMode", payMode);
            if (isBankDisabled.isTrue()) {
                valuesMap.put("bankCode", bankCode);
            } else {
                if (isEDCLinks) {
                    valuesMap.put("bankCode", bankCode);
                } else {
                    valuesMap.put("bankCode", "");
                }
            }
            if (txnType != null) {

                if (binPayMethod != null) {

                    switch (binPayMethod) {
                    case "CREDIT_CARD":
                    case "CC":
                    case "Credit Card": {
                        valuesMap.put("cardType", CREDIT);
                        if (BooleanUtils.isTrue(binDetailResponseBody.isCorporateCardFail())) {
                            valuesMap.put("cardType", CORPORATE);
                        } else if (BooleanUtils.isTrue(prepaidCardSupported)) {
                            valuesMap.put("cardType", PREPAID);
                        }
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
                        if (StringUtils.isNotBlank(payMode) && StringUtils.isNotBlank(cardType)
                                && !payMode.equals(SubsPaymentMode.UNKNOWN.name())) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE).trim();
                        } else if (StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE).trim();
                        } else {
                            return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
                        }
                    }
                    case "DEBIT_CARD":
                    case "DC":
                    case "Debit Card": {
                        valuesMap.put("cardType", DEBIT);
                        if (BooleanUtils.isTrue(binDetailResponseBody.isCorporateCardFail())) {
                            valuesMap.put("cardType", CORPORATE);
                        } else if (BooleanUtils.isTrue(prepaidCardSupported)) {
                            valuesMap.put("cardType", PREPAID);
                        }
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
                        if (StringUtils.isNotBlank(payMode) && StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE).trim();
                        } else if (StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE).trim();
                        } else {
                            return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
                        }
                    }

                    default:
                        return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();

                    }

                } else {

                    return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();

                }
            }
        } catch (Exception e) {
        }
        return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
    }

    private void validatePayMethodAndChannelAllowed(NativeBinDetailRequest request, BinDetailResponse binDetailResponse)
            throws Exception {
        boolean isEdcLinks = false;
        if (null != binDetailResponse && null != binDetailResponse.getBody()
                && null != binDetailResponse.getBody().getBinDetail()) {
            String binPayMethod = binDetailResponse.getBody().getBinDetail().getPayMethod();
            String bankCode = binDetailResponse.getBody().getBinDetail().getIssuingBankCode();
            binDetailResponse.getBody().getBinDetail()
                    .setChannelCode(binDetailResponse.getBody().getBinDetail().getChannelName());
            String channelCode = binDetailResponse.getBody().getBinDetail().getChannelCode();

            CardSchemeInfo cardSchemeInfo = cardUtils.getCardSchemeInfo(channelCode);
            if (channelCode.equals(CashierConstant.BAJAJFN) || channelCode.equals(CashierConstant.BAJAJ_CARD)) {
                cardSchemeInfo.setIsCVVRequired("false");
                cardSchemeInfo.setIsExpiryRequired("false");
            }

            if (cardSchemeInfo != null) {
                binDetailResponse.getBody().getBinDetail().setMinCardNum(cardSchemeInfo.getMinCardNumberLength());
                binDetailResponse.getBody().getBinDetail().setMaxCardNum(cardSchemeInfo.getMaxCardNumberLength());
                binDetailResponse.getBody().getBinDetail().setIsCVVRequired(cardSchemeInfo.getIsCVVRequired());
                binDetailResponse.getBody().getBinDetail().setCvvLength(cardSchemeInfo.getCvvLength());
                binDetailResponse.getBody().getBinDetail().setIsExpiryRequired(cardSchemeInfo.getIsExpiryRequired());
            }

            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead()
                    .getTxnToken());
            if (cashierInfoResponse != null && TokenType.GUEST.equals(request.getHead().getTokenType())) {
                LOGGER.info("FPO Served from cache for token {}", request.getHead().getTxnToken());
            }

            checkIfIssuerBlockOnMid(binDetailResponse, request, getTxnAmount(request));

            if (cashierInfoResponse == null) {
                HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes()).getRequest();
                if (Objects.nonNull(request.getBody().getCardPreAuthType())) {
                    httpServletRequest.setAttribute(CARD_PRE_AUTH_TYPE, request.getBody().getCardPreAuthType().name());
                }
                if (TokenType.SSO.equals(request.getHead().getTokenType())) {
                    nativePaymentUtil.fetchPaymentOptionsWithSsoToken(request.getHead(), request.getBody().getMid(),
                            false, null);
                } else if (TokenType.GUEST.equals(request.getHead().getTokenType())
                        || TokenType.CHECKSUM.equals(request.getHead().getTokenType())
                        || TokenType.ACCESS.equals(request.getHead().getTokenType())) {

                    nativePaymentUtil.fetchPaymentOptionsForGuest(request.getHead(), request.getBody().getMid(), false,
                            request.getBody().getReferenceId(), null);

                    String txnToken = request.getHead().getTxnToken();
                    String mid = request.getBody().getMid();
                    if (StringUtils.equals(txnToken, nativeSessionUtil.createGuestTokenOnMidForFetchBinDetailApi(mid))
                            && ff4jUtils.isFeatureEnabledOnMid(mid,
                                    TheiaConstant.FF4J.ENABLE_CACHING_FOR_FPO_IN_FETCH_BIN_DETAIL, false)) {
                        LOGGER.info("updating Txn Token expiry for mid : {}", mid);
                        nativeSessionUtil.updateTokenExpiryForGuestReqInFetchBinDetailApi(txnToken);
                    }

                } else if (request.getBody().isSuperGwApiHit() != null && request.getBody().isSuperGwApiHit()) {
                    nativePaymentUtil.fetchPaymentOptionsWithPaymentContext(request.getHead(), request.getBody()
                            .getMid(), request.getBody().getReferenceId(), request.getBody().getEnablePaymentMode(),
                            request.getBody().getDisablePaymentMode(), request.getBody().getUserDetails(), request
                                    .getBody().getCustId(), request.getBody().getRequestType(), request.getBody()
                                    .getSubscriptionTransactionRequestBody());
                } else {
                    nativePaymentUtil.fetchPaymentOptions(request.getHead(), null, null);
                }
                httpServletRequest.removeAttribute(CARD_PRE_AUTH_TYPE);
                cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
            }

            if (!org.springframework.util.CollectionUtils
                    .isEmpty(cashierInfoResponse.getBody().getChannelCoftPayment())
                    && cashierInfoResponse.getBody().getChannelCoftPayment().get(binPayMethod + "_" + channelCode) != null
                    && iPgpFf4jClient.checkWithdefault("theia.getTokenizedCardsInFPO", null, true)) {
                binDetailResponse
                        .getBody()
                        .getBinDetail()
                        .setIsCoftPaymentSupported(
                                String.valueOf(cashierInfoResponse.getBody().getChannelCoftPayment()
                                        .get(binPayMethod + "_" + channelCode)));
            } else {
                binDetailResponse.getBody().getBinDetail().setIsCoftPaymentSupported("false");
            }

            if (cashierInfoResponse != null && cashierInfoResponse.getBody() != null
                    && cashierInfoResponse.getBody().getLink() != null
                    && cashierInfoResponse.getBody().getLink().getEdcEmiLinkPaymentDetails() != null) {
                isEdcLinks = true;
            }

            boolean prepaidFeatureDisabled = false;
            if (BooleanUtils.isNotTrue(binDetailResponse.getBody().getPrepaidCard())
                    || !iPgpFf4jClient.checkWithdefault(TheiaConstant.PrepaidCard.FF4J_PREPAID_CARD_STRING,
                            new HashMap<>(), false)) {
                prepaidFeatureDisabled = true;
            }

            String txnAmount = null;
            if (!prepaidFeatureDisabled) {
                txnAmount = getTxnAmount(request);
            }

            MutableBoolean isBankDisabled = new MutableBoolean(false);
            MutableBoolean isChannelDisabled = new MutableBoolean(false);

            if (StringUtils.isEmpty(request.getBody().getTxnType())
                    || "merchant".equals(request.getBody().getTxnType())) {
                if (!(cashierInfoResponse.getBody().isZeroCostEmi() && (StringUtils.equals(binPayMethod,
                        EPayMethod.CREDIT_CARD.getMethod()) || StringUtils.equals(binPayMethod,
                        EPayMethod.DEBIT_CARD.getMethod())))) {

                    PayOption merchantPayOption = cashierInfoResponse.getBody().getMerchantPayOption();

                    if (isDisabledByContract(binPayMethod, channelCode, merchantPayOption, request.getBody(), bankCode,
                            false, txnAmount, prepaidFeatureDisabled, isBankDisabled, binDetailResponse.getBody(),
                            isChannelDisabled)) {

                        LOGGER.error(
                                "Payment not allowed on bin - isZeroCostEmi: {} binPayMethod: {} channelCode: {} bin: {}, bank: {} ,merchantPayOption: {} , paymentMode: {}",
                                cashierInfoResponse.getBody().isZeroCostEmi(), binPayMethod, channelCode,
                                binDetailResponse.getBody().getBinDetail().getBin(), binDetailResponse.getBody()
                                        .getBinDetail().getIssuingBankCode(), merchantPayOption, request.getBody()
                                        .getPaymentMode());

                        BinDetailException exception = BinDetailException
                                .getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN);

                        exception.getResultInfo().setResultMsg(
                                getErrorMessage(request.getBody().getTxnType(), binPayMethod, channelCode, request
                                        .getBody().getPaymentMode(), binDetailResponse.getBody().getPrepaidCard(),
                                        bankCode, isBankDisabled, binDetailResponse.getBody(), isChannelDisabled,
                                        isEdcLinks));
                        throw exception;
                    }
                    try {
                        if (TheiaConstant.ExtraConstants.FALSE.equals(binDetailResponse.getBody().getBinDetail()
                                .getIsIndian())
                                && merchantPayOption != null) {

                            nativePaymentUtil.validateInternationalCard(merchantPayOption, channelCode,
                                    binDetailResponse.getBody().getBinDetail().getPayMethod());
                        }
                    } catch (PaytmValidationException e) {
                        throw BinDetailException.getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN,
                                CARD_NOT_SUPPORTED_MESSAGE);
                    }

                    setOneClickSupportedAndPrepaidCardSupported(binDetailResponse, merchantPayOption, channelCode,
                            binPayMethod);
                }

            } else if (addMoney.equals(request.getBody().getTxnType())) {
                PayOption addMoneyPayOption = cashierInfoResponse.getBody().getAddMoneyPayOption();

                if (isDisabledByContract(binPayMethod, channelCode, addMoneyPayOption, request.getBody(), bankCode,
                        true, txnAmount, prepaidFeatureDisabled, isBankDisabled, binDetailResponse.getBody(),
                        isChannelDisabled)) {
                    LOGGER.error(
                            "add money payment not allowed on bin - binPayMethod: {} channelCode: {} bin: {}, bank: {} , paymentMode: {}",
                            binPayMethod, channelCode, binDetailResponse.getBody().getBinDetail().getBin(),
                            binDetailResponse.getBody().getBinDetail().getIssuingBankCode(), request.getBody()
                                    .getPaymentMode());

                    BinDetailException exception = BinDetailException
                            .getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN);

                    exception.getResultInfo().setResultMsg(
                            getErrorMessage(request.getBody().getTxnType(), binPayMethod, channelCode, request
                                    .getBody().getPaymentMode(), binDetailResponse.getBody().getPrepaidCard(),
                                    bankCode, isBankDisabled, binDetailResponse.getBody(), isChannelDisabled,
                                    isEdcLinks));
                    throw exception;
                }
                try {
                    if (TheiaConstant.ExtraConstants.FALSE.equals(binDetailResponse.getBody().getBinDetail()
                            .getIsIndian())
                            && addMoneyPayOption != null) {

                        nativePaymentUtil.validateInternationalCard(addMoneyPayOption, channelCode, binDetailResponse
                                .getBody().getBinDetail().getPayMethod());
                    }
                } catch (PaytmValidationException e) {
                    throw BinDetailException.getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN,
                            CARD_NOT_SUPPORTED_MESSAGE);
                }

                /*
                 * Visa one click and prepaid not supported for add money
                 * merchant
                 */
                binDetailResponse.getBody().setOneClickSupported(false);
                binDetailResponse.getBody().setPrepaidCard(false);
            }
            if (!addMoney.equals(request.getBody().getTxnType()) && cashierInfoResponse.getBody().isPcfEnabled()) {
                FeeRateFactors feeRateFactors = new FeeRateFactors();
                feeRateFactors.setPrepaidCard(BooleanUtils.isTrue(binDetailResponse.getBody().getPrepaidCard()));
                feeRateFactors.setOneClickSupported(binDetailResponse.getBody().isOneClickSupported());
                feeRateFactors.setInternationalCardPayment(!BooleanUtils.toBoolean(binDetailResponse.getBody()
                        .getBinDetail().getIsIndian()));
                // if supported cardSubtype contains corporate card then set
                // feeRateFactor true
                if (CollectionUtils.isNotEmpty(binDetailResponse.getBody().getSupportedCardSubTypes())) {
                    feeRateFactors.setCorporateCard(binDetailResponse.getBody().getSupportedCardSubTypes()
                            .contains(CardSubType.CORPORATE_CARD.getCardSubType()));
                }

                Map<EPayMethod, NativeConsultDetails> consultFeeResponse = null;
                NativeConsultDetails consultDetails = null;
                String bin = request.getBody().getBin();
                if (TokenType.SSO.equals(request.getHead().getTokenType())
                        || TokenType.ACCESS.equals(request.getHead().getTokenType())) {
                    if (StringUtils.isNotEmpty(request.getBody().getTxnAmount())) {
                        if (StringUtils.equals(EPayMethod.EMI.getMethod(), (request.getBody().getPaymentMode()))) {
                            consultFeeResponse = consultFeeForGivenPaymodeWithSsoOrAccessToken(request, request
                                    .getBody().getPaymentMode(), request.getBody().getChannelCode(), feeRateFactors);
                            consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(request.getBody()
                                    .getPaymentMode()));
                        } else {
                            consultFeeResponse = consultFeeForGivenPaymodeWithSsoOrAccessToken(request, binPayMethod,
                                    channelCode, feeRateFactors);
                            consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(binPayMethod));
                        }
                    }
                } else if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())
                        && StringUtils.isNotEmpty(request.getBody().getTxnAmount())) {
                    if (StringUtils.equals(EPayMethod.EMI.getMethod(), (request.getBody().getPaymentMode()))) {
                        consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(request.getHead()
                                .getTxnToken(), request.getBody().getPaymentMode(), request.getBody().getChannelCode(),
                                feeRateFactors, bin);
                        consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(request.getBody()
                                .getPaymentMode()));
                    } else {
                        consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(request.getHead()
                                .getTxnToken(), binPayMethod, channelCode, feeRateFactors, bin);
                        consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(binPayMethod));
                    }
                } else {
                    if (StringUtils.equals(EPayMethod.EMI.getMethod(), (request.getBody().getPaymentMode()))) {
                        consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(request.getHead()
                                .getTxnToken(), request.getBody().getPaymentMode(), request.getBody().getChannelCode(),
                                feeRateFactors, bin);
                        consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(request.getBody()
                                .getPaymentMode()));
                    } else {
                        consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(request.getHead()
                                .getTxnToken(), binPayMethod, channelCode, feeRateFactors, bin);
                        consultDetails = consultFeeResponse.get(EPayMethod.getPayMethodByMethod(binPayMethod));
                    }
                }
                if (consultDetails != null) {
                    PCFFeeCharges pcfFeeCharges = new PCFFeeCharges(consultDetails.getFeeAmount(),
                            consultDetails.getTaxAmount(), consultDetails.getTotalTransactionAmount());
                    binDetailResponse.getBody().setPcfFeeCharges(pcfFeeCharges);
                }

            }
        }

    }

    private String getTxnAmount(NativeBinDetailRequest request) {
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(request.getHead()
                .getTxnToken());
        if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
            InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
            if (orderDetail != null) {
                return (orderDetail.getTxnAmount() != null) ? orderDetail.getTxnAmount().getValue() : null;
            }
        }
        return null;
    }

    private void checkIfIssuerBlockOnMid(BinDetailResponse binDetailResponse, NativeBinDetailRequest request,
            String txnAmount) {
        BinData binDetail = binDetailResponse.getBody().getBinDetail();
        Map<String, String> issuerMap = coftUtil.getIssuerTokenProcessingOnMidMap();
        txnAmount = StringUtils.isNotEmpty(txnAmount) ? txnAmount : request.getBody().getTxnAmount();
        if (Objects.nonNull(binDetail)) {
            String issuer = request.getBody().getMid() + "." + binDetail.getPayMethod() + "."
                    + binDetail.getChannelCode() + "." + binDetail.getIssuingBankCode();
            if (MapUtils.isNotEmpty(issuerMap) && !coftUtil.checkTokenProcessingEnable(issuerMap, txnAmount, issuer)) {
                throw BinDetailException.getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN,
                        CARD_NOT_ALLOWD_ON_MERCHANT);
            }
            checkIfIssuerAllowedOnMid(request, issuer);
        }
    }

    private void setOneClickSupportedAndPrepaidCardSupported(BinDetailResponse binDetailResponse,
            PayOption merchantPayOption, String channelCode, String binPayMethod) {

        MutableBoolean isoneClickSupported = new MutableBoolean(false);
        MutableBoolean prepaidCardSupported = new MutableBoolean(false);

        /*
         * Checking whether one click is supported for this particular scheme on
         * merchant
         */
        if ((null != merchantPayOption && null != binPayMethod && null != channelCode)) {
            List<PayMethod> payMethods = merchantPayOption.getPayMethods();
            if (null != payMethods && !payMethods.isEmpty()) {
                for (PayMethod payMethod : payMethods) {

                    if (binPayMethod.equals(payMethod.getPayMethod())) {
                        List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
                        if (isOneClickSupportedOrPrepaidSupported(channelCode, payChannelOptions, isoneClickSupported,
                                prepaidCardSupported)) {
                            break;
                        }
                    }
                }
            }
        }

        Boolean binOneClickSupportedResponse = false;
        Boolean binPrepaidCardSupportedResponse = false;

        /*
         * setting oneClick response on basis of both bin support and merchant
         * litepayview Support
         */
        if (binDetailResponse.getBody().isOneClickSupported() && isoneClickSupported.booleanValue()) {
            binOneClickSupportedResponse = true;
        }
        if (BooleanUtils.isTrue(binDetailResponse.getBody().getPrepaidCard()) && prepaidCardSupported.booleanValue()) {
            binPrepaidCardSupportedResponse = true;
        }

        binDetailResponse.getBody().setOneClickSupported(binOneClickSupportedResponse);

        setEventLogForOneClickSupported(binDetailResponse);

        // flag based handling of response to be done
        binDetailResponse.getBody().setPrepaidCard(binPrepaidCardSupportedResponse);
        binDetailResponse.getBody().setPrepaidCardMaxAmount(
                ConfigurationUtil.getProperty(PREPAID_CARD_MAX_AMOUNT, "100000"));
    }

    private void setEventLogForOneClickSupported(BinDetailResponse binDetailResponse) {
        BinDetailResponseBody binDetailBody = binDetailResponse.getBody();
        Map<String, String> metaData = new HashMap<>();

        metaData.put("Message", "oneClickSupported:" + binDetailBody.isOneClickSupported());
        if (null != binDetailBody.getBinDetail()) {
            metaData.put("BIN", binDetailBody.getBinDetail().getBin());
        }
        EventUtils.pushTheiaEvents(EventNameEnum.ONE_CLICK_SUPPORTED, metaData);
    }

    /**
     * Method to check if issuer config is allowed on MID or not.
     *
     * @param request
     *            NativeBinDetailRequest
     * @param issuerConfig
     *            Issuer config string as :
     *            MID.CARD_TYPE.CARD_SCHEME.ISSUING_BANK
     */
    private void checkIfIssuerAllowedOnMid(NativeBinDetailRequest request, String issuerConfig) {
        boolean isAddNPayOrAddMoneyFlow = addMoney.equals(request.getBody().getTxnType())
                || nativePaymentUtil.isAddMoneyFlow(request.getBody().getMid(), request.getHead().getTxnToken(), null);
        if (!cardTransactionUtil.isIssuerConfigAllowedOnMid(request.getBody().getRequestType(), request.getBody()
                .getMid(), issuerConfig, isAddNPayOrAddMoneyFlow)) {
            String customErrorMsg = cardTransactionUtil.getCustomErrMsgForIssuerNotAllowed(request.getBody().getMid());
            throw BinDetailException.getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN,
                    StringUtils.isNotBlank(customErrorMsg) ? customErrorMsg : CARD_NOT_ALLOWD_ON_MERCHANT);
        }
    }

    private boolean isOneClickSupportedOrPrepaidSupported(String channelCode, List<PayChannelBase> payChannelOptions,
            MutableBoolean isOneClickSupported, MutableBoolean prepaidCardSupported) {

        if (CollectionUtils.isNotEmpty(payChannelOptions)) {
            for (PayChannelBase payChannel : payChannelOptions) {
                if (payChannel instanceof BankCard) {
                    if (channelCode.equals(((BankCard) payChannel).getInstId())) {

                        if (payChannel.getIsDisabled() != null
                                && TheiaConstant.ExtraConstants.TRUE.equals(payChannel.getIsDisabled().getStatus())
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannel.getIsDisabled().getMsg())) {
                            continue;
                        }

                        if (BooleanUtils.isTrue(((BankCard) payChannel).getOneClickSupported()))
                            isOneClickSupported.setValue(true);

                        if (BooleanUtils.isTrue(((BankCard) payChannel).isPrepaidCardSupported()))
                            prepaidCardSupported.setValue(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String getErrorMessage(String txnType, String binPayMethod, String cardType, String payMode,
            PayOption merchantPayOption) {
        try {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("cardName", StringUtils.capitalize(StringUtils.lowerCase(cardType)));
            valuesMap.put("payMode", payMode);
            if (txnType != null) {

                if (binPayMethod != null) {

                    switch (binPayMethod) {

                    case "CREDIT_CARD":
                    case "CC":
                    case "Credit Card": {
                        valuesMap.put("cardType", CREDIT);
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);

                        if (isPayMethodEnabled(merchantPayOption, binPayMethod, payMode)) {

                            if (StringUtils.isNotBlank(cardType)) {
                                return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE);
                            } else {
                                return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
                            }
                        } else {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_ONLY_CARD_TYPE);
                        }

                    }
                    case "DEBIT_CARD":
                    case "DC":
                    case "Debit Card": {
                        valuesMap.put("cardType", DEBIT);
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);

                        if (isPayMethodEnabled(merchantPayOption, binPayMethod, payMode)) {

                            if (StringUtils.isNotBlank(cardType)) {
                                return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE);
                            } else {
                                return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
                            }
                        } else {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_ONLY_CARD_TYPE);
                        }
                    }

                    default:
                        return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();

                    }

                } else {

                    return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();

                }
            }
        } catch (Exception e) {
        }
        return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
    }

    private boolean isDisabledByContract(String binPayMethod, String channelCode, PayOption merchantPayOption,
            NativeBinDetailRequestBody body, String bankCode, boolean addNPay, String txnAmount,
            boolean prepaidFeatureDisabled, MutableBoolean isBankDisabled, BinDetailResponseBody binDetailResponseBody,
            MutableBoolean isChannelDisabled) {
        boolean isDisabled = true;
        String payMode = body.getPaymentMode();
        if (TheiaConstant.Bank.BBK.equalsIgnoreCase(bankCode)) {
            return false;
        }
        if ((null != merchantPayOption && null != binPayMethod && null != channelCode)) {
            List<PayMethod> payMethods = merchantPayOption.getPayMethods();
            if (null != payMethods && !payMethods.isEmpty()) {
                // for backward compatibility
                if (payMode == null) {
                    for (PayMethod payMethod : payMethods) {
                        if (binPayMethod.equals(payMethod.getPayMethod())) {
                            List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
                            isDisabled = isPayOptionDisable(channelCode, payChannelOptions, addNPay, txnAmount,
                                    prepaidFeatureDisabled, binDetailResponseBody, isChannelDisabled);

                            if (!isDisabled) {
                                List<String> enabledBanks = payMethod.getEnabledBanks();
                                isDisabled = isBankDisabled(bankCode, enabledBanks);
                                if (BooleanUtils.isTrue(isDisabled)) {
                                    isBankDisabled.setValue(true);
                                }
                            }

                            break;
                        }
                    }
                    // check if emi is configured
                    if (isDisabled) {
                        for (PayMethod payMethod : payMethods) {
                            if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                                isDisabled = payOptionDisableForEMI(bankCode, channelCode, payMethod, body, false,
                                        txnAmount, prepaidFeatureDisabled, binDetailResponseBody, isChannelDisabled);
                                break;
                            }
                        }
                    }
                } else if (EPayMethod.EMI.getMethod().equalsIgnoreCase(payMode)) {
                    for (PayMethod payMethod : payMethods) {
                        if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                            isDisabled = payOptionDisableForEMI(bankCode, channelCode, payMethod, body, false,
                                    txnAmount, prepaidFeatureDisabled, binDetailResponseBody, isChannelDisabled);
                            break;
                        }
                    }
                } else {
                    for (PayMethod payMethod : payMethods) {
                        if (binPayMethod.equals(payMethod.getPayMethod())) {
                            List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
                            isDisabled = isPayOptionDisable(channelCode, payChannelOptions, false, txnAmount,
                                    prepaidFeatureDisabled, binDetailResponseBody, isChannelDisabled);

                            if (!isDisabled) {
                                List<String> enabledBanks = payMethod.getEnabledBanks();
                                isDisabled = isBankDisabled(bankCode, enabledBanks);
                                if (BooleanUtils.isTrue(isDisabled)) {
                                    isBankDisabled.setValue(true);
                                }
                            }
                            break;
                        }
                    }
                }

            }
            return isDisabled;
        }
        LOGGER.error(
                "Payment not allowed on bin due to empty details merchantPayOption: {} , binPayMethod: {} , channelCode: {}",
                merchantPayOption, binPayMethod, channelCode);
        return false;
    }

    private static boolean isPayMethodEnabled(PayOption merchantPayOption, String binPayMethod, String payMode) {

        if (null != merchantPayOption && null != binPayMethod) {

            List<PayMethod> payMethods = merchantPayOption.getPayMethods();

            if (null != payMethods && !payMethods.isEmpty()) {
                // for backward compatibility
                if (payMode == null || EPayMethod.EMI.getMethod().equalsIgnoreCase(payMode) == false) {

                    for (PayMethod payMethod : payMethods) {

                        if (binPayMethod.equals(payMethod.getPayMethod())) {
                            return true;
                        }
                    }

                } else if (EPayMethod.EMI.getMethod().equalsIgnoreCase(payMode)) {

                    for (PayMethod payMethod : payMethods) {
                        return true;
                    }
                }
                return false;
            }
        }
        LOGGER.error("paymethod not allowed in merchantPayOption: {} , binPayMethod: {} , paymode: {}",
                merchantPayOption, binPayMethod, payMode);
        return false;
    }

    private boolean isPayOptionDisable(String channelCode, List<PayChannelBase> payChannelOptions, boolean addNPay,
            String txnAmount, boolean prepaidFeatureDisabled, BinDetailResponseBody binDetailResponseBody,
            MutableBoolean isChannelDisabled) {
        if (CollectionUtils.isNotEmpty(payChannelOptions)) {
            for (PayChannelBase payChannel : payChannelOptions) {
                if (payChannel instanceof BankCard && channelCode.equals(((BankCard) payChannel).getInstId())) {
                    if (TheiaConstant.ExtraConstants.FALSE.equals(payChannel.getIsDisabled().getStatus())
                            && isPrepaidCardPayOptionEnabled((BankCard) payChannel, addNPay, txnAmount,
                                    prepaidFeatureDisabled)
                            && isPayOptionSupported((BankCard) payChannel, binDetailResponseBody)) {
                        binDetailResponseBody.setPreAuthDetails(payChannel.getPreAuthDetails());
                        return false;
                    } else if (TheiaConstant.ExtraConstants.TRUE.equals(payChannel.getIsDisabled().getStatus())
                            && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                    payChannel.getIsDisabled().getMsg())) {
                        isChannelDisabled.setValue(true);
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private boolean isPayOptionSupported(PayChannelBase payChannel, BinDetailResponseBody binDetailResponseBody) {
        boolean validPayOption = true;
        /*
         * 1.what to do if lists are empty. 2.if strings are different , how to
         * handle this case.
         */

        if (CollectionUtils.isNotEmpty(binDetailResponseBody.getSupportedCardSubTypes())) {
            // bin support some subCardType
            // if paychnnel list is empty return false
            if (CollectionUtils.isEmpty((payChannel.getSupportedCardSubTypes()))) {
                validPayOption = false;
                binDetailResponseBody.setCorporateCardFail(true);
            } else {
                // if not empty then binDetails supportedCardSubType should be
                // subset of Channel allowed cardSubType List
                if (!payChannel.getSupportedCardSubTypes()
                        .containsAll(binDetailResponseBody.getSupportedCardSubTypes())) {
                    validPayOption = false;
                    binDetailResponseBody.setCorporateCardFail(true);
                }
            }
        }

        return validPayOption;
    }

    private boolean payOptionDisableForEMI(String bankCode, String channelCode, PayMethod payMethod,
            NativeBinDetailRequestBody body, boolean addNPay, String txnAmount, boolean prepaidFeatureDisabled,
            BinDetailResponseBody binDetailResponseBody, MutableBoolean isChannelDisabled) {
        String emiType = body.getEmiType();
        boolean isDisable = true;
        // EnabledBanks in case of EMI
        List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
        // EnabledCardsheme for EMI
        List<String> enabledPayChannels = payMethod.getEnabledPayChannels();
        if (CollectionUtils.isNotEmpty(payChannelOptions)) {
            for (PayChannelBase payChannel : payChannelOptions) {
                if (payChannel instanceof EmiChannel) {
                    EmiChannel emiChannel = (EmiChannel) payChannel;
                    if (bankCode.equals(emiChannel.getInstId())) {
                        if (TheiaConstant.ExtraConstants.FALSE.equals(payChannel.getIsDisabled().getStatus())
                                && (StringUtils.isNotBlank(emiType) ? emiChannel.getEmiType().getType().equals(emiType)
                                        : true && isPrepaidCardPayOptionEnabled(payChannel, addNPay, txnAmount,
                                                prepaidFeatureDisabled))
                                && isPayOptionSupported((EmiChannel) payChannel, binDetailResponseBody)) {
                            isDisable = false;
                        } else if (TheiaConstant.ExtraConstants.TRUE.equals(payChannel.getIsDisabled().getStatus())
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannel.getIsDisabled().getMsg())) {
                            isChannelDisabled.setValue(true);
                            isDisable = true;
                        }
                    }
                }
            }
        }
        if (!isDisable && CollectionUtils.isNotEmpty(enabledPayChannels)) {
            for (String channel : enabledPayChannels) {
                if (channelCode.equals(channel)) {
                    return isDisable;
                }
            }
            isDisable = true;
        }
        return isDisable;
    }

    @Override
    protected NativeBinDetailResponse postProcess(NativeBinDetailRequest request, BinDetailRequest serviceReq,
            BinDetailResponse serviceRes) throws Exception {
        return transformResponse(request, serviceRes, serviceReq);
    }

    private void validate(NativeBinDetailRequest request) {
        validateBin(request);
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        setSubscriptionDetailsFromOrderToRequest(orderDetail, request);
        if (Objects.isNull(request.getBody().getCardPreAuthType())) {
            request.getBody().setCardPreAuthType(orderDetail.getCardPreAuthType());
        }
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
    }

    private void setSubscriptionDetailsFromOrderToRequest(InitiateTransactionRequestBody orderDetail,
            NativeBinDetailRequest request) {
        if (request != null && orderDetail != null && request.getBody() != null
                && orderDetail instanceof SubscriptionTransactionRequestBody) {
            SubscriptionTransactionRequestBody requestBody = (SubscriptionTransactionRequestBody) orderDetail;

            // hack to support UNKNOWN paymdoe in subscription
            if (SubsPaymentMode.UNKNOWN.name().equalsIgnoreCase(requestBody.getSubscriptionPaymentMode())
                    && !"ADDANDPAY".equalsIgnoreCase(request.getBody().getTxnType())) {
                request.getBody().setPaymentMode(SubsPaymentMode.NORMAL.name());
            } else {
                request.getBody().setPaymentMode(requestBody.getSubscriptionPaymentMode());
            }
            request.getBody().setRequestType(ERequestType.getByRequestType(orderDetail.getRequestType()));
        }
    }

    private void validateBin(NativeBinDetailRequest request) {
        if (null == request.getBody() || null == request.getBody().getBin() || request.getBody().getBin().length() < 6) {
            throw BinDetailException.getException(ResultCode.BIN_NUMBER_EXCEPTION);
        }
    }

    private BinDetailRequest transformRequest(NativeBinDetailRequest request) {
        BinDetailRequestBody body = new BinDetailRequestBody();
        body.setBin(request.getBody().getBin());
        body.setChannelId(request.getHead().getChannelId());
        body.setChannelCode(request.getBody().getChannelCode());
        body.setEmiType(request.getBody().getEmiType());
        body.setPayMode(request.getBody().getPaymentMode());
        body.setRequestType(request.getBody().getRequestType());
        body.setMid(StringUtils.isNotEmpty(request.getBody().getMid()) ? request.getBody().getMid() : MDC
                .get(TheiaConstant.RequestParams.MID));
        return new BinDetailRequest(null, body);
    }

    private NativeBinDetailResponse transformResponse(NativeBinDetailRequest request, BinDetailResponse serviceRes,
            BinDetailRequest serviceReq) {
        NativeBinDetailResponseBody body = new NativeBinDetailResponseBody(serviceRes.getBody());
        body.setPcfFeeCharges(serviceRes.getBody().getPcfFeeCharges());
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.PrepaidCard.FF4J_PREPAID_CARD_STRING, new HashMap<>(), false)) {
            body.setPrepaidCard(BooleanUtils.isTrue(serviceRes.getBody().getPrepaidCard()));
            body.setPrepaidCardMaxAmount(serviceRes.getBody().getPrepaidCardMaxAmount());
        }

        if (StringUtils.isNotBlank(serviceRes.getBody().getRemainingLimit())) {
            body.setRemainingLimit(serviceRes.getBody().getRemainingLimit());
        }

        if (CollectionUtils.isNotEmpty(serviceRes.getBody().getSupportedCardSubTypes())) {
            body.setSupportedCardSubTypes(serviceRes.getBody().getSupportedCardSubTypes());
        }

        if (StringUtils.isNotBlank(serviceReq.getBody().getPayMode())
                && EPayMethod.EMI.getMethod().equals(serviceReq.getBody().getPayMode())
                && !validateBinForEMI(body, serviceReq.getBody(), request)) {
            return new NativeBinDetailResponse(new ResponseHeader(), body);
        }

        if (StringUtils.isNotBlank(serviceReq.getBody().getPayMode())
                && EPayMethod.EMI.getMethod().equals(serviceReq.getBody().getPayMode())
                && !checkValidEMIBinsFromMapping(body, serviceReq.getBody(), request)) {
            return new NativeBinDetailResponse(new ResponseHeader(), body);
        }

        if (request.getBody().getRequestType() != null
                && ERequestType.isSubscriptionCreationRequest(request.getBody().getRequestType().getType())
                && serviceRes.getBody().getIsSubscriptionAvailable() != null
                && !serviceRes.getBody().getIsSubscriptionAvailable()) {
            return new NativeBinDetailResponse(new ResponseHeader(), body);
        }

        if (!(request != null && request.getBody() != null && request.getBody().getRequestType() != null && ERequestType
                .isSubscriptionCreationRequest(request.getBody().getRequestType().getType())))
            setEMIParamsToResponse(request, body);
        List<String> authModes = new ArrayList<>();
        authModes.add(AuthMode.OTP.getType());
        // commenting as no direct channels for new card need to be supported as
        // of now
        /*
         * if (EPayMethod.DEBIT_CARD.getMethod().equals(body.getBinDetail().
         * getPayMethod()) &&
         * isDirectChannelEnabled(request.getHead().getTxnToken(),
         * body.getBinDetail().getIssuingBankCode(), body.getBinDetail()) &&
         * !EPayMethod.EMI.getMethod().equals(serviceReq.getBody().getPayMode())
         * &&
         * !StringUtils.equalsIgnoreCase(ConfigurationUtil.getProperty("scw.mid"
         * , "scwpay09224240900570"), MDC.get(TheiaConstant.RequestParams.MID)))
         * { authModes.add(AuthMode.PIN.getType()); }
         */
        body.setAuthModes(authModes);
        body.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(body.getBinDetail().getPayMethod(),
                body.getBinDetail().getChannelCode())
                || hybridDisablingUtil.isHybridDisabledForBank(body.getBinDetail().getIssuingBankCode()));
        body.setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        body.setPreAuthDetails(serviceRes.getBody().getPreAuthDetails());
        return new NativeBinDetailResponse(new ResponseHeader(), body);
    }

    private void setEMIParamsToResponse(NativeBinDetailRequest request, NativeBinDetailResponseBody body) {
        EmiType type = EPayMethod.DEBIT_CARD.getMethod().equals(body.getBinDetail().getPayMethod()) ? EmiType.DEBIT_CARD
                : EmiType.CREDIT_CARD;
        EmiChannel emiChannel = getEmiChannel(request, body.getBinDetail().getIssuingBankCode(), type);
        boolean isEMIDetailFlag = true;
        if (null != emiChannel) {
            body.setIsEmiAvailable(true);
        }
        if (request.getBody().isEMIDetail() != null && !Boolean.valueOf(request.getBody().isEMIDetail())) {
            isEMIDetailFlag = false;
        }

        if (isEMIDetailFlag) {
            body.setEmiChannel(emiChannel);
        }
        if (!body.getIsEmiAvailable()) {
            body.setErrorMessage("EMI not available");
        }
    }

    private boolean isDirectChannelEnabled(String token, String bankCode, BinData binData) {
        EntityPaymentOptionsTO entityPaymentoptions = (EntityPaymentOptionsTO) nativeSessionUtil
                .getEntityPaymentOptions(token);
        if (null == entityPaymentoptions) {
            LOGGER.error("Unable to populate direct channel for bin as token data does not exist : {}",
                    binData.getBin());
            return false;
        }
        return theiaSessionDataService.isDirectChannelEnabled(binData.getIssuingBankCode(), binData.getPayMethod(),
                entityPaymentoptions.getDirectServiceInsts(), false, entityPaymentoptions.getSupportAtmPins());
    }

    public EmiChannel getEmiChannel(NativeBinDetailRequest request, String bankCode, EmiType type) {
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead()
                .getTxnToken());
        com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod emiPayMethod = cashierInfoResponse.getBody()
                .getMerchantPayOption().getPayMethods().stream()
                .filter(s -> EPayMethod.EMI.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
        if (null != emiPayMethod && null != emiPayMethod.getPayChannelOptions()) {
            EmiChannel emiChannel = emiPayMethod.getPayChannelOptions().stream().map(s -> (EmiChannel) s)
                    .filter(s -> s.getInstId().equals(bankCode) && type.equals(s.getEmiType())).findAny().orElse(null);
            return emiChannel;
        }
        return null;
    }

    private boolean validateBinForEMI(NativeBinDetailResponseBody responseBody, BinDetailRequestBody serviceReq,
            NativeBinDetailRequest request) {
        // to handle case where emiType and channelCode are not available for JS
        // Checkout
        if (CHECKOUT.equals(request.getHead().getWorkFlow())) {
            String emiType = serviceReq.getEmiType();
            String channelCode = serviceReq.getChannelCode();
            if (emiType != null) {
                if (!responseBody.getBinDetail().getPayMethod().equalsIgnoreCase(serviceReq.getEmiType())) {
                    responseBody.setIsEmiAvailable(false);
                    responseBody.setErrorMessage("Please enter "
                            + EPayMethod.getPayMethodByMethod(serviceReq.getEmiType()).getDisplayName());
                    return false;
                }
            }
            if (channelCode != null) {
                if (!responseBody.getBinDetail().getIssuingBankCode().equalsIgnoreCase(serviceReq.getChannelCode())) {
                    responseBody.setIsEmiAvailable(false);
                    responseBody.setErrorMessage("Please enter " + serviceReq.getChannelCode() + " card");
                    return false;
                }
            }
        } else {
            // case when user enters card type from different from the type of
            // Emi.
            if (!responseBody.getBinDetail().getPayMethod().equalsIgnoreCase(serviceReq.getEmiType())) {
                responseBody.setIsEmiAvailable(false);
                responseBody.setErrorMessage("Please enter "
                        + EPayMethod.getPayMethodByMethod(serviceReq.getEmiType()).getDisplayName());
                return false;
            }

            // case when user enters bank card different from the choosen bank
            // emi
            if (!responseBody.getBinDetail().getIssuingBankCode().equalsIgnoreCase(serviceReq.getChannelCode())) {
                responseBody.setIsEmiAvailable(false);
                responseBody.setErrorMessage("Please enter " + serviceReq.getChannelCode() + " card");
                return false;
            }
        }
        return true;
    }

    private void validateWithSsoToken(NativeBinDetailRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        validateBin(request);
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));

    }

    private void validateWithPaymentContext(NativeBinDetailRequest request) {
        request.getHead().setTxnToken(
                nativeSessionUtil.getCacheKeyForSuperGw(request.getBody().getMid(), request.getBody().getReferenceId(),
                        request.getBody().getRequestType().getType()));

    }

    private void validateWithGuestToken(NativeBinDetailRequest request) {
        validateBin(request);

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if (StringUtils.equals(servletRequest.getRequestURI(), NATIV_FETCH_BIN_DETAIL_URL)) {
            nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
            String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
            nativeValidationService.validateMid(mid);
        }
        request.getHead().setTxnToken(getToken(request));
    }

    private String getToken(NativeBinDetailRequest request) {
        if (StringUtils.isNotBlank(request.getHead().getToken())) {
            return request.getHead().getToken();
        } else {
            return nativeSessionUtil.createTokenOnMidForGuest(request.getBody().getMid());
        }
    }

    private boolean isBankDisabled(String bankCode, List<String> enabledBanks) {
        if (CollectionUtils.isEmpty(enabledBanks)) {
            return false;
        }

        for (String bank : enabledBanks) {
            if (bankCode.equals(bank)) {
                return false;
            }

        }

        return true;
    }

    private Map<EPayMethod, NativeConsultDetails> consultFeeForGivenPaymodeWithSsoOrAccessToken(
            NativeBinDetailRequest request, String paymethod, String channelCode, FeeRateFactors feeRateFactors)
            throws Exception {

        FetchPcfDetailRequest fetchPcfDetailRequest = new FetchPcfDetailRequest();
        FetchPcfDetailRequestBody body = new FetchPcfDetailRequestBody();
        TokenRequestHeader head = new TokenRequestHeader();
        if (TokenType.SSO.equals(request.getHead().getTokenType())
                || TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            head.setTokenType(request.getHead().getTokenType());
            head.setToken(request.getHead().getToken());
            body.setTxnAmount(request.getBody().getTxnAmount());
            body.setMid(request.getBody().getMid());
            body.setReferenceId(request.getBody().getReferenceId());
        } else {
            head.setTxnToken(request.getHead().getTxnToken());
        }
        List<PayChannelOptionView> payMethods = new ArrayList<PayChannelOptionView>();
        FetchPcfDetailResponse fetchPcfDetailResponse = null;
        com.paytm.pgplus.facade.enums.PayMethod paymentMethod;
        if (EPayMethod.PPBL.getMethod().equals(paymethod)) {
            paymentMethod = com.paytm.pgplus.facade.enums.PayMethod.PPBL;
        } else {
            paymentMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(paymethod);
        }
        PayChannelOptionView payChannelOptionView = new PayChannelOptionView(paymentMethod.getOldName(), paymentMethod,
                true);
        payChannelOptionView.setInstId(channelCode);
        payChannelOptionView.setFeeRateFactors(feeRateFactors);
        payMethods.add(payChannelOptionView);
        body.setPayMethods(payMethods);
        body.setBin(request.getBody().getBin());
        fetchPcfDetailRequest.setHead(head);
        fetchPcfDetailRequest.setBody(body);
        fetchPcfDetailResponse = nativeFetchPcfDetailsRequestProcessor.process(fetchPcfDetailRequest);
        return fetchPcfDetailResponse.getBody().getConsultDetails();
    }

    private void validateWithCheckSum(NativeBinDetailRequest request) {

        String validateChecksum = ConfigurationUtil.getProperty("VALIDATE_CHECKSUM_VSC_TP", "Y");
        if ("Y".equals(validateChecksum)) {
            tokenValidationHelper.validateChecksum(request.getHead().getToken(), request.getBody(), request.getBody()
                    .getMid());
        }
        validateBin(request);
        request.getHead().setTxnToken(nativeSessionUtil.createTokenForGuest(request.getBody().getMid()));

        LOGGER.info("Changing request tokenType from CHECKSUM to GUEST for fetchPayOptions");
        request.getHead().setTokenType(TokenType.GUEST);

        if (ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(),
                TheiaConstant.FF4J.ENABLE_CACHING_FOR_FPO_IN_FETCH_BIN_DETAIL, false)) {
            LOGGER.info("Creating Guest Token on mid : {} ", request.getBody().getMid());
            String token = nativeSessionUtil.createGuestTokenOnMidForFetchBinDetailApi(request.getBody().getMid());
            request.getHead().setTxnToken(token);
            request.getHead().setToken(token);
        }

    }

    private void validateWithAccessToken(NativeBinDetailRequest request) {

        validateBin(request);

        String referenceId = request.getBody().getReferenceId();
        String mid = request.getBody().getMid();
        request.getHead().setTxnToken(request.getHead().getToken());
        request.getHead().setTokenType(TokenType.ACCESS);
        CreateAccessTokenServiceRequest createAccessTokenServiceRequest = accessTokenUtils.validateAccessToken(mid,
                referenceId, request.getHead().getToken());
        if (Objects.isNull(request.getBody().getCardPreAuthType())) {
            request.getBody().setCardPreAuthType(createAccessTokenServiceRequest.getPreAuthType());
        }

    }

    boolean isPrepaidCardPayOptionEnabled(PayChannelBase payChannelBase, boolean addNPay, String txnAmount,
            boolean prepaidFeatureDisabled) {

        if (prepaidFeatureDisabled) {
            return true;
        }

        /*
         * if prepaid is enabled on merchant and card is prepaid then block EMI
         * ,addNPay,if txnAmount > maxPrepaidTxnAmount
         */

        if (addNPay || EPayMethod.EMI.getMethod().equals(payChannelBase.getPayMethod())
                || !(payChannelBase instanceof BankCard)) {
            return false;
        }

        boolean validSavedPrepaidCard = false;
        if (BooleanUtils.isTrue(((BankCard) payChannelBase).isPrepaidCardSupported())
                && prepaidCardValidationUtil.isPrepaidCardLimitValid(txnAmount, false)) {
            validSavedPrepaidCard = true;
        }
        return validSavedPrepaidCard;

    }

    private boolean checkValidEMIBinsFromMapping(NativeBinDetailResponseBody responseBody,
            BinDetailRequestBody serviceReq, NativeBinDetailRequest request) {
        String planID;
        String isOfus;

        if (responseBody.getBinDetail() == null || responseBody.getBinDetail().getBin() == null) {
            responseBody.setIsEmiAvailable(false);
            responseBody.setErrorMessage("Card Not eligible for EMI");
            LOGGER.error("No BinDetail found");
            return false;
        }
        String binNumber = String.valueOf(responseBody.getBinDetail().getBin());
        EmiType type = EPayMethod.DEBIT_CARD.getMethod().equals(responseBody.getBinDetail().getPayMethod()) ? EmiType.DEBIT_CARD
                : EmiType.CREDIT_CARD;
        EmiChannel emiChannel = getEmiChannel(request, responseBody.getBinDetail().getIssuingBankCode(), type);

        if (null == emiChannel) {
            responseBody.setIsEmiAvailable(false);
            responseBody.setErrorMessage("Card Not eligible for EMI");
            LOGGER.error("emiChannel is null");
            return false;
        } else {
            for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {

                if (StringUtils.isNotBlank(emiChannelInfo.getPlanId())
                        && StringUtils.isNotBlank(emiChannelInfo.getTenureId())) {
                    planID = emiChannelInfo.getPlanId();
                    isOfus = "0";
                } else {
                    BankInfoData bankInfoData = null;
                    try {
                        bankInfoData = merchantBankInfoDataService.getBankInfo(emiChannel.getInstId());
                    } catch (Exception e) {
                        LOGGER.error("Error while fetching BankInfo for InstId :{}", emiChannel.getInstId(), e);
                    }
                    if (bankInfoData == null) {
                        responseBody.setIsEmiAvailable(false);
                        responseBody.setErrorMessage("Card Not eligible for EMI");
                        LOGGER.error("No bankInfoData found");
                        return false;
                    }
                    planID = String.valueOf(bankInfoData.getBankId());
                    isOfus = "1";

                    if (EmiBinValidationUtil.CardAcquiringMode.ONUS.toString().equals(
                            emiChannelInfo.getCardAcquiringMode().getCardAcquiringMode())) {
                        isOfus = "2";
                    }

                }
                StringBuilder sb = new StringBuilder();
                sb.append(planID).append("|").append(isOfus);

                try {
                    if (EmiType.CREDIT_CARD.equals(emiChannel.getEmiType())) {
                        EMIValidBinsData validBins = configurationDataService.getEmiValidBins(sb.toString());
                        if (null != validBins && !validBins.getValidBins().isEmpty()) {
                            if (!validBins.getValidBins().contains(binNumber)) {
                                LOGGER.error("Bin {} is not found in  Valid bins", binNumber);
                                responseBody.setIsEmiAvailable(false);
                                responseBody.setErrorMessage("Card Not eligible for EMI");
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while fetching EMI Valid Bins for BIN: {}", binNumber, e);
                }
            }
        }
        return true;
    }
}