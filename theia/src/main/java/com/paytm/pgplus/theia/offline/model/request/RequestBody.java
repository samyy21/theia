package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.theia.offline.model.base.BaseBody;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestBody extends BaseBody {

    private static final long serialVersionUID = -1212122565857715997L;
    @ApiModelProperty(required = true)
    @NotNull
    private EChannelId channelId;

    @ApiModelProperty(required = true)
    private String deviceId;

    private String industryTypeId;

    public RequestBody() {
    }

    public EChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(EChannelId channelId) {
        this.channelId = channelId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIndustryTypeId() {
        return industryTypeId;
    }

    public void setIndustryTypeId(String industryTypeId) {
        this.industryTypeId = industryTypeId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestBody{");
        sb.append("channelId=").append(channelId);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", industryTypeId='").append(industryTypeId).append('\'');
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
