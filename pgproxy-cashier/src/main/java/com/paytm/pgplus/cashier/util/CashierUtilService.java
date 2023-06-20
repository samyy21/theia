package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.SaveCardValidationException;
import com.paytm.pgplus.cashier.models.CashierResponseCodeDetails;
import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;

/**
 * @author Himanshu Sardana, Amit Dubey
 *
 */
@Service
public class CashierUtilService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashierUtilService.class);

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardService;

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    private static final String EXPIRY_DATE_SEPARATOR = "/";
    private static final String EXPIRY_DATE_SEPARATOR_TWO = "\\|";
    private static final String EXPIRY_DATE_SEPARATOR_ = "|";
    private static final String SIX_DIGIT_BIN_LOGGING = "theia.sixDigitBinLogging";

    /**
     * @param binCardNumber
     * @return
     * @throws PaytmValidationException
     * @throws NumberFormatException
     */
    public BinCardRequest getBinCardRequest(String binCardNumber) throws PaytmValidationException {

        return new BinCardRequest(cardUtils.fetchBinDetails(binCardNumber));
    }

    /**
     * @param savedCardId
     * @return
     * @throws CashierCheckedException
     */
    public SavedCardVO getSavedCardDetails(Long savedCardId, String userID) throws SaveCardValidationException {

        SavedCardVO savedCard;

        SavedCardResponse<SavedCardVO> saveCardServiceResponse = savedCardService.getSavedCardByCardId(savedCardId,
                userID);

        if ((saveCardServiceResponse != null) && saveCardServiceResponse.getStatus()) {
            savedCard = saveCardServiceResponse.getResponseData();
        } else {
            throw new SaveCardValidationException(ConfigurationUtil.getProperty("savecard.not.found", ""),
                    PaytmValidationExceptionType.DELETED_SAVECARD, savedCardId.toString());
        }
        return savedCard;
    }

    public BankCardRequest buildBankCardRequestFromSavedCardID(SavedCardVO savedCard, String cvv)
            throws CashierCheckedException, PaytmValidationException {

        BankCardRequest bankCardRequest;

        if (savedCard != null) {
            String[] dateYear = getExpiryYearAndMonthFromDate(savedCard.getExpiryDate());
            String cardNumber = savedCard.getCardNumber();

            if (StringUtils.isEmpty(cardNumber) || cardNumber.length() < 10) {
                throw new CashierCheckedException("Process failed :SavedCard Not Found...");
            }

            String binNumber = StringUtils.substring(cardNumber, 0, 6);

            if (iPgpFf4jClient.checkWithDefault(SIX_DIGIT_BIN_LOGGING, false) && StringUtils.isNotBlank(binNumber)) {
                LOGGER.info("Bin number length is 6.");
            }
            String cardScheme = cardUtils.fetchCardSchemeUsingCustomLogic(binNumber);
            bankCardRequest = new BankCardRequest(cardNumber, cvv, dateYear[1], dateYear[0], cardScheme);
        } else {
            throw new CashierCheckedException("Process failed :SavedCard Not Found...");
        }

        return bankCardRequest;
    }

    /**
     * @param expiryDate
     * @return
     * @throws CashierCheckedException
     */
    public String[] getExpiryYearAndMonthFromDate(String expiryDate) throws CashierCheckedException {

        if (StringUtils.isBlank(expiryDate)) {
            throw new CashierCheckedException("ExpiryDate not found for the saved card");
        }

        if (expiryDate.contains(EXPIRY_DATE_SEPARATOR)) {
            String[] dateArray = expiryDate.split(EXPIRY_DATE_SEPARATOR);

            if (dateArray.length != 2) {
                throw new CashierCheckedException("There is some error in ExpiryDate while splitting");
            }
            return dateArray;
        } else if (expiryDate.contains(EXPIRY_DATE_SEPARATOR_)) {
            String[] dateArray = expiryDate.split(EXPIRY_DATE_SEPARATOR_TWO);

            if (dateArray.length != 2) {
                throw new CashierCheckedException("There is some error in ExpiryDate while splitting");
            }
            return dateArray;
        } else {
            if (expiryDate.length() == 6) {
                String[] dateArray = new String[2];
                dateArray[0] = expiryDate.substring(0, 2);
                dateArray[1] = expiryDate.substring(2, 6);

                return dateArray;
            } else if (expiryDate.length() == 4) {
                String[] dateArray = new String[2];
                dateArray[0] = expiryDate.substring(0, 2);
                dateArray[1] = "20" + expiryDate.substring(2, 4);

                return dateArray;
            } else {
                throw new CashierCheckedException("ExpiryDate param length should be six, actual length found : "
                        + expiryDate.length());
            }
        }
    }

    /**
     * @param resultCode
     * @return
     */
    public CashierResponseCodeDetails getMerchantResponseCode(String resultCode) {
        CashierResponseCodeDetails cashierResponseCodeDetails = new CashierResponseCodeDetails();

        try {
            ResponseCodeDetails responseCodeDetails = responseCodeService.getResponseCodeDetails(resultCode);
            if ((responseCodeDetails != null) && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {

                cashierResponseCodeDetails.setPaytmResponseCode(responseCodeDetails.getResponseCode());
                // cashierResponseCodeDetails.setRetry(responseCodeDetails.isRetryPossible());
                cashierResponseCodeDetails.setRetry(true);
                cashierResponseCodeDetails.setAlipayResponseCode(resultCode);
                cashierResponseCodeDetails.setErrorMessage(responseCodeDetails.getDisplayMessage());
            }

        } catch (Exception e) {
            LOGGER.error("Unable to find response code details, exception occured :", e);
        }

        return cashierResponseCodeDetails;
    }

    /**
     * @param savedCardId
     * @param userID
     * @return
     * @throws CashierCheckedException
     * @throws SecurityException
     */
    public String getCardNumer(Long savedCardId, String userID) throws PaytmValidationException, SecurityException {
        SavedCardVO savedCard = getSavedCardDetails(savedCardId, userID);
        return savedCard.getCardNumber();
    }

    /**
     * @param savedCardId
     * @throws CashierCheckedException
     */
    public void updateSavedCardLastUsage(String savedCardId) throws CashierCheckedException {

        if (StringUtils.isBlank(savedCardId)) {
            throw new CashierCheckedException("No saved card id found for updating record");
        }

        savedCardService.updateSaveCardLastUse(Long.parseLong(savedCardId));
    }

    public SavedCardVO getSavedCardDetailsByCustIdMid(Long savedCardId, String userID, String custId, String mId)
            throws SaveCardValidationException {

        SavedCardVO savedCard;

        SavedCardResponse<SavedCardVO> saveCardServiceResponse = savedCardService.getSavedCardByCardId(savedCardId,
                userID, custId, mId);

        if ((saveCardServiceResponse != null) && saveCardServiceResponse.getStatus()) {
            savedCard = saveCardServiceResponse.getResponseData();
        } else {
            throw new SaveCardValidationException(ConfigurationUtil.getProperty("savecard.not.found", ""),
                    PaytmValidationExceptionType.DELETED_SAVECARD, savedCardId.toString());
        }
        return savedCard;
    }

    public SavedCardVO getSavedCardDetailByCustMid(Long savedCardId, String custId, String mid)
            throws SaveCardValidationException {

        SavedCardVO savedCard;

        SavedCardResponse<SavedCardVO> saveCardServiceResponse = savedCardService.getSavedCardByCardId(savedCardId,
                custId, mid);

        if ((saveCardServiceResponse != null) && saveCardServiceResponse.getStatus()) {
            savedCard = saveCardServiceResponse.getResponseData();
        } else {
            throw new SaveCardValidationException(ConfigurationUtil.getProperty("savecard.not.found", ""),
                    PaytmValidationExceptionType.DELETED_SAVECARD, savedCardId.toString());
        }
        return savedCard;
    }
}