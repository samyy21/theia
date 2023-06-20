package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by rahulverma on 28/8/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostConvenienceFee implements Serializable {

    private static final long serialVersionUID = 1810733279710709700L;
    @ApiModelProperty(required = true)
    @NotBlank
    private String enabled;

    public PostConvenienceFee(String enabled) {
        this.enabled = enabled;
    }

    public PostConvenienceFee() {
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PostConvenienceFee{");
        sb.append("enabled='").append(enabled).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
