package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by rahulverma on 23/8/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusInfo implements Serializable {

    private static final long serialVersionUID = -333026678786743652L;
    @ApiModelProperty(required = true)
    @NotBlank
    private String status;
    @LocaleField
    private String msg;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showDisabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String displayMsg;

    public StatusInfo(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Boolean getShowDisabled() {
        return showDisabled;
    }

    public void setShowDisabled(Boolean showDisabled) {
        this.showDisabled = showDisabled;
    }

    public String getDisplayMsg() {
        return displayMsg;
    }

    public void setDisplayMsg(String displayMsg) {
        this.displayMsg = displayMsg;
    }

    public StatusInfo() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatusInfo{");
        sb.append("status='").append(status).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
