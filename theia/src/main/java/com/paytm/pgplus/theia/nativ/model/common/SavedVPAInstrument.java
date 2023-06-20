package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.theia.nativ.enums.BankDisplayNames;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedVPAInstrument implements Serializable {

    private static final long serialVersionUID = -2181218249208584700L;

    private String id;

    private String vpaId;

    @LocaleField(localeEnum = BankDisplayNames.class, methodName = "getBankName")
    private String bankName;

    private String bankAccountNo;

    private SarvatraVpaDetails payeeData;

    private String type;

    private String authMode;

    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    @LocaleField
    private String displayName;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

    private boolean selected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UpiBankAccountV4 payeeDataV2;

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    private String bankLogoUrl;

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVpaId() {
        return vpaId;
    }

    public void setVpaId(String vpaId) {
        this.vpaId = vpaId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public SarvatraVpaDetails getPayeeData() {
        return payeeData;
    }

    public void setPayeeData(SarvatraVpaDetails payeeData) {
        this.payeeData = payeeData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public UpiBankAccountV4 getPayeeDataV2() {
        return payeeDataV2;
    }

    public void setPayeeDataV2(UpiBankAccountV4 payeeDataV2) {
        this.payeeDataV2 = payeeDataV2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavedVPAInstrument{");
        sb.append("id='").append(id).append('\'');
        sb.append("vpaId='").append(vpaId).append('\'');
        sb.append(", bankName='").append(bankName).append('\'');
        sb.append(", bankAccountNo='").append(bankAccountNo).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", authMode='").append(authMode).append('\'');
        sb.append(", hybridDisabled='").append(hybridDisabled).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", selected='").append(selected).append('\'');
        sb.append(", payeeDataV2='").append(payeeDataV2).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
