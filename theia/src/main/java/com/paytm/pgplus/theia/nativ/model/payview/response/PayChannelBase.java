package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PayChannelBase implements Serializable {

    private static final long serialVersionUID = 4108302921078296280L;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonIgnore
    private String payMethod;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonIgnore
    private String payChannelOption;

    @Valid
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = StatusInfoFilter.class)
    private StatusInfo isDisabled;

    @Valid
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = StatusInfoFilter.class)
    private StatusInfo hasLowSuccess;

    @ApiModelProperty(required = true)
    @NotBlank
    private String iconUrl;

    @JsonIgnore
    private List<String> directServiceInsts;

    @JsonIgnore
    private List<String> supportAtmPins;

    @Valid
    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> supportedCardSubTypes;

    private String bankLogoUrl;

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PreAuthDetails> preAuthDetails;

    public PayChannelBase() {
    }

    public PayChannelBase(String payMethod, String payChannelOption, StatusInfo isDisabled, StatusInfo hasLowSuccess,
            String iconUrl) {
        this.payMethod = payMethod;
        this.payChannelOption = payChannelOption;
        this.isDisabled = isDisabled;
        this.hasLowSuccess = hasLowSuccess;
        this.iconUrl = iconUrl;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayChannelOption() {
        return payChannelOption;
    }

    public void setPayChannelOption(String payChannelOption) {
        this.payChannelOption = payChannelOption;
    }

    public StatusInfo getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(StatusInfo isDisabled) {
        this.isDisabled = isDisabled;
    }

    public StatusInfo getHasLowSuccess() {
        return hasLowSuccess;
    }

    public void setHasLowSuccess(StatusInfo hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getDirectServiceInsts() {
        return directServiceInsts;
    }

    public void setDirectServiceInsts(List<String> directServiceInsts) {
        this.directServiceInsts = directServiceInsts;
    }

    public List<String> getSupportAtmPins() {
        return supportAtmPins;
    }

    public void setSupportAtmPins(List<String> supportAtmPins) {
        this.supportAtmPins = supportAtmPins;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
    }

    public List<String> getSupportedCardSubTypes() {
        return supportedCardSubTypes;
    }

    public void setSupportedCardSubTypes(List<String> supportedCardSubTypes) {
        this.supportedCardSubTypes = supportedCardSubTypes;
    }

    public List<PreAuthDetails> getPreAuthDetails() {
        return preAuthDetails;
    }

    public void setPreAuthDetails(List<PreAuthDetails> preAuthDetails) {
        this.preAuthDetails = preAuthDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayChannelBase{");
        sb.append("payMethod='").append(payMethod).append('\'');
        sb.append(", payChannelOption='").append(payChannelOption).append('\'');
        sb.append(", isDisabled=").append(isDisabled);
        sb.append(", hasLowSuccess=").append(hasLowSuccess);
        sb.append(", directServiceInsts=").append(directServiceInsts);
        sb.append(", supportAtmPins=").append(supportAtmPins);
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append(", hybridDisabled='").append(hybridDisabled).append('\'');
        sb.append(", supportedCardSubTypes='").append(supportedCardSubTypes).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
