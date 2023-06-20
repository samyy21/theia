/**
 *
 */
package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * @author Lalit Mehra
 * @since April 11, 2016
 *
 */
public class TheiaPaymentRequest implements Serializable {

    private static final long serialVersionUID = 2016439371309476420L;

    private final HttpServletRequest request;
    private final String txnAmount;
    private final String authMode;
    private final String cardType;
    private String bankCode;
    private final String merchantCallbackURL;
    private String paymentMode;
    private final String cardNo;
    private final String cvv;
    private final String walletAmount;
    private final String savedCardId;
    private String txnMode;
    private final String expiryYear;
    private final String expiryMonth;
    private final String browserUserAgent;
    private String emiPlanID;
    private final String channelId;
    private EChannelId channel;

    private String mmid;
    private String otp;
    private String instNetworkCode;
    private String holderMobileNo;
    private String storeCardFlag;
    private String addMoneyFlag;
    private String savedCardType;
    private final String txnMde;
    private String vpa;
    private String isIciciIDebit;
    private String passCode;
    private String mpin;
    private String deviceId;
    private String sequenceNumber;
    private String ccDirect;
    private String appId;

    public TheiaPaymentRequest(final HttpServletRequest request) {
        this.request = request;
        this.txnAmount = request.getParameter(PaymentRequestParam.TXN_AMOUNT.getValue());
        this.authMode = request.getParameter(PaymentRequestParam.AUTH_MODE.getValue());
        this.cardType = request.getParameter(PaymentRequestParam.CARD_TYPE.getValue());
        this.bankCode = StringUtils.isNotBlank(request.getParameter(PaymentRequestParam.BANK_CODE.getValue())) ? request
                .getParameter(PaymentRequestParam.BANK_CODE.getValue()) : request
                .getParameter(PaymentRequestParam.EMI_BANK_CODE.getValue());
        this.merchantCallbackURL = request.getParameter(PaymentRequestParam.MERCHANT_CALLBACK_URL.getValue());
        this.paymentMode = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
        this.cardNo = StringUtils.isNotBlank(request.getParameter(PaymentRequestParam.CARD_NUMBER.getValue())) ? request
                .getParameter(PaymentRequestParam.CARD_NUMBER.getValue()).replaceAll(" ", "").replaceAll("\t", "")
                : null;
        this.cvv = StringUtils.isNotBlank(request.getParameter(PaymentRequestParam.CVV.getValue())) ? request
                .getParameter(PaymentRequestParam.CVV.getValue()).replaceAll(" ", "").replaceAll("\t", "") : null;
        this.walletAmount = request.getParameter(PaymentRequestParam.WALLET_AMOUNT.getValue());
        this.savedCardId = request.getParameter(PaymentRequestParam.SAVED_CARD_ID.getValue());
        this.txnMode = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
        this.txnMde = request.getParameter(PaymentRequestParam.TXN_MDE.getValue());
        this.expiryYear = request.getParameter(PaymentRequestParam.EXPIRY_YEAR.getValue());
        this.expiryMonth = request.getParameter(PaymentRequestParam.EXPIRY_MONTH.getValue());
        this.channelId = request.getParameter(TheiaConstant.RequestParams.CHANNEL_ID);
        this.browserUserAgent = request.getHeader(TheiaConstant.RequestHeaders.USER_AGENT);
        this.mmid = request.getParameter(TheiaConstant.RequestParams.IMPS_MMID);
        this.otp = request.getParameter(TheiaConstant.RequestParams.IMPS_OTP);
        this.holderMobileNo = request.getParameter(TheiaConstant.RequestParams.IMPS_MOBILE_NO);
        this.instNetworkCode = request.getParameter(PaymentRequestParam.BANK_CODE.getValue());
        this.emiPlanID = request.getParameter(PaymentRequestParam.EMI_PLAN_ID.getValue());
        this.storeCardFlag = request.getParameter(PaymentRequestParam.STORE_CARD_FLAG.getValue());
        this.addMoneyFlag = request.getParameter(PaymentRequestParam.ADD_MONEY.getValue());
        this.savedCardType = request.getParameter(PaymentRequestParam.SAVED_CARD_TYPE.getValue());
        this.vpa = request.getParameter(PaymentRequestParam.VIRTUAL_PAYMENT_ADDRESS.getValue());
        this.isIciciIDebit = request.getParameter(PaymentRequestParam.ICICI_IDEBIT.getValue());
        this.passCode = request.getParameter(PaymentRequestParam.PASS_CODE.getValue());
        this.mpin = request.getParameter(PaymentRequestParam.MPIN.getValue());
        this.deviceId = request.getParameter(PaymentRequestParam.DEVICE_ID.getValue());
        this.sequenceNumber = request.getParameter(PaymentRequestParam.SEQ_NO.getValue());
        this.ccDirect = request.getParameter(PaymentRequestParam.CC_DIRECT.getValue());
        this.appId = request.getParameter(PaymentRequestParam.APP_ID.getValue());
    }

    public String getCcDirect() {
        return ccDirect;
    }

    public void setCcDirect(String ccDirect) {
        this.ccDirect = ccDirect;
    }

    public String isIciciIDebit() {
        return isIciciIDebit;
    }

    public String getSavedCardType() {
        return savedCardType;
    }

    public String getEmiPlanID() {
        return emiPlanID;
    }

    public void setEmiPlanID(String emiPlanID) {
        this.emiPlanID = emiPlanID;
    }

    /**
     * @return the channel
     */
    public EChannelId getChannel() {
        return channel;
    }

    /**
     * @param channel
     *            the channel to set
     */
    public void setChannel(final EChannelId channel) {
        this.channel = channel;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public String getAuthMode() {
        return authMode;
    }

    public String getCardType() {
        return cardType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getMerchantCallbackURL() {
        return merchantCallbackURL;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCvv() {
        return cvv;
    }

    public String getWalletAmount() {
        return walletAmount;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getTxnMode() {
        return txnMode;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    /**
     * @return the channelId
     */
    public String getChannelId() {
        return channelId;
    }

    public String getBrowserUserAgent() {
        return browserUserAgent;
    }

    /**
     * @return the mmid
     */
    public String getMmid() {
        return mmid;
    }

    /**
     * @param mmid
     *            the mmid to set
     */
    public void setMmid(String mmid) {
        this.mmid = mmid;
    }

    /**
     * @return the otp
     */
    public String getOtp() {
        return otp;
    }

    /**
     * @param otp
     *            the otp to set
     */
    public void setOtp(String otp) {
        this.otp = otp;
    }

    /**
     * @return the instNetworkCode
     */
    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    /**
     * @param instNetworkCode
     *            the instNetworkCode to set
     */
    public void setInstNetworkCode(String instNetworkCode) {
        this.instNetworkCode = instNetworkCode;
    }

    /**
     * @return the holderMobileNo
     */
    public String getHolderMobileNo() {
        return holderMobileNo;
    }

    /**
     * @param holderMobileNo
     *            the holderMobileNo to set
     */
    public void setHolderMobileNo(String holderMobileNo) {
        this.holderMobileNo = holderMobileNo;
    }

    public String getStoreCardFlag() {
        return storeCardFlag;
    }

    /**
     * @return the addMoneyFlag
     */
    public String getAddMoneyFlag() {
        return addMoneyFlag;
    }

    /**
     * @return the txnMde
     */
    public String getTxnMde() {
        return txnMde;
    }

    /**
     * @param paymentMode
     *            the paymentMode to set
     */
    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    /**
     * @param txnMode
     *            the txnMode to set
     */
    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getAppId() {
        return appId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TheiaPaymentRequest [txnAmount=").append(txnAmount).append(", authMode=").append(authMode)
                .append(", cardType=").append(cardType).append(", bankCode=").append(bankCode)
                .append(", merchantCallbackURL=").append(merchantCallbackURL).append(", paymentMode=")
                .append(paymentMode).append(", cardNo=").append(maskedCardNumber(cardNo)).append(", cvv=")
                .append(StringUtils.isNotBlank(cvv)).append(", walletAmount=").append(walletAmount)
                .append(", savedCardId=").append(savedCardId).append(", txnMode=").append(txnMode)
                .append(", expiryYear=").append(StringUtils.isNotBlank(expiryYear)).append(", expiryMonth=")
                .append(StringUtils.isNotBlank(expiryMonth)).append(", browserUserAgent=").append(browserUserAgent)
                .append(", emiPlanID=").append(emiPlanID).append(", channelId=").append(channelId).append(", channel=")
                .append(channel).append(", mmid=").append(StringUtils.isNotBlank(mmid)).append(", otp=")
                .append(StringUtils.isNotBlank(otp)).append(", instNetworkCode=").append(instNetworkCode)
                .append(", holderMobileNo=").append(holderMobileNo).append(", storeCardFlag=").append(storeCardFlag)
                .append(", addMoneyFlag=").append(addMoneyFlag).append(", savedCardType=").append(savedCardType)
                .append(", txnMde=").append(txnMde).append(", vpa=").append(vpa).append(", isIciciIDebit=")
                .append(isIciciIDebit).append(", passCode=").append(StringUtils.isNotBlank(passCode)).append(", mpin=")
                .append(StringUtils.isNotBlank(mpin)).append(", deviceId=").append(deviceId)
                .append(", sequenceNumber=").append(sequenceNumber).append(", appId=").append(appId).append("]");
        return builder.toString();
    }

    private String maskedCardNumber(String cardNo) {
        if (StringUtils.isBlank(cardNo)) {
            return "N";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(cardNo.substring(0, 6));
        int i = 0;
        int numberOfXs = cardNo.length() - 10;
        while (i < numberOfXs) {
            sb.append("X");
            i++;
        }
        sb.append(cardNo.substring(cardNo.length() - 4));
        return sb.toString();
    }
}