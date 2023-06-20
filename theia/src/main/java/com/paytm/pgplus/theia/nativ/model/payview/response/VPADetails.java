package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class VPADetails implements Serializable {

    private static final long serialVersionUID = -7643826411056759958L;

    @ApiModelProperty(required = true)
    @NotBlank
    private String vpa;

    public VPADetails() {
    }

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VPADetails{");
        sb.append("vpa='").append(vpa).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
