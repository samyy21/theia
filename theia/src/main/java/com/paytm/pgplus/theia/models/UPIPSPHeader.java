package com.paytm.pgplus.theia.models;

import javax.validation.constraints.Pattern;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Santosh chourasia
 *
 */
@JsonIgnoreProperties
public class UPIPSPHeader {

    private String clientId;
    @NotBlank(message = "RequestTimeStamp passed in Request is null ")
    private String requestTimestamp;
    @NotBlank(message = "version passed in the request is null")
    private String version;
    @NotBlank(message = "Request id passed in the request is null")
    @Length(max = 60, message = "Invalid length for orderID")
    @Pattern(regexp = "^[a-zA-Z0-9-|_@.-]*$", message = "Validation regex not matching for orderID")
    private String requestMsgId;
    @NotBlank(message = "signature passed in the request is null")
    private String signature;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestMsgId() {
        return requestMsgId;
    }

    public void setRequestMsgId(String requestMsgId) {
        this.requestMsgId = requestMsgId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
