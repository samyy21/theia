/**
 *
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author kesari
 * @createdOn 28-Mar-2016
 */
public class TransactionInfo implements Serializable {

    private static final long serialVersionUID = 1257657438499584894L;
    @Tag(value = 1)
    private String currencyCode = "INR";
    @Tag(value = 2)
    private String walletAmount;
    @Tag(value = 3)
    private String cardType;

    @Tag(value = 4)
    private String txnAmount;
    @Tag(value = 5)
    private String paymentTypeId;

    @Tag(value = 6)
    private String txnId;
    @Tag(value = 7)
    private String txnStatus;
    @Tag(value = 8)
    private String responseCode;
    @Tag(value = 9)
    private String requestType;
    @Tag(value = 10)
    private String mid;

    @Tag(value = 11)
    private String orderId;
    @Tag(value = 12)
    private String mobileno;
    @Tag(value = 13)
    private String emailId;
    @Tag(value = 14)
    private String orderDetails;
    @Tag(value = 15)
    private String address1;
    @Tag(value = 16)
    private String address2;
    @Tag(value = 17)
    private String city;
    @Tag(value = 18)
    private String state;
    @Tag(value = 19)
    private String pincode;
    @Tag(value = 20)
    private String offerMessage;
    @Tag(value = 21)
    private boolean autoSignup;

    @Tag(value = 22)
    private String ssoID;
    @Tag(value = 23)
    private String ssoTokenType;
    @Tag(value = 24)
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String ssoToken;

    @Tag(value = 25)
    private String txnMode;
    @Tag(value = 26)
    private String custID;
    @Tag(value = 27)
    private String currencyID;

    @Tag(value = 28)
    private String requestID;
    @Tag(value = 29)
    private String securityID;
    @Tag(value = 30)
    private String addAndPaySecurityId;

    @Tag(value = 31)
    private long chargeFeeAmountATM = 0;
    @Tag(value = 32)
    private long chargeFeeAmountBALANCE = 0;
    @Tag(value = 33)
    private long chargeFeeAmountNetBanking = 0;
    @Tag(value = 34)
    private long chargeFeeAmountCC = 0;
    @Tag(value = 35)
    private long chargeFeeAmountDC = 0;
    @Tag(value = 36)
    private long chargeFeeAmountEMI = 0;
    @Tag(value = 37)
    private long chargeFeeAmountCOD = 0;
    @Tag(value = 38)
    private long chargeFeeAmountIMPS = 0;
    @Tag(value = 39)
    private long chargeFeeAmountBankExpress = 0;
    @Tag(value = 40)
    private long chargeFeeAmountHybrid = 0;

    @Tag(value = 41)
    private String website;

    @Tag(value = 42)
    private String subscriptionFrequency;
    @Tag(value = 43)
    private String subscriptionFrequencyUnit;
    @Tag(value = 44)
    private String subscriptionEnableRetry;
    @Tag(value = 45)
    private String subscriptionGraceDays;
    @Tag(value = 46)
    private String subscriptionStartDate;
    @Tag(value = 47)
    private String subscriptionExpiryDate;
    @Tag(value = 48)
    private String subscriptionServiceID;
    @Tag(value = 49)
    private String subscriptionAmountType;
    @Tag(value = 50)
    private String subscriptionPPIOnly;
    @Tag(value = 51)
    private String subscriptionPaymentMode;
    @Tag(value = 52)
    private String subscriptionMinAmount;
    @Tag(value = 53)
    private String subscriptionMaxAmount;

    @Tag(value = 54)
    private PromoCodeResponse promoCodeResponse;

    @Tag(value = 55)
    private boolean retry;
    @Tag(value = 56)
    private boolean promoCodeValid;
    @Tag(value = 57)
    private String displayMsg;
    @Tag(value = 58)
    private String previousPaymentMode;
    @Tag(value = 59)
    private String selectedBank;

    // Risk Parameter
    @Tag(value = 60)
    private boolean riskAllowed = true;
    @Tag(value = 61)
    private String totalConvenienceCharges;
    @Tag(value = 62)
    private String totalTransactionAmount;
    @Tag(value = 63)
    private String cacheCardToken;

    // payment otp
    @Tag(value = 64)
    private String saveStateAgainstPaymentOTP;
    @Tag(value = 65)
    private boolean isPaymentOTPRequired;
    @Tag(value = 66)
    private int maxInvalidAttempts;
    @Tag(value = 67)
    private int paymentOtpSendCount;

    @Tag(value = 68)
    private long chargeFeeAmountUPI = 0;
    @Tag(value = 69)
    private String transCreatedTime;
    @Tag(value = 70)
    private String VPA;
    @Tag(value = 71)
    private long chargeFeeAmountDigitalCredit = 0;
    @Tag(value = 72)
    private List<Integer> promoBins;

    @Tag(value = 73)
    private boolean isZeroRupeesSubscription;
    @Tag(value = 74)
    private String emiOption;
    @Tag(value = 75)
    private boolean cardTokenRequired;

    // QR Code

    @Tag(value = 76)
    private QRCodeDetailsResponse qrDetails;

    @Tag(value = 77)
    private String addMoneyDestination;

    // Samsung and zero cost EMI
    @Tag(value = 78)
    private boolean isCartValidationRequired;

    @Tag(value = 79)
    private boolean onus;

    @Tag(value = 80)
    private int prnValidationCount;

    @Tag(value = 81)
    private boolean prnValidationSuccessful;

    @Tag(value = 82)
    private Map<UserSubWalletType, BigDecimal> subwalletAmount;

    @Tag(value = 83)
    private String targetPhoneNo;

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    // for riskExtendedInfo
    @Tag(value = 84)
    private String trustFactor;

    /**
     * @return the cacheCardToken
     */
    public String getCacheCardToken() {
        return cacheCardToken;
    }

    /**
     * @param cacheCardToken
     *            the cacheCardToken to set
     */
    public void setCacheCardToken(String cacheCardToken) {
        this.cacheCardToken = cacheCardToken;
    }

    public boolean isRiskAllowed() {
        return riskAllowed;
    }

    public void setRiskAllowed(boolean riskAllowed) {
        this.riskAllowed = riskAllowed;
    }

    public String getSaveStateAgainstPaymentOTP() {
        return saveStateAgainstPaymentOTP;
    }

    public String getTotalConvenienceCharges() {
        return totalConvenienceCharges;
    }

    public void setTotalConvenienceCharges(String totalConvenienceCharges) {
        this.totalConvenienceCharges = totalConvenienceCharges;
    }

    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

    public void setSaveStateAgainstPaymentOTP(String saveStateAgainstPaymentOTP) {
        this.saveStateAgainstPaymentOTP = saveStateAgainstPaymentOTP;
    }

    public boolean getIsPaymentOTPRequired() {
        return isPaymentOTPRequired;
    }

    public void setIsPaymentOTPRequired(boolean isPaymentOTPRequired) {
        this.isPaymentOTPRequired = isPaymentOTPRequired;
    }

    public int getMaxInvalidAttempts() {
        return maxInvalidAttempts;
    }

    public void setMaxInvalidAttempts(int maxInvalidAttempts) {
        this.maxInvalidAttempts = maxInvalidAttempts;
    }

    public int getPaymentOtpSendCount() {
        return paymentOtpSendCount;
    }

    public void setPaymentOtpSendCount(int paymentOtpSendCount) {
        this.paymentOtpSendCount = paymentOtpSendCount;
    }

    /**
     * @return the previousPaymentMode
     */
    public String getPreviousPaymentMode() {
        return previousPaymentMode;
    }

    /**
     * @param previousPaymentMode
     *            the previousPaymentMode to set
     */
    public void setPreviousPaymentMode(String previousPaymentMode) {
        this.previousPaymentMode = previousPaymentMode;
    }

    /**
     * @return the promoCodeValid
     */
    public boolean isPromoCodeValid() {
        return promoCodeValid;
    }

    /**
     * @param promoCodeValid
     *            the promoCodeValid to set
     */
    public void setPromoCodeValid(boolean promoCodeValid) {
        this.promoCodeValid = promoCodeValid;
    }

    /**
     * @return the retry
     */
    public boolean isRetry() {
        return retry;
    }

    /**
     * @param retry
     *            the retry to set
     */
    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public String getSubscriptionPPIOnly() {
        return subscriptionPPIOnly;
    }

    public void setSubscriptionPPIOnly(final String subscriptionPPIOnly) {
        this.subscriptionPPIOnly = subscriptionPPIOnly;
    }

    public String getSubscriptionPaymentMode() {
        return subscriptionPaymentMode;
    }

    public void setSubscriptionPaymentMode(final String subscriptionPaymentMode) {
        this.subscriptionPaymentMode = subscriptionPaymentMode;
    }

    public String getSubscriptionMinAmount() {
        return subscriptionMinAmount;
    }

    public void setSubscriptionMinAmount(final String subscriptionMinAmount) {
        this.subscriptionMinAmount = subscriptionMinAmount;
    }

    public String getSubscriptionMaxAmount() {
        return subscriptionMaxAmount;
    }

    public void setSubscriptionMaxAmount(final String subscriptionMaxAmount) {
        this.subscriptionMaxAmount = subscriptionMaxAmount;
    }

    public String getSubscriptionFrequency() {
        return subscriptionFrequency;
    }

    public void setSubscriptionFrequency(final String subscriptionFrequency) {
        this.subscriptionFrequency = subscriptionFrequency;
    }

    public String getSubscriptionFrequencyUnit() {
        return subscriptionFrequencyUnit;
    }

    public void setSubscriptionFrequencyUnit(final String subscriptionFrequencyUnit) {
        this.subscriptionFrequencyUnit = subscriptionFrequencyUnit;
    }

    public String getSubscriptionEnableRetry() {
        return subscriptionEnableRetry;
    }

    public void setSubscriptionEnableRetry(final String subscriptionEnableRetry) {
        this.subscriptionEnableRetry = subscriptionEnableRetry;
    }

    public String getSubscriptionGraceDays() {
        return subscriptionGraceDays;
    }

    public void setSubscriptionGraceDays(final String subscriptionGraceDays) {
        this.subscriptionGraceDays = subscriptionGraceDays;
    }

    public String getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    /**
     * @return the cardType
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * @param cardType
     *            the cardType to set
     */
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setSubscriptionStartDate(final String subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public String getSubscriptionExpiryDate() {
        return subscriptionExpiryDate;
    }

    public void setSubscriptionExpiryDate(final String subscriptionExpiryDate) {
        this.subscriptionExpiryDate = subscriptionExpiryDate;
    }

    public String getSubscriptionServiceID() {
        return subscriptionServiceID;
    }

    public void setSubscriptionServiceID(final String subscriptionServiceID) {
        this.subscriptionServiceID = subscriptionServiceID;
    }

    public String getSubscriptionAmountType() {
        return subscriptionAmountType;
    }

    public void setSubscriptionAmountType(final String subscriptionAmountType) {
        this.subscriptionAmountType = subscriptionAmountType;
    }

    public long getChargeFeeAmountHybrid() {
        return chargeFeeAmountHybrid;
    }

    public void setChargeFeeAmountHybrid(final long chargeFeeAmountHybrid) {
        this.chargeFeeAmountHybrid = chargeFeeAmountHybrid;
    }

    public long getChargeFeeAmountBankExpress() {
        return chargeFeeAmountBankExpress;
    }

    public void setChargeFeeAmountBankExpress(final long chargeFeeAmountBankExpress) {
        this.chargeFeeAmountBankExpress = chargeFeeAmountBankExpress;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(final String requestID) {
        this.requestID = requestID;
    }

    public String getSecurityID() {
        return securityID;
    }

    public void setSecurityID(final String securityID) {
        this.securityID = securityID;
    }

    public long getChargeFeeAmountATM() {
        return chargeFeeAmountATM;
    }

    public void setChargeFeeAmountATM(final long chargeFeeAmountATM) {
        this.chargeFeeAmountATM = chargeFeeAmountATM;
    }

    public long getChargeFeeAmountBALANCE() {
        return chargeFeeAmountBALANCE;
    }

    public void setChargeFeeAmountBALANCE(final long chargeFeeAmountBALANCE) {
        this.chargeFeeAmountBALANCE = chargeFeeAmountBALANCE;
    }

    public long getChargeFeeAmountNetBanking() {
        return chargeFeeAmountNetBanking;
    }

    public void setChargeFeeAmountNetBanking(final long chargeFeeAmountNetBanking) {
        this.chargeFeeAmountNetBanking = chargeFeeAmountNetBanking;
    }

    public long getChargeFeeAmountCC() {
        return chargeFeeAmountCC;
    }

    public void setChargeFeeAmountCC(final long chargeFeeAmountCC) {
        this.chargeFeeAmountCC = chargeFeeAmountCC;
    }

    public long getChargeFeeAmountDC() {
        return chargeFeeAmountDC;
    }

    public void setChargeFeeAmountDC(final long chargeFeeAmountDC) {
        this.chargeFeeAmountDC = chargeFeeAmountDC;
    }

    public long getChargeFeeAmountEMI() {
        return chargeFeeAmountEMI;
    }

    public void setChargeFeeAmountEMI(final long chargeFeeAmountEMI) {
        this.chargeFeeAmountEMI = chargeFeeAmountEMI;
    }

    public long getChargeFeeAmountCOD() {
        return chargeFeeAmountCOD;
    }

    public void setChargeFeeAmountCOD(final long chargeFeeAmountCOD) {
        this.chargeFeeAmountCOD = chargeFeeAmountCOD;
    }

    public long getChargeFeeAmountIMPS() {
        return chargeFeeAmountIMPS;
    }

    public void setChargeFeeAmountIMPS(final long chargeFeeAmountIMPS) {
        this.chargeFeeAmountIMPS = chargeFeeAmountIMPS;
    }

    public TransactionInfo() {
    }

    public TransactionInfo(final HttpServletRequest request) {
        orderId = request.getParameter("ORDER_ID");
        txnMode = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
        cardType = request.getParameter(PaymentRequestParam.CARD_TYPE.getValue());
    }

    public String getOfferMessage() {
        return offerMessage;
    }

    public void setOfferMessage(final String offerMessage) {
        this.offerMessage = offerMessage;
    }

    public boolean isAutoSignup() {
        return autoSignup;
    }

    public void setAutoSignup(final boolean autoSignup) {
        this.autoSignup = autoSignup;
    }

    public String getSsoID() {
        return ssoID;
    }

    public void setSsoID(final String ssoID) {
        this.ssoID = ssoID;
    }

    public String getSsoTokenType() {
        return ssoTokenType;
    }

    public void setSsoTokenType(final String ssoTokenType) {
        this.ssoTokenType = ssoTokenType;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(final String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getTxnMode() {
        return txnMode;
    }

    public void setTxnMode(final String txnMode) {
        this.txnMode = txnMode;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(final String custID) {
        this.custID = custID;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(final String currencyID) {
        this.currencyID = currencyID;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(final String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(final String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(final String pincode) {
        this.pincode = pincode;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(final String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getMobileno() {
        return mobileno;
    }

    public void setMobileno(final String mobileno) {
        this.mobileno = mobileno;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(final String walletAmount) {
        this.walletAmount = walletAmount;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(final String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(final String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(final String txnId) {
        this.txnId = txnId;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(final String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(final String responseCode) {
        this.responseCode = responseCode;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(final String requestType) {
        this.requestType = requestType;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(final String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @param website
     *            the website to set
     */
    public void setWebsite(final String website) {
        this.website = website;
    }

    /**
     * @return the addAndPaySecurityId
     */
    public String getAddAndPaySecurityId() {
        return addAndPaySecurityId;
    }

    /**
     * @param addAndPaySecurityId
     *            the addAndPaySecurityId to set
     */
    public void setAddAndPaySecurityId(final String addAndPaySecurityId) {
        this.addAndPaySecurityId = addAndPaySecurityId;
    }

    public PromoCodeResponse getPromoCodeResponse() {
        return promoCodeResponse;
    }

    public void setPromoCodeResponse(final PromoCodeResponse promoCodeResponse) {
        this.promoCodeResponse = promoCodeResponse;
    }

    /**
     * @return the displayMsg
     */
    public String getDisplayMsg() {
        return displayMsg;
    }

    /**
     * @param displayMsg
     *            the displayMsg to set
     */
    public void setDisplayMsg(String displayMsg) {
        this.displayMsg = displayMsg;
    }

    /**
     * @return the selectedBank
     */
    public String getSelectedBank() {
        return selectedBank;
    }

    /**
     * @param selectedBank
     *            the selectedBank to set
     */
    public void setSelectedBank(String selectedBank) {
        this.selectedBank = selectedBank;
    }

    /**
     * @return the isZeroRupeesSubscription
     */
    public boolean isZeroRupeesSubscription() {
        return isZeroRupeesSubscription;
    }

    /**
     * @param isZeroRupeesSubscription
     *            the isZeroRupeesSubscription to set
     */
    public void setZeroRupeesSubscription(boolean isZeroRupeesSubscription) {
        this.isZeroRupeesSubscription = isZeroRupeesSubscription;
    }

    public long getChargeFeeAmountUPI() {
        return chargeFeeAmountUPI;
    }

    public void setChargeFeeAmountUPI(long chargeFeeAmountUPI) {
        this.chargeFeeAmountUPI = chargeFeeAmountUPI;
    }

    public String getTransCreatedTime() {
        return transCreatedTime;
    }

    public void setTransCreatedTime(String transCreatedTime) {
        this.transCreatedTime = transCreatedTime;
    }

    public String getVPA() {
        return VPA;
    }

    public void setVPA(String vPA) {
        VPA = vPA;
    }

    public long getChargeFeeAmountDigitalCredit() {
        return chargeFeeAmountDigitalCredit;
    }

    public void setChargeFeeAmountDigitalCredit(long chargeFeeAmountDigitalCredit) {
        this.chargeFeeAmountDigitalCredit = chargeFeeAmountDigitalCredit;
    }

    public List<Integer> getPromoBins() {
        return promoBins;
    }

    public void setPromoBins(List<Integer> promoCardBins) {
        this.promoBins = promoCardBins;
    }

    public String getEmiOption() {
        return emiOption;
    }

    public void setEmiOption(String emiOption) {
        this.emiOption = emiOption;
    }

    public boolean isCardTokenRequired() {
        return cardTokenRequired;
    }

    public void setCardTokenRequired(boolean cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
    }

    public QRCodeDetailsResponse getQrDetails() {
        return qrDetails;
    }

    public void setQrDetails(QRCodeDetailsResponse qrDetails) {
        this.qrDetails = qrDetails;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public boolean isCartValidationRequired() {
        return isCartValidationRequired;
    }

    public void setCartValidationRequired(boolean cartValidationRequired) {
        isCartValidationRequired = cartValidationRequired;
    }

    public boolean isOnus() {
        return onus;
    }

    public void setOnus(boolean onus) {
        this.onus = onus;
    }

    public int getPrnValidationCount() {
        return prnValidationCount;
    }

    public void setPrnValidationCount(int prnValidationCount) {
        this.prnValidationCount = prnValidationCount;
    }

    public boolean isPrnValidationSuccessful() {
        return prnValidationSuccessful;
    }

    public void setPrnValidationSuccessful(boolean prnValidationSuccessful) {
        this.prnValidationSuccessful = prnValidationSuccessful;
    }

    public Map<UserSubWalletType, BigDecimal> getSubwalletAmount() {
        return subwalletAmount;
    }

    public void setSubwalletAmount(Map<UserSubWalletType, BigDecimal> subwalletAmount) {
        this.subwalletAmount = subwalletAmount;
    }

    public String getTrustFactor() {
        return trustFactor;
    }

    public void setTrustFactor(String trustFactor) {
        this.trustFactor = trustFactor;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
