package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PayMethod implements Serializable {

    private static final long serialVersionUID = 4853871828367608617L;

    @ApiModelProperty(required = true)
    @NotBlank
    private String payMethod;

    @ApiModelProperty(required = true)
    @NotBlank
    private String displayName;

    @Valid
    private StatusInfo isDisabled;

    @Valid
    private List<PayChannelBase> payChannelOptions;

    private int priority;

    private boolean isOnboarding;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCardSupported;

    public PayMethod() {
    }

    public String getPayMethod() {
        return this.payMethod;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public StatusInfo getIsDisabled() {
        return this.isDisabled;
    }

    public List<PayChannelBase> getPayChannelOptions() {
        return this.payChannelOptions;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setIsDisabled(StatusInfo isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setPayChannelOptions(List<PayChannelBase> payChannelOptions) {
        this.payChannelOptions = payChannelOptions;
    }

    public boolean isOnboarding() {
        return isOnboarding;
    }

    public void setOnboarding(boolean onboarding) {
        isOnboarding = onboarding;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayMethod{");
        sb.append("payMethod='").append(payMethod).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", isDisabled=").append(isDisabled);
        sb.append(", payChannelOptions=").append(payChannelOptions);
        sb.append(", isOnboarding=").append(isOnboarding);
        sb.append('}');
        return sb.toString();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Boolean getPrepaidCardSupported() {
        return prepaidCardSupported;
    }

    public void setPrepaidCardSupported(Boolean prepaidCardSupported) {
        this.prepaidCardSupported = prepaidCardSupported;
    }
}
