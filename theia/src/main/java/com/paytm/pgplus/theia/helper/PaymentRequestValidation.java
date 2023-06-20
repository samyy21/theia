/**
 *
 */
package com.paytm.pgplus.theia.helper;

import java.util.*;

import com.paytm.pgplus.theia.enums.TransactionMode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.enums.DirectChannelBank;
import com.paytm.pgplus.biz.enums.DirectPaymentVerificationMethod;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.integration.enums.SupportRegion;
import com.paytm.pgplus.pgproxycommon.enums.CardType;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.mapper.TheiaCashierMapper;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.TheiaCashierUtil;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.INTERNATIONAL_PAYMENT_KEY;

/**
 * @author amitdubey
 */
@Service
public class PaymentRequestValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestValidation.class);
    @Autowired
    @Qualifier("emiBinValidationUtil")
    EmiBinValidationUtil emiBinValidationUtil;

    @Autowired
    TheiaCashierMapper theiaCashierMapper;

    @Autowired
    TheiaCashierUtil theiaCashierUtil;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    public void prepareCardRequest(final TheiaPaymentRequest theiaPaymentRequest,
            final EntityPaymentOptionsTO entityPaymentOptions, CashierWorkflow cashierWorkflow,
            final CashierRequestBuilder cashierRequestBuilder, CardInfo cardInfo, PaymentRequest paymentRequest)
            throws PaytmValidationException {
        CardRequest cardRequest;

        if ((cashierWorkflow == CashierWorkflow.ISOCARD) || (cashierWorkflow == CashierWorkflow.ADD_MONEY_ISOCARD)) {
            BinDetail binDetail;

            try {
                cardRequest = theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest);
            } catch (CashierCheckedException e) {
                throw new TheiaServiceException("SYSTEM_ERROR : UNABLE TO PROCESS THE REQUEST", e);
            }

            if (StringUtils.isBlank(theiaPaymentRequest.getSavedCardId())) {
                binDetail = validateAndBuildBinForBankCard(theiaPaymentRequest, entityPaymentOptions);
            } else {
                /** Saved card request */
                if (cardRequest.getSavedCardRequest() == null) {
                    throw new TheiaServiceException("SYSTEM_ERROR : Unable to process the request");
                }

                binDetail = validateAndBuildBinForSavedCard(theiaPaymentRequest, cardInfo, cardRequest);
            }

            if (binDetail.getIsIndian()) {
                paymentRequest.getPayBillOptions().setIssuingCountry(SupportRegion.IN.name());
            } else {
                validateInternationalCard(entityPaymentOptions, binDetail.getCardName(),
                        theiaPaymentRequest.getTxnMode(), false);
                paymentRequest.getPayBillOptions().setIssuingCountry(SupportRegion.INTL.name());
                cardRequest.setInternationalCard(true);
                // to support international payment
                paymentRequest.getPayBillOptions().getChannelInfo()
                        .put(INTERNATIONAL_PAYMENT_KEY, SupportRegion.INTL.name());
            }

            /** Check for the direct payment option */
            checkForSeamlesstBankCardPaymentGateWay(entityPaymentOptions, cashierRequestBuilder, paymentRequest,
                    binDetail, theiaPaymentRequest);

            try {
                cashierRequestBuilder.setBinCardRequest(new BinCardRequest(binDetail));
                cashierRequestBuilder.setCardRequest(cardRequest);
                cashierRequestBuilder.setPaymentRequest(paymentRequest);
            } catch (CashierCheckedException e) {
                throw new TheiaServiceException("SYSTEM_ERROR : UNABLE TO PROCESS THE REQUEST", e);
            }
        } else if ((cashierWorkflow == CashierWorkflow.IMPS) || (cashierWorkflow == CashierWorkflow.ADD_MONEY_IMPS)) {

            /** Add IMPS Validations Here */
            cardUtils.validateIMPSCardRequest(theiaPaymentRequest.getSavedCardId(), theiaPaymentRequest.getMmid(),
                    theiaPaymentRequest.getOtp(), theiaPaymentRequest.getHolderMobileNo());

            try {
                cardRequest = theiaCashierMapper.preapreImpsCardRequest(theiaPaymentRequest);
                cashierRequestBuilder.setCardRequest(cardRequest);
            } catch (CashierCheckedException e) {
                throw new TheiaServiceException("SYSTEM_ERROR : UNABLE TO PROCESS THE REQUEST", e);
            }
        } else if ((cashierWorkflow == cashierWorkflow.UPI) || (cashierWorkflow == CashierWorkflow.ADD_MONEY_UPI)) {
            try {
                LOGGER.debug("CardInfo : {} ", cardInfo);
                cardRequest = theiaCashierMapper.prepareUPIRequest(theiaPaymentRequest);
                LOGGER.debug("CardRequest : {} ", cardRequest);

                if (cardInfo != null && cardInfo.getMerchantViewSavedCardsList() != null
                        && !cardInfo.getMerchantViewSavedCardsList().isEmpty() && cardRequest != null) {
                    cardRequest.setMerchantViewSavedCardsList(cardInfo.getMerchantViewSavedCardsList());
                }

                cashierRequestBuilder.setCardRequest(cardRequest);
            } catch (CashierCheckedException e) {
                throw new TheiaServiceException("SYSTEM_ERROR : UNABLE TO PROCESS THE REQUEST", e);
            }
        }
    }

    private BinDetail validateAndBuildBinForSavedCard(final TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo,
            final CardRequest cardRequest) throws PaytmValidationException {

        if (StringUtils.isBlank(theiaPaymentRequest.isIciciIDebit()) || theiaPaymentRequest.isIciciIDebit().equals("N")) {
            validateSavedCardCvv(cardRequest.getSavedCardRequest());
        }

        String savedCardId = cardRequest.getSavedCardRequest().getSavedCardId();
        if (StringUtils.isBlank(savedCardId)) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_SAVED_CARD);
        }

        SavedCardInfo savedCardInfo;

        if (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag())) {
            savedCardInfo = cardInfo.getAddAnPaySavedCardMap().get(savedCardId);
        } else {
            savedCardInfo = cardInfo.getSavedCardMap().get(savedCardId);
        }

        if (savedCardInfo == null) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC.getValidationFailedMsg());
        }

        return cardUtils.fetchBinDetails(String.valueOf(savedCardInfo.getFirstSixDigit()));
    }

    private BinDetail validateAndBuildBinForBankCard(final TheiaPaymentRequest theiaPaymentRequest,
            final EntityPaymentOptionsTO entityPaymentOptions) throws PaytmValidationException {

        if (StringUtils.isBlank(theiaPaymentRequest.getCardNo())) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
        }

        if (theiaPaymentRequest.getCardNo().length() > ExtraConstants.MAX_CARD_LENGTH) {
            LOGGER.error("card length should be smaller than 19");
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
        }

        BinDetail binDetail = cardUtils.fetchBinDetails(theiaPaymentRequest.getCardNo().substring(0, 6));

        cardUtils.validateExpiryDate(theiaPaymentRequest.getExpiryMonth(), theiaPaymentRequest.getExpiryYear(),
                binDetail.getCardName(), StringUtils.EMPTY);
        if (StringUtils.isBlank(theiaPaymentRequest.isIciciIDebit()) || theiaPaymentRequest.isIciciIDebit().equals("N")) {
            cardUtils.validateCVV(theiaPaymentRequest.getCvv(), binDetail.getCardName(), StringUtils.EMPTY);
        }

        /*
         * Card Type will be validated/generated here if not present in BIN
         * details
         */
        validateMerchantEnabledCards(theiaPaymentRequest, entityPaymentOptions, binDetail);
        validateEmiBIN(theiaPaymentRequest, entityPaymentOptions, binDetail);
        return binDetail;
    }

    /**
     * @param cashierRequestBuilder
     * @param binDetail
     */
    private void checkForSeamlesstBankCardPaymentGateWay(final EntityPaymentOptionsTO entityPaymentOptions,
            final CashierRequestBuilder cashierRequestBuilder, final PaymentRequest paymentRequest,
            final BinDetail binDetail, final TheiaPaymentRequest theiaPaymentRequest) {
        boolean directBankGatewaySupported = false;
        String verificationMethod = null;
        if (binDetail == null) {
            return;
        }

        HashMap<String, String> directChannelMap = directChannel();
        for (String directChannel : entityPaymentOptions.getDirectServiceInsts()) {
            String[] channelTypes = directChannel.split("@");

            String bankCode = directChannelMap.get(channelTypes[0]);

            if (bankCode != null && bankCode.equalsIgnoreCase(binDetail.getBankCode())
                    && binDetail.getCardType().equals(channelTypes[1])) {

                if ("Y".equals(theiaPaymentRequest.isIciciIDebit())) {
                    verificationMethod = DirectPaymentVerificationMethod.ATM.getValue();
                    directBankGatewaySupported = true;
                } else {
                    verificationMethod = DirectPaymentVerificationMethod.OTP.getValue();
                    directBankGatewaySupported = true;
                }
            }
        }

        LOGGER.debug("directBankGatewaySupported : {} & verificationMethod : {}", directBankGatewaySupported,
                verificationMethod);

        if (directBankGatewaySupported) {
            paymentRequest.getPayBillOptions().getChannelInfo()
                    .put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
            paymentRequest.getPayBillOptions().getChannelInfo()
                    .put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD, verificationMethod);
            cashierRequestBuilder.setDirectBankCardPayRequest(true);
        } else {
            paymentRequest.getPayBillOptions().getChannelInfo()
                    .put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
        }
    }

    /**
     * @param entityPaymentOptions
     * @param cardScheme
     * @param txnMode
     * @return
     * @throws PaytmValidationException
     */
    public void validateInternationalCard(final EntityPaymentOptionsTO entityPaymentOptions, String cardScheme,
            String txnMode, boolean isSavedCard) throws PaytmValidationException {
        List<BankInfo> channelList;
        if (TransactionMode.CC.getMode().equals(txnMode)) {
            channelList = entityPaymentOptions.getCompleteCcList();
        } else {
            channelList = entityPaymentOptions.getCompleteDcList();
        }
        if (CollectionUtils.isNotEmpty(channelList)) {
            BankInfo channelInfo = null;
            for (BankInfo bankInfo : channelList) {
                if (bankInfo.getBankName().equals(cardScheme)) {
                    channelInfo = bankInfo;
                    break;
                }
            }

            if ((channelInfo != null) && !channelInfo.getSupportCountries().contains(SupportRegion.INTL.name())) {
                if (!isSavedCard) {
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_INTERNATIONAL_CARD);
                }
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_INTL_CARD);
            }
        }
    }

    private void validateSavedCardCvv(final SavedCardRequest saveCardRequest) throws PaytmValidationException {

        if (saveCardRequest == null) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_SAVED_CARD);
        }

        cardUtils.validateCVV(saveCardRequest.getCvv(), saveCardRequest.getCardScheme(), StringUtils.EMPTY);
    }

    /**
     * Check which kind of card is enabled on merchant
     **/
    private void validateMerchantEnabledCards(final TheiaPaymentRequest theiaPaymentRequest,
            final EntityPaymentOptionsTO entityPaymentOptions, BinDetail binDetail) throws PaytmValidationException {

        if (binDetail == null) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_BIN_DETAILS);
        }

        String cardType = binDetail.getCardType();
        if (StringUtils.isBlank(cardType)) {
            switch (theiaPaymentRequest.getTxnMode()) {
            case "DC":
                cardType = CardType.DEBIT_CARD.name();
                binDetail.setCardType(cardType);
                break;
            case "CC":
                cardType = CardType.CREDIT_CARD.name();
                binDetail.setCardType(cardType);
                break;
            default:
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_TYPE);
            }
        }

        validateTxnMode(theiaPaymentRequest, entityPaymentOptions, cardType);
    }

    private void validateTxnMode(final TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptions, String cardType) throws PaytmValidationException {
        if (cardType.contains(TheiaConstant.ExtraConstants.DEBIT)
                && !(entityPaymentOptions.isDcEnabled() || (ExtraConstants.ADD_MONEY_FLAG_VALUE
                        .equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptions.isAddDcEnabled()))) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_DC);
        }

        if (cardType.contains(TheiaConstant.ExtraConstants.CREDIT)
                && !(entityPaymentOptions.isCcEnabled() || (ExtraConstants.ADD_MONEY_FLAG_VALUE
                        .equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptions.isAddCcEnabled()))) {

            if (!PayMethod.EMI.getOldName().equals(theiaPaymentRequest.getPaymentMode())) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_CC);
            }
        }

        if (PayMethod.EMI.getOldName().equals(theiaPaymentRequest.getPaymentMode())
                && cardType.contains(TheiaConstant.ExtraConstants.DEBIT)) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_EMI);
        }
    }

    /**
     * Need to validate BIN details in case of EMI payMethod,against
     * EMI_VALID_BINS table
     **/
    private void validateEmiBIN(final TheiaPaymentRequest theiaPaymentRequest,
            final EntityPaymentOptionsTO entityPaymentOptions, BinDetail binDetail) throws PaytmValidationException {
        if (PayMethod.EMI.getOldName().equals(theiaPaymentRequest.getPaymentMode())) {
            final boolean isValidEMICardDetailsEntered = emiBinValidationUtil.isValidEmiCardDetailsEntered(
                    theiaPaymentRequest, entityPaymentOptions, binDetail);

            if (!isValidEMICardDetailsEntered) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_EMI);
            }
        }
    }

    private static HashMap<String, String> directChannel() {

        HashMap<String, String> directChannelMap = new HashMap<String, String>();
        try {
            for (DirectChannelBank directChannel : DirectChannelBank.values()) {
                directChannelMap.put(directChannel.toString(), directChannel.getBankCode());
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching or parsing direct-channels list");
        }
        return directChannelMap;

    }
}
