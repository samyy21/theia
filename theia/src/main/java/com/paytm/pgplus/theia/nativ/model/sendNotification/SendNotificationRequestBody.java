package com.paytm.pgplus.theia.nativ.model.sendNotification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SendNotificationRequestBody {

    @NotBlank
    @JsonProperty("mid")
    private String mid;
    @NotBlank
    @JsonProperty("orderId")
    private String orderId;
}
