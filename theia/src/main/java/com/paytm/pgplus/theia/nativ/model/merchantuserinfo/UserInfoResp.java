package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.UserInfo;
import lombok.*;

import java.io.Serializable;

/**
 * Created by paraschawla on 29/3/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UserInfoResp implements Serializable {

    @JsonProperty("userInfo")
    private UserInfo userInfo;

}
