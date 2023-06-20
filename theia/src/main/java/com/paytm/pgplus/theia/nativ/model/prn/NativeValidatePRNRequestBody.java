package com.paytm.pgplus.theia.nativ.model.prn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeValidatePRNRequestBody implements Serializable {

    private static final long serialVersionUID = 6004131756409833894L;

    @NotBlank
    private String mid;

    @NotBlank
    private String orderId;

    @NotBlank
    private String prnCode;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPrnCode() {
        return prnCode;
    }

    public void setPrnCode(String prnCode) {
        this.prnCode = prnCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeValidatePRNRequestBody{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", prnCode='").append(prnCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
