package com.paytm.pgplus.theia.nativ.model.sendNotification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.notification.SendLinkNotificationRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenTypeRequestHeader;
import lombok.*;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SendNotificationRequest implements Serializable {

    private TokenTypeRequestHeader head;

    private SendNotificationRequestBody body;

}
