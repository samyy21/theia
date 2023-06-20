package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

/**
 * Created by charu on 07/10/18.
 */

public class UserInfo implements Serializable {

    private static final long serialVersionUID = 9177982368144429218L;

    @JsonProperty("ssoToken")
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String ssoToken;

    public String getSsoToken() {
        return ssoToken;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
