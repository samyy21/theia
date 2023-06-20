package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.cashier.cachecard.model.VPACardRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.IMPSCardRequest;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.common.enums.CardTypeEnum;
import com.paytm.pgplus.common.enums.StatusEnum;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ICacheCardService;

/**
 * @author Manoj, Amit
 *
 */
@Service("savedCardUtilService")
public class SavedCardUtilService {

    @Autowired
    @Qualifier("cacheCardService")
    ICacheCardService cacheCardService;

    private SavedCardVO saveBankCardData(final BankCardRequest cardRequest, final String payerUserId,
            final String cardScheme, final String custId, final String mId) throws CashierCheckedException {

        // String encryptedCardNumber ;
        // String encryptedExpiryDate ;
        String decryptedCardNumber;
        String decryptedExpiryDate;
        Long firstSixDigit;
        Long lastFourDigit;

        try {
            // encryptedCardNumber =
            // CryptoUtils.encrypt(cardRequest.getCardNo());
            decryptedCardNumber = cardRequest.getCardNo();
            StringBuilder expiryMonth = new StringBuilder();

            if (cardRequest.getExpiryMonth().length() == 1) {
                expiryMonth.append(CashierConstant.ZERO).append(String.valueOf(cardRequest.getExpiryMonth()));
            } else {
                expiryMonth.append(cardRequest.getExpiryMonth());
            }

            // encryptedExpiryDate = CryptoUtils.encrypt(expiryMonth.toString()
            // + cardRequest.getExpiryYear());
            decryptedExpiryDate = expiryMonth.toString() + cardRequest.getExpiryYear();
            firstSixDigit = Long.valueOf(cardRequest.getCardNo().substring(0, 6));
            int cardNumberLength = cardRequest.getCardNo().length();
            lastFourDigit = Long.valueOf(cardRequest.getCardNo().substring(cardNumberLength - 4, cardNumberLength));

        } catch (Exception e) {
            throw new CashierCheckedException("Exception occured while encrypting card details using master key", e);
        }

        return buildSavedCardData(CardTypeEnum.getEnumByName(cardScheme), payerUserId, decryptedCardNumber,
                decryptedExpiryDate, firstSixDigit, lastFourDigit, custId, mId);
    }

    private SavedCardVO saveImpsCardData(final IMPSCardRequest impsCardRequest, String payerUserId)
            throws CashierCheckedException {
        // String encryptedCardNumber;
        // String encryptedExpiryDate ;
        String decryptedCardNumber;
        String decryptedExpiryDate;
        Long firstSixDigit;
        Long lastFourDigit;

        try {
            decryptedCardNumber = impsCardRequest.getHolderMobileNo();
            decryptedExpiryDate = impsCardRequest.getMmid();
            // encryptedCardNumber =
            // CryptoUtils.encrypt(impsCardRequest.getHolderMobileNo());
            // encryptedExpiryDate =
            // CryptoUtils.encrypt(impsCardRequest.getMmid());
            firstSixDigit = Long.valueOf(impsCardRequest.getHolderMobileNo().substring(0, 6));
            lastFourDigit = Long.valueOf(impsCardRequest.getMmid().substring(0, 4));

        } catch (Exception e) {
            throw new CashierCheckedException("Exception occured while encrypting card details using master key", e);
        }

        return buildSavedCardData(CardTypeEnum.IMPS, payerUserId, decryptedCardNumber, decryptedExpiryDate,
                firstSixDigit, lastFourDigit);
    }

    private SavedCardVO buildSavedCardData(CardTypeEnum cardTypeEnum, String payerUserId, String decryptedCardNumber,
            String decryptedExpiryDate, Long firstSixDigit, Long lastFourDigit) {
        SavedCardVO savedCardVO = new SavedCardVO();

        savedCardVO.setCardType(cardTypeEnum);

        savedCardVO.setCardNumber(decryptedCardNumber);
        savedCardVO.setExpiryDate(decryptedExpiryDate);
        savedCardVO.setStatus(StatusEnum.ACTIVE);
        savedCardVO.setUserId(payerUserId);
        savedCardVO.setFirstSixDigit(firstSixDigit);
        savedCardVO.setLastFourDigit(lastFourDigit);

        return savedCardVO;
    }

    private SavedCardVO buildSavedCardData(CardTypeEnum cardTypeEnum, String payerUserId, String decryptedCardNumber,
            String decryptedExpiryDate, Long firstSixDigit, Long lastFourDigit, String custId, String mId) {
        SavedCardVO savedCardVO = new SavedCardVO();

        savedCardVO.setCardType(cardTypeEnum);

        savedCardVO.setCardNumber(decryptedCardNumber);
        savedCardVO.setExpiryDate(decryptedExpiryDate);
        savedCardVO.setStatus(StatusEnum.ACTIVE);
        savedCardVO.setUserId(payerUserId);
        savedCardVO.setFirstSixDigit(firstSixDigit);
        savedCardVO.setLastFourDigit(lastFourDigit);
        savedCardVO.setCustId(custId);
        savedCardVO.setmId(mId);

        return savedCardVO;
    }

    private SavedCardVO saveVpaData(final VPACardRequest cardRequest, String payerUserId) {
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setCardNumber(cardRequest.getVpa());
        savedCardVO.setCardType(CardTypeEnum.VPA);
        savedCardVO.setStatus(StatusEnum.ACTIVE);
        savedCardVO.setUserId(payerUserId);
        return savedCardVO;
    }

    public void cacheCardData(CashierRequest cashierRequest) throws CashierCheckedException {
        if ((null != cashierRequest) && (null != cashierRequest.getInternalCardRequest())
                && (null != cashierRequest.getPaymentRequest())) {
            SavedCardVO savedCardVO;

            CompleteCardRequest completeCardRequest = cashierRequest.getInternalCardRequest();
            String userId = cashierRequest.getPaymentRequest().getExtendInfo().get(CashierConstant.PAYTM_USER_ID);
            String custId = cashierRequest.getPaymentRequest().getExtendInfo().get(CashierConstant.CUST_ID);
            String mId = cashierRequest.getCashierMerchant().getMerchantId();

            if (StringUtils.isBlank(userId) && StringUtils.isBlank(custId)) {
                throw new CashierCheckedException("No user found for saving card");
            }

            if (cashierRequest.getPaymentRequest().getPayBillOptions().isSaveChannelInfoAfterPay()) {
                switch (completeCardRequest.getInstNetworkType()) {
                case IMPS:
                    savedCardVO = saveImpsCardData(completeCardRequest.getImpsCardRequest(), userId);
                    break;
                case ISOCARD:
                    String cardScheme = completeCardRequest.getBinCardRequest().getCardScheme().getScheme();
                    savedCardVO = saveBankCardData(completeCardRequest.getBankCardRequest(), userId, cardScheme,
                            custId, mId);
                    break;
                case UPI:
                    savedCardVO = saveVpaData(completeCardRequest.getVpaCardRequest(), userId);
                    break;
                case IFSC:
                default:
                    throw new CashierCheckedException("Unsupported InstNetwork type");
                }

                String transactionId = cashierRequest.getPaymentRequest().getTransId();
                cacheCardService.saveCardDetailsInCache(transactionId, savedCardVO);

            }
        }
    }
}
