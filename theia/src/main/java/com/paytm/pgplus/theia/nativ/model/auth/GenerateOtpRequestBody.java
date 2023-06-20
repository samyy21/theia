package com.paytm.pgplus.theia.nativ.model.auth;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateOtpRequestBody implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3799900277923580840L;

    @ApiModelProperty(notes = "Mobile Number is mandatory.")
    @NotBlank(message = "Mobile number is mandatory.")
    private String mobileNumber;

    private String autoReadHash;

    private String referenceId;

    @JsonIgnore
    private Boolean allowRegisteredUserOnly;

    public GenerateOtpRequestBody() {

    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAutoReadHash() {
        return autoReadHash;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Boolean getAllowRegisteredUserOnly() {
        return allowRegisteredUserOnly;
    }

    public void setAllowRegisteredUserOnly(Boolean allowRegisteredUserOnly) {
        this.allowRegisteredUserOnly = allowRegisteredUserOnly;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GenerateOtpRequestBody [mobileNumber=");
        builder.append(StringUtils.overlay(mobileNumber, "****", 0, 4));
        builder.append("]");
        return builder.toString();
    }

}