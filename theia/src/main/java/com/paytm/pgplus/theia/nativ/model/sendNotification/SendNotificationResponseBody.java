package com.paytm.pgplus.theia.nativ.model.sendNotification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResultInfo;
import lombok.*;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SendNotificationResponseBody implements Serializable {

    @JsonProperty(value = "resultInfo")
    private ResultInfo resultInfo;

    @JsonProperty(value = "messageSent")
    private String messageSent;

    @JsonProperty(value = "notificationSent")
    private String notificationSent;

    @JsonProperty(value = "pageTimeout")
    private String pageTimeout;
}
