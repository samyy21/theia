package com.paytm.pgplus.theia.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

/**
 * @author Santosh chourasia
 *
 */
@JsonIgnoreProperties
public class UPIPSPRequest {

    @NotNull(message = "Header passed in the request is null")
    @Valid
    private UPIPSPHeader header;
    @NotNull(message = "Body passed in the request is null")
    @Valid
    private UPIPSPBody body;

    public UPIPSPHeader getHeader() {
        return header;
    }

    public void setHeader(UPIPSPHeader header) {
        this.header = header;
    }

    public UPIPSPBody getBody() {
        return body;
    }

    public void setBody(UPIPSPBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
