package com.paytm.pgplus.theia.nativ.model.sendNotification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import lombok.*;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SendNotificationResponse implements Serializable {

    @JsonProperty(value = "head")
    private ResponseHeader head;

    @JsonProperty(value = "body")
    private SendNotificationResponseBody body;

}
