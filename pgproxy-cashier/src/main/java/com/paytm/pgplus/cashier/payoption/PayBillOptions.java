/**
 *
 */
package com.paytm.pgplus.cashier.payoption;

import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.facade.enums.PayMethod;

/**
 * @author amit.dubey
 *
 */
public class PayBillOptions implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = -8906556161229004896L;

    private Long walletBalance;

    @NotNull(message = "{notnull}")
    @Min(100L)
    private Long serviceAmount;

    @NotNull(message = "{notnull}")
    private Long chargeFeeAmount;

    private boolean topupAndPay;

    @NotNull(message = "{notnull}")
    private Map<PayMethod, String> payOptions;

    @NotEmpty(message = "{notnull}")
    private Map<String, String> channelInfo;

    @NotEmpty(message = "{notnull}")
    private Map<String, String> extendInfo;

    private String payerAccountNo;
    private String cardCacheToken;
    private boolean saveChannelInfoAfterPay;
    private String issuingCountry;
    private String maskedCardNo;
    private String cardIndexNo;

    public PayBillOptions(PayBillOptionsBuilder builder) {
        this.walletBalance = builder.walletBalance;
        this.serviceAmount = builder.serviceAmount;
        this.chargeFeeAmount = builder.chargeFeeAmount;
        this.payOptions = builder.payOptions;
        this.topupAndPay = builder.topupAndPay;
        this.payerAccountNo = builder.payerAccountNo;
        this.cardCacheToken = builder.cardCacheToken;
        this.saveChannelInfoAfterPay = builder.saveChannelInfoAfterPay;
        this.channelInfo = builder.channelInfo;
        this.extendInfo = builder.extendInfo;
        this.issuingCountry = builder.issuingCountry;
    }

    /**
     * @return the walletAmount
     */
    public Long getWalletBalance() {
        return walletBalance;
    }

    /**
     * @return the serviceAmount
     */
    public Long getServiceAmount() {
        return serviceAmount;
    }

    /**
     * @return the chargeFeeAmount
     */
    public Long getChargeFeeAmount() {
        return chargeFeeAmount;
    }

    public void setChargeFeeAmount(Long chargeFeeAmount) {
        this.chargeFeeAmount = chargeFeeAmount;
    }

    /**
     * @return the payOptions
     */
    public Map<PayMethod, String> getPayOptions() {
        return payOptions;
    }

    /**
     * @return the payerAccountNo
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * @return the cardCacheToken
     */
    public String getCardCacheToken() {
        return cardCacheToken;
    }

    public void setCardCacheToken(String cardCacheToken) {
        this.cardCacheToken = cardCacheToken;
    }

    /**
     * @return the channelInfo
     */
    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * @return the topupAndPay
     */
    public boolean isTopupAndPay() {
        return topupAndPay;
    }

    /**
     * @return the saveChannelInfoAfterPay
     */
    public boolean isSaveChannelInfoAfterPay() {
        return saveChannelInfoAfterPay;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }

    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    public void setMaskedCardNo(String maskedCardNo) {
        this.maskedCardNo = maskedCardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public static class PayBillOptionsBuilder {
        private Long walletBalance;
        private Long serviceAmount;
        private Long chargeFeeAmount;
        private Map<PayMethod, String> payOptions;
        private boolean topupAndPay;
        private String payerAccountNo;
        private String cardCacheToken;
        private boolean saveChannelInfoAfterPay;
        private Map<String, String> channelInfo;
        private Map<String, String> extendInfo;
        private String issuingCountry;

        public PayBillOptionsBuilder(Long serviceAmount, Long chargeFeeAmount, Map<PayMethod, String> payOptions)
                throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(serviceAmount, "serviceAmount");
            this.serviceAmount = serviceAmount;

            BeanParameterValidator.validateInputObjectParam(chargeFeeAmount, "chargeFeeAmount");
            this.chargeFeeAmount = chargeFeeAmount;

            BeanParameterValidator.validateInputMapParam(payOptions, "payOptions");
            this.payOptions = payOptions;
        }

        public PayBillOptionsBuilder setWalletBalance(Long walletBalance) throws CashierCheckedException {
            this.walletBalance = walletBalance;
            return this;
        }

        public PayBillOptionsBuilder setPayerAccountNumber(String payerAccountNo) throws CashierCheckedException {
            this.payerAccountNo = payerAccountNo;
            return this;
        }

        public PayBillOptionsBuilder setTopAndPay(boolean topupAndPay) throws CashierCheckedException {
            this.topupAndPay = topupAndPay;
            return this;
        }

        public PayBillOptionsBuilder setCacheCardToken(String cardCacheToken) throws CashierCheckedException {
            this.cardCacheToken = cardCacheToken;
            return this;
        }

        public PayBillOptionsBuilder setSaveChannelInfoAfterPay(boolean saveChannelInfoAfterPay)
                throws CashierCheckedException {
            this.saveChannelInfoAfterPay = saveChannelInfoAfterPay;
            return this;
        }

        public PayBillOptionsBuilder setChannelInfo(Map<String, String> channelInfo) throws CashierCheckedException {
            this.channelInfo = channelInfo;
            return this;
        }

        public PayBillOptionsBuilder setExtendInfo(Map<String, String> extendInfo) throws CashierCheckedException {
            this.extendInfo = extendInfo;
            return this;
        }

        public PayBillOptionsBuilder setIssuingCountry(final String issuingCountry) throws CashierCheckedException {
            this.issuingCountry = issuingCountry;
            return this;
        }

        public PayBillOptions build() {
            return new PayBillOptions(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PayBillOptions [walletBalance=").append(walletBalance).append(", serviceAmount=")
                .append(serviceAmount).append(", chargeFeeAmount=").append(chargeFeeAmount).append(", topupAndPay=")
                .append(topupAndPay).append(", payOptions=").append(payOptions).append(", payerAccountNo=")
                .append(payerAccountNo).append(", cardCacheToken=").append(cardCacheToken)
                .append(", saveChannelInfoAfterPay=").append(saveChannelInfoAfterPay).append(", channelInfo=")
                .append(channelInfo).append(", extendInfo=").append(extendInfo).append(", issuingCountry=")
                .append(issuingCountry).append("]");
        return builder.toString();
    }

}