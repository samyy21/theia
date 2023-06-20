package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.facade.postpaid.model.FullTnCDetails;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponseBody;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.DetailedBalanceInfo;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PPBLAccountInfo extends AccountInfo {

    @LocaleField
    private String displayMessage;

    private String accountStatus;

    private Boolean isRedemptionAllowed;

    private Double redeemableInvestmentBalance;

    private String investmentTnCUrl;

    private List<PPBLAccountInfo.BankPartnerDetail> partnerBankBalances;

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
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

    public List<PPBLAccountInfo.BankPartnerDetail> getPartnerBankBalances() {
        return partnerBankBalances;
    }

    public void setPartnerBankBalances(List<PPBLAccountInfo.BankPartnerDetail> partnerBankBalances) {
        this.partnerBankBalances = partnerBankBalances;
    }

    public void setPartnerBankBalances(String partnerBankBalances) {
        try {
            this.partnerBankBalances = JsonMapper.readValue(partnerBankBalances,
                    new TypeReference<List<PPBLAccountInfo.BankPartnerDetail>>() {
                    });
        } catch (IOException e) {
        }
    }

    public String getInvestmentTnCUrl() {
        return investmentTnCUrl;
    }

    public void setInvestmentTnCUrl(String investmentTnCUrl) {
        this.investmentTnCUrl = investmentTnCUrl;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Data
    static class BankPartnerDetail implements Serializable {
        private static final long serialVersionUID = 8569028972045477998L;
        private double interestEarned;
        private double balance;
        private String name;

    }

}
