package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.CardSchemeInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponse;
import com.paytm.pgplus.theia.nativ.service.ICardValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
import com.paytm.pgplus.theia.services.impl.CardValidationHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "cardValidationService")
public class CardValidationServiceImpl implements ICardValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardValidationServiceImpl.class);

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITH_ONLY_CARD_TYPE = "${cardType} card is not allowed for this payment. Please try paying using other cards/options.";

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE = "${cardName} ${cardType} card is not allowed for this payment. Please try paying using other cards/options.";
    private static String CREDIT = "Credit";
    private static String DEBIT = "Debit";

    private static String PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE = "${cardName} ${cardType} card is not allowed for ${payMode} payment. Please try paying using other cards/options.";
    @Autowired
    private CardValidationHelper cardValidationHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("binDetailService")
    private BinDetailService binDetailService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    public ValidateCardResponse validateCardandFetchDetails(ValidateCardRequest validateCardRequest)
            throws FacadeCheckedException {
        NativeBinDetailRequest request = new NativeBinDetailRequest();
        NativeBinDetailRequestBody nativeBinDetailRequestBody = new NativeBinDetailRequestBody();
        nativeBinDetailRequestBody.setBin(validateCardRequest.getBody().getBin());
        request.setBody(nativeBinDetailRequestBody);
        BinDetailResponse binDetailResponse = fetchBinDetails(validateCardRequest);
        try {
            if (null != binDetailResponse && null != binDetailResponse.getBody()
                    && null != binDetailResponse.getBody().getBinDetail()) {
                String binPayMethod = binDetailResponse.getBody().getBinDetail().getPayMethod();
                String bankCode = binDetailResponse.getBody().getBinDetail().getIssuingBankCode();
                binDetailResponse.getBody().getBinDetail()
                        .setChannelCode(binDetailResponse.getBody().getBinDetail().getChannelName());
                String channelCode = binDetailResponse.getBody().getBinDetail().getChannelCode();

                CardSchemeInfo cardSchemeInfo = cardUtils.getCardSchemeInfo(channelCode);
                if (CashierConstant.BAJAJFN.equals(channelCode) || CashierConstant.BAJAJ_CARD.equals(channelCode)) {
                    cardSchemeInfo.setIsCVVRequired("false");
                    cardSchemeInfo.setIsExpiryRequired("false");
                }

                if (cardSchemeInfo != null) {
                    binDetailResponse.getBody().getBinDetail().setMinCardNum(cardSchemeInfo.getMinCardNumberLength());
                    binDetailResponse.getBody().getBinDetail().setMaxCardNum(cardSchemeInfo.getMaxCardNumberLength());
                    binDetailResponse.getBody().getBinDetail().setIsCVVRequired(cardSchemeInfo.getIsCVVRequired());
                    binDetailResponse.getBody().getBinDetail().setCvvLength(cardSchemeInfo.getCvvLength());
                    binDetailResponse.getBody().getBinDetail()
                            .setIsExpiryRequired(cardSchemeInfo.getIsExpiryRequired());
                }

                NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil
                        .getCashierInfoResponse(validateCardRequest.getHead().getToken());

                if (cashierInfoResponse == null) {
                    nativePaymentUtil.fetchPaymentOptionsForGuest(validateCardRequest.getHead(), validateCardRequest
                            .getBody().getMid(), false, null, null);
                    cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(validateCardRequest.getHead()
                            .getToken());
                }

                if (!(cashierInfoResponse.getBody().isZeroCostEmi() && (StringUtils.equals(binPayMethod,
                        EPayMethod.CREDIT_CARD.getMethod()) || StringUtils.equals(binPayMethod,
                        EPayMethod.DEBIT_CARD.getMethod())))) {

                    PayOption merchantPayOption = cashierInfoResponse.getBody().getMerchantPayOption();

                    if (isDisabledByContract(binPayMethod, channelCode, merchantPayOption, request.getBody(), bankCode)) {

                        LOGGER.error(
                                "Payment not allowed on bin - isZeroCostEmi: {} binPayMethod: {} channelCode: {} bin: {}, bank: {} ,merchantPayOption: {} , paymentMode: {}",
                                cashierInfoResponse.getBody().isZeroCostEmi(), binPayMethod, channelCode,
                                binDetailResponse.getBody().getBinDetail().getBin(), binDetailResponse.getBody()
                                        .getBinDetail().getIssuingBankCode(), merchantPayOption, binDetailResponse
                                        .getBody().getBinDetail().getPayMethod());

                        BinDetailException exception = BinDetailException
                                .getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN);

                        exception.getResultInfo().setResultMsg(
                                getErrorMessage(request.getBody().getTxnType(), binPayMethod, channelCode, request
                                        .getBody().getPaymentMode()));
                        throw exception;
                    }
                }

            }

        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }
        return cardValidationHelper.prepareValidateCardResponse(binDetailResponse);
    }

    private boolean isDisabledByContract(String binPayMethod, String channelCode, PayOption merchantPayOption,
            NativeBinDetailRequestBody body, String bankCode) {
        boolean isDisabled = true;
        String payMode = null;
        if (TheiaConstant.Bank.BBK.equalsIgnoreCase(bankCode)) {
            return false;
        }
        if ((null != merchantPayOption && null != binPayMethod && null != channelCode)) {
            List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods = merchantPayOption
                    .getPayMethods();
            if (null != payMethods && !payMethods.isEmpty()) {
                // for backward compatibility
                if (payMode == null) {
                    for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : payMethods) {
                        if (binPayMethod.equals(payMethod.getPayMethod())) {
                            List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
                            isDisabled = isPayOptionDisable(channelCode, payChannelOptions);

                            if (!isDisabled) {
                                List<String> enabledBanks = payMethod.getEnabledBanks();
                                isDisabled = isBankDisabled(bankCode, enabledBanks);
                            }

                            break;
                        }
                    }
                    // check if emi is configured
                    if (isDisabled) {
                        for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : payMethods) {
                            if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                                isDisabled = payOptionDisableForEMI(bankCode, channelCode, payMethod, body);
                                break;
                            }
                        }
                    }
                } else if (EPayMethod.EMI.getMethod().equalsIgnoreCase(payMode)) {
                    for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : payMethods) {
                        if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                            isDisabled = payOptionDisableForEMI(bankCode, channelCode, payMethod, body);
                            break;
                        }
                    }
                } else {
                    for (PayMethod payMethod : payMethods) {
                        if (binPayMethod.equals(payMethod.getPayMethod())) {
                            List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();
                            isDisabled = isPayOptionDisable(channelCode, payChannelOptions);

                            if (!isDisabled) {
                                List<String> enabledBanks = payMethod.getEnabledBanks();
                                isDisabled = isBankDisabled(bankCode, enabledBanks);
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

    private static String getErrorMessage(String txnType, String binPayMethod, String cardType, String payMode) {
        try {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("cardName", cardType);
            valuesMap.put("payMode", payMode);
            if (txnType != null) {

                if (binPayMethod != null) {

                    switch (binPayMethod) {

                    case "CREDIT_CARD":
                    case "CC":
                    case "Credit Card": {
                        valuesMap.put("cardType", CREDIT);
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
                        if (StringUtils.isNotBlank(payMode) && StringUtils.isNotBlank(cardType)
                                && !payMode.equals(SubsPaymentMode.UNKNOWN.name())) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE);
                        } else if (StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE);
                        } else {
                            return ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg();
                        }
                    }
                    case "DEBIT_CARD":
                    case "DC":
                    case "Debit Card": {
                        valuesMap.put("cardType", DEBIT);
                        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
                        if (StringUtils.isNotBlank(payMode) && StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITH_PAYMODE);
                        } else if (StringUtils.isNotBlank(cardType)) {
                            return strSubstitutor.replace(PAYMENT_NOT_ALLOWED_MESSAGE_WITHOUT_PAYMODE);
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

    private boolean isPayOptionDisable(String channelCode, List<PayChannelBase> payChannelOptions) {
        if (CollectionUtils.isNotEmpty(payChannelOptions)) {
            for (PayChannelBase payChannel : payChannelOptions) {
                if (payChannel instanceof BankCard) {
                    if (channelCode.equals(((BankCard) payChannel).getInstId())
                            && TheiaConstant.ExtraConstants.FALSE.equals(payChannel.getIsDisabled().getStatus())) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    private boolean payOptionDisableForEMI(String bankCode, String channelCode, PayMethod payMethod,
            NativeBinDetailRequestBody body) {
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
                    if (bankCode.equals(emiChannel.getInstId())
                            && TheiaConstant.ExtraConstants.FALSE.equals(payChannel.getIsDisabled().getStatus())
                            && (StringUtils.isNotBlank(emiType) ? emiChannel.getEmiType().getType().equals(emiType)
                                    : true)) {
                        isDisable = false;
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

    public BinDetailResponse fetchBinDetails(ValidateCardRequest validateCardRequest) {
        BinDetailRequest request = cardValidationHelper.prepareBinDetailsRequest(
                validateCardRequest.getBody().getBin(), validateCardRequest.getHead().getChannelId());
        BinDetailResponse binDetailResponse = binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(request);
        List<String> authModes = new ArrayList<>();
        authModes.add(AuthMode.OTP.getType());
        binDetailResponse.getBody().setAuthModes(authModes);
        return binDetailResponse;
    }

}
