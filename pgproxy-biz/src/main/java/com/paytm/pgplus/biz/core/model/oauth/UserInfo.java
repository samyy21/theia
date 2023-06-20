package com.paytm.pgplus.biz.core.model.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.Annotations.Mask;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 2990805590068214057L;
    private String email;
    @Mask(prefixNoMaskLen = 3, maskStr = "*", suffixNoMaskLen = 3)
    private String phone;
    private String countryCode;
    private String firstName;
    private String lastName;
    private String displayName;

}
