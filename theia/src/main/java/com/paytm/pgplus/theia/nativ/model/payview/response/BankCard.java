package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.offline.enums.CountryCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankCard extends Bank {

    private static final long serialVersionUID = -2520625954321267087L;

    @ApiModelProperty(required = true)
    @NotEmpty
    @JsonIgnore
    private List<CountryCode> supportedCountries;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean oneClickSupported;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCardSupported;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean corporateCard;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> dccServiceInstIds;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PreAuthDetails> preAuthDetails;

    public Boolean getOneClickSupported() {
        return oneClickSupported;
    }

    public void setOneClickSupported(Boolean oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
    }

    public BankCard(List<CountryCode> supportedCountries) {
        this.supportedCountries = supportedCountries;
    }

    public BankCard() {
    }

    public List<CountryCode> getSupportedCountries() {
        return supportedCountries;
    }

    public void setSupportedCountries(List<CountryCode> supportedCountries) {
        this.supportedCountries = supportedCountries;
    }

    public Boolean isPrepaidCardSupported() {
        return prepaidCardSupported;
    }

    public void setPrepaidCardSupported(Boolean prepaidCardSupported) {
        this.prepaidCardSupported = prepaidCardSupported;
    }

    public List<String> getDccServiceInstIds() {
        return dccServiceInstIds;
    }

    public void setDccServiceInstIds(List<String> dccServiceInstIds) {
        this.dccServiceInstIds = dccServiceInstIds;
    }

    public Boolean getCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(Boolean corporateCard) {
        this.corporateCard = corporateCard;
    }

    public List<PreAuthDetails> getPreAuthDetails() {
        return preAuthDetails;
    }

    public void setPreAuthDetails(List<PreAuthDetails> preAuthDetails) {
        this.preAuthDetails = preAuthDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BankCard{");
        sb.append("supportedCountries=").append(supportedCountries);
        sb.append(", oneClickSupported=").append(oneClickSupported);
        sb.append(", dccServiceInstIds=").append(dccServiceInstIds);
        sb.append(", corporateCard=").append(corporateCard);
        sb.append(",preAuthDetails=").append(preAuthDetails);
        sb.append('}');
        return sb.toString();
    }
}
