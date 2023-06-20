/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.service.impl;

import com.paytm.pgplus.cashier.cachecard.model.*;
import com.paytm.pgplus.cashier.cachecard.service.ICashierCardService;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.cashier.validator.service.IBankCardValidation;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.paytm.pgplus.cashier.constant.CashierConstant.PAYTM_USER_ID;

/**
 * @author amitdubey
 *
 */
@Component("cacheCardServiceImpl")
public class CashierCardServiceImpl implements ICashierCardService {
    public static final Logger LOGGER = LoggerFactory.getLogger(CashierCardServiceImpl.class);
    public static final String USER_CARDS = "USER_CARDS_";

    @Autowired
    IFacadeService facadeServiceImpl;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    IBankCardValidation bankCardValidation;

    /*
     * (non-Javadoc)
     * 
     * @see com.paytm.pgplus.cashier.cachecard.service.ICashierCardService#
     * submitCacheCard(com.paytm.pgplus.cashier.models.CashierRequest,
     * com.paytm.pgplus.facade.enums.InstNetworkType)
     */
    @Override
    public CacheCardResponseBody submitCacheCard(CashierRequest cashierRequest, InstNetworkType instNetworkType)
            throws CashierCheckedException, PaytmValidationException {

        String savedCardId = null;
        boolean isIMPSRequest = false;
        BinCardRequest binCardRequest = null;
        BankCardRequest bankCardRequest = null;
        CardRequest cardRequest = cashierRequest.getCardRequest();

        bankCardValidation.validateSubmitCacheCardRequest(cardRequest);

        CompleteCardRequest completeCardRequest = new CompleteCardRequest(instNetworkType);

        if (null != cardRequest.getSavedCardRequest()) {
            SavedCardRequest savedCardRequest = cardRequest.getSavedCardRequest();
            savedCardId = savedCardRequest.getSavedCardId();
            String userID = cashierRequest.getPaymentRequest().getExtendInfo().get(PAYTM_USER_ID);
            boolean storeCardPref = Boolean.parseBoolean(cashierRequest.getPaymentRequest().getExtendInfo()
                    .get(CashierConstant.STORE_CARD_PREFERENCE));
            String mId = cashierRequest.getCashierMerchant().getMerchantId();
            String custId = cashierRequest.getPaymentRequest().getExtendInfo().get(CashierConstant.CUST_ID);
            SavedCardVO savedCard = null;
            if (storeCardPref && StringUtils.isNotEmpty(custId) && StringUtils.isNotEmpty(mId)) {
                savedCard = cashierUtilService.getSavedCardDetailsByCustIdMid(Long.parseLong(savedCardId), userID,
                        custId, mId);
            } else {
                savedCard = cashierUtilService.getSavedCardDetails(Long.parseLong(savedCardId), userID);
            }
            bankCardValidation.validateSavedCard(savedCard);

            binCardRequest = cashierRequest.getBinCardRequest();
            bankCardRequest = cashierUtilService.buildBankCardRequestFromSavedCardID(savedCard, cardRequest
                    .getSavedCardRequest().getCvv());

            if (null == bankCardRequest) {
                throw new CashierCheckedException("Process failed : bank card request null for saved card id ");
            }

            bankCardRequest.setCardType(savedCardRequest.getCardType());

            completeCardRequest.setBankCardRequest(bankCardRequest);
            completeCardRequest.setBinCardRequest(binCardRequest);
            completeCardRequest.setSavedDataRequest(true);
        } else if (null != cardRequest.getSavedImpsCardRequest()) {
            String otp = cardRequest.getSavedImpsCardRequest().getOtp();
            savedCardId = cardRequest.getSavedImpsCardRequest().getSavedCardId();
            String userID = cashierRequest.getPaymentRequest().getExtendInfo().get("PAYTM_USER_ID");
            String instNetworkCode = cardRequest.getSavedImpsCardRequest().getInstNetworkCode();

            SavedCardVO savedCard = cashierUtilService.getSavedCardDetails(Long.parseLong(savedCardId), userID);
            bankCardValidation.validateSavedCard(savedCard);

            String mmid = savedCard.getExpiryDate();
            String holderMobileNo = savedCard.getCardNumber();

            IMPSCardRequest impsCardRequest = new IMPSCardRequest(mmid, otp, instNetworkCode, holderMobileNo);
            completeCardRequest.setImpsCardRequest(impsCardRequest);

            isIMPSRequest = true;
            completeCardRequest.setSavedDataRequest(true);
        } else if (null != cardRequest.getBankCardRequest()) {
            binCardRequest = cashierRequest.getBinCardRequest();
            bankCardRequest = cardRequest.getBankCardRequest();

            completeCardRequest.setBankCardRequest(bankCardRequest);
            completeCardRequest.setBinCardRequest(binCardRequest);
            completeCardRequest.setSavedDataRequest(false);
        } else if (null != cardRequest.getImpsCardRequest()) {
            completeCardRequest.setImpsCardRequest(cardRequest.getImpsCardRequest());
            isIMPSRequest = true;
            completeCardRequest.setSavedDataRequest(false);
        }

        if (!isIMPSRequest
                && (binCardRequest != null && null != binCardRequest.getCardScheme() && null != binCardRequest
                        .getCardType())) {

            completeCardRequest.setDirectInstService(cashierRequest.isDirectBankCardPayRequest());
            bankCardValidation.validateCard(completeCardRequest, binCardRequest);

            Map<String, String> extendedInfo = cashierRequest.getPaymentRequest().getPayBillOptions().getExtendInfo();

            extendedInfo.put(CashierConstant.ISSUING_BANK_NAME, completeCardRequest.getBinCardRequest().getBankName());
            extendedInfo.put(CashierConstant.ISSUING_BANK_ID, completeCardRequest.getBinCardRequest().getInstId());

            PaymentRequest paymentRequest = cashierRequest.getPaymentRequest();

            String cardSchema = binCardRequest.getCardScheme().getScheme();
            String cardTypeVal = binCardRequest.getCardType().getValue();

            if (PayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(cardTypeVal)) {
                paymentRequest.getPayBillOptions().getPayOptions().remove(PayMethod.CREDIT_CARD);
                paymentRequest.getPayBillOptions().getPayOptions()
                        .put(PayMethod.DEBIT_CARD, CashierConstant.DEBIT_CARD_PAY_OPTION.concat(cardSchema));
            } else if (PayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(cardTypeVal)
                    && !paymentRequest.getPayBillOptions().getPayOptions().containsKey(PayMethod.EMI)) {
                paymentRequest.getPayBillOptions().getPayOptions().remove(PayMethod.DEBIT_CARD);
                paymentRequest.getPayBillOptions().getPayOptions()
                        .put(PayMethod.CREDIT_CARD, CashierConstant.CREDIT_CARD_PAY_OPTION.concat(cardSchema));
            }
        }

        CacheCardResponseBody cacheCardResponse = facadeServiceImpl.getCacheCardTokenId(completeCardRequest);

        if (StringUtils.isBlank(cacheCardResponse.getTokenId())) {
            LOGGER.error("No cache card token received");
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
        }

        // update saved card last usage
        if (StringUtils.isNotBlank(savedCardId)) {
            cashierUtilService.updateSavedCardLastUsage(savedCardId);
        }

        // save card request to used in notification
        cashierRequest.setInternalCardRequest(completeCardRequest);

        return cacheCardResponse;
    }
}