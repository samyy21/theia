package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.enums.PassCodeType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 13/10/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassCodeValidationRequestBody extends RequestBody {

    private static final long serialVersionUID = 3508355745125956111L;
    @NotBlank
    private String passCode;
    @NotNull
    private PassCodeType passCodeType;

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public PassCodeType getPassCodeType() {
        return passCodeType;
    }

    public void setPassCodeType(PassCodeType passCodeType) {
        this.passCodeType = passCodeType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PassCodeValidationRequestBody [passCode=");
        builder.append(StringUtils.isNotBlank(passCode));
        builder.append(", passCodeType=");
        builder.append(passCodeType);
        builder.append("]");
        return builder.toString();
    }

}
