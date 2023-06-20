package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Map;

/**
 * @author charu
 *
 */

public class ChannelAccount implements Serializable {

    private static final long serialVersionUID = 9143895429883471402L;

    private String accountNo;
    private String accountBalance;
    private boolean status;
    private String disableReason;
    // to be used not the account balance
    private String availableBalance;
    private Map<String, String> extendInfo;

    /* Available Loyalty points */
    private String availablePoints;

    /* Loyalty point Exchange Rate */
    private String exchangeRate;

    /* TemplateId of Merchant Gift Voucher */
    private String templateId;

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(String disableReason) {
        this.disableReason = disableReason;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(String availablePoints) {
        this.availablePoints = availablePoints;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelAccount{");
        sb.append("accountNo='").append(accountNo).append('\'');
        sb.append(", accountBalance='").append(accountBalance).append('\'');
        sb.append(", status=").append(status);
        sb.append(", disableReason='").append(disableReason).append('\'');
        sb.append(", availableBalance='").append(availableBalance).append('\'');
        sb.append(", extendInfo='").append(extendInfo).append('\'');
        sb.append(", availablePoints='").append(availablePoints).append('\'');
        sb.append(", exchangeRate='").append(exchangeRate).append('\'');
        sb.append(", templateId='").append(templateId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
