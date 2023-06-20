package com.paytm.pgplus.theia.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

/**
 * @author kartik
 * @date 05-07-2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelTransRequest implements Serializable {

    private static final long serialVersionUID = 925165912937044348L;

    @JsonProperty("MID")
    private String merchantId;

    @JsonProperty("ORDER_ID")
    private String orderId;

    @Mask
    @JsonProperty("USER_TOKEN")
    private String userToken;

    @JsonProperty("IS_FORCE_CLOSE")
    private boolean isForceClose;

    public CancelTransRequest() {
    }

    public CancelTransRequest(String merchantId, String orderId, String userToken, boolean isForceClose) {
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.userToken = userToken;
        this.isForceClose = isForceClose;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserToken() {
        return userToken;
    }

    public boolean isForceClose() {
        return isForceClose;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
