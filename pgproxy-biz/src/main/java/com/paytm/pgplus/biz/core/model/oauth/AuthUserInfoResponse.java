package com.paytm.pgplus.biz.core.model.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUserInfoResponse implements Serializable {

    private static final long serialVersionUID = -4246630474492229498L;
    private UserInfo basicInfo;
}
