package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.facade.postpaid.model.FullTnCDetails;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.TwoFARespData;
import com.paytm.pgplus.response.BaseResponseBody;
import io.swagger.annotations.ApiModel;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceInfoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;

    @Ignore
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchAccountBalanceResponse.class);

    @JsonProperty("balanceInfo")
    private Money balanceInfo;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRedemptionAllowed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double redeemableInvestmentBalance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BankPartnerDetail> partnerBankBalances;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String investmentTnCUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TwoFARespData walletTwoFAConfig;

    public BalanceInfoResponseBody() {
    }

    public Money getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(Money balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getIsRedemptionAllowed() {
        return isRedemptionAllowed;
    }

    public void setIsRedemptionAllowed(Boolean redemptionAllowed) {
        isRedemptionAllowed = redemptionAllowed;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getRedeemableInvestmentBalance() {
        return redeemableInvestmentBalance;
    }

    public void setRedeemableInvestmentBalance(double redeemableInvestmentBalance) {
        this.redeemableInvestmentBalance = redeemableInvestmentBalance;
    }

    public List<BankPartnerDetail> getPartnerBankBalances() {
        return partnerBankBalances;
    }

    public void setPartnerBankBalances(List<BankPartnerDetail> partnerBankBalances) {
        this.partnerBankBalances = partnerBankBalances;
    }

    public void setPartnerBankBalances(String partnerBankBalances) {
        try {
            this.partnerBankBalances = JsonMapper.readValue(partnerBankBalances,
                    new TypeReference<List<BankPartnerDetail>>() {
                    });
        } catch (Exception e) {
            // Error in parsing partner bank balances
            LOGGER.error("Error in mapping partnerBank details:{}", e.getMessage());
        }
    }

    public String getInvestmentTnCUrl() {
        return investmentTnCUrl;
    }

    public void setInvestmentTnCUrl(String investmentTnCUrl) {
        this.investmentTnCUrl = investmentTnCUrl;
    }

    public TwoFARespData getWalletTwoFAConfig() {
        return walletTwoFAConfig;
    }

    public void setWalletTwoFAConfig(TwoFARespData walletTwoFAConfig) {
        this.walletTwoFAConfig = walletTwoFAConfig;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Data
    static class BankPartnerDetail {
        private double interestEarned;
        private double balance;
        private String name;

    }
}
