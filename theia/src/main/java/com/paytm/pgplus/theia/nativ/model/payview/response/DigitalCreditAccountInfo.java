package com.paytm.pgplus.theia.nativ.model.payview.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.postpaid.model.FullTnCDetails;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.DetailedBalanceInfo;
import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;

public class DigitalCreditAccountInfo extends AccountInfo {

    private static final long serialVersionUID = -8602562123768081864L;
    private Map<String, String> extendInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("passcodeRequired")
    private Boolean passcodeRequired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @LocaleField
    private String displayMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String infoButtonMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("mictLines")
    @LocaleField
    private List<String> mictLines;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("fullTnCDetails")
    private FullTnCDetails fullTnCDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("enable")
    private Boolean enable;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String kycCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String kycVersion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DetailedBalanceInfo> detailedBalanceInfos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String availablePoints;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String exchangeRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Money monthlySanctionLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Money monthlyAvailableSanctionLimit;

    public Boolean getPasscodeRequired() {
        return passcodeRequired;
    }

    public void setPasscodeRequired(Boolean passcodeRequired) {
        this.passcodeRequired = passcodeRequired;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getInfoButtonMessage() {
        return infoButtonMessage;
    }

    public void setInfoButtonMessage(String infoButtonMessage) {
        this.infoButtonMessage = infoButtonMessage;
    }

    public List<String> getMictLines() {
        return mictLines;
    }

    public void setMictLines(List<String> mictLines) {
        this.mictLines = mictLines;
    }

    public FullTnCDetails getFullTnCDetails() {
        return fullTnCDetails;
    }

    public void setFullTnCDetails(FullTnCDetails fullTnCDetails) {
        this.fullTnCDetails = fullTnCDetails;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getKycCode() {
        return kycCode;
    }

    public void setKycCode(String kycCode) {
        this.kycCode = kycCode;
    }

    public String getKycVersion() {
        return kycVersion;
    }

    public void setKycVersion(String kycVersion) {
        this.kycVersion = kycVersion;
    }

    public List<DetailedBalanceInfo> getDetailedBalanceInfos() {
        return detailedBalanceInfos;
    }

    public void setDetailedBalanceInfos(List<DetailedBalanceInfo> detailedBalanceInfos) {
        this.detailedBalanceInfos = detailedBalanceInfos;
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

    public Money getMonthlySanctionLimit() {
        return monthlySanctionLimit;
    }

    public void setMonthlySanctionLimit(Money monthlySanctionLimit) {
        this.monthlySanctionLimit = monthlySanctionLimit;
    }

    public Money getMonthlyAvailableSanctionLimit() {
        return monthlyAvailableSanctionLimit;
    }

    public void setMonthlyAvailableSanctionLimit(Money monthlyAvailableSanctionLimit) {
        this.monthlyAvailableSanctionLimit = monthlyAvailableSanctionLimit;
    }

    public DigitalCreditAccountInfo(String payerAccountNo, Money accountBalance, String extendInfo) {
        super(payerAccountNo, accountBalance);
        if (StringUtils.isEmpty(extendInfo))
            return;
        try {
            this.extendInfo = (Map<String, String>) JsonMapper.mapJsonToObject(extendInfo, Map.class);
        } catch (FacadeCheckedException e) {

        }
    }

    public DigitalCreditAccountInfo(String payerAccountNo, Money accountBalance, Map<String, String> extendInfo) {
        super(payerAccountNo, accountBalance);
        if (extendInfo == null) {
            return;
        }
        this.extendInfo = extendInfo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalCreditBalanceInfo{");
        sb.append("extendInfo=").append(extendInfo);
        sb.append('}');
        return sb.toString();
    }
}
