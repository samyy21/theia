package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private List<CountryCode> supportedCountries;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCardSupported;

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

    public Boolean getPrepaidCardSupported() {
        return prepaidCardSupported;
    }

    public void setPrepaidCardSupported(Boolean prepaidCardSupported) {
        this.prepaidCardSupported = prepaidCardSupported;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BankCard{");
        sb.append("supportedCountries=").append(supportedCountries);
        sb.append('}');
        return sb.toString();
    }
}
