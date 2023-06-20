package com.paytm.pgplus.theia.nativ.model.vpaValidate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpaValidateRequestBody implements Serializable {

    private static final long serialVersionUID = -92857242841762474L;

    private String vpa;

    private String mid;

    private String queryParams; // setting this after request is received on
                                // controller

    private String referenceId;

    private String phoneNo;

    private String numericId;

    public VpaValidateRequestBody() {

    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonIgnore
    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getNumericId() {
        return numericId;
    }

    public void setNumericId(String numericId) {
        this.numericId = numericId;
    }

    @Override
    public String toString() {
        return "VpaValidateRequestBody{" + "vpa='" + vpa + '\'' + ", numericId='" + numericId + '\'' + ", mid='" + mid
                + '\'' + ", queryParams='" + queryParams + '\'' + '}';
    }
}