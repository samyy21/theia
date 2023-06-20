package com.paytm.pgplus.theia.accesstoken.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.enums.ProductType;
import com.paytm.pgplus.models.UserInfo;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccessTokenRequestBody implements Serializable {

    private final static long serialVersionUID = 443664366787252151L;

    @JsonProperty(value = "mid")
    private String mid;

    @JsonProperty(value = "referenceId")
    private String referenceId;

    /*
     * ssoToken is not mandatory Used in FPO - tokenType CHECKSUM and ACCESS
     */
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    @JsonProperty(value = "paytmSsoToken")
    private String paytmSsoToken;

    private String custId;

    private UserInfo userInfo;

    private String cardPreAuthType;

    private String preAuthBlockSeconds;

    private ProductType productType;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPaytmSsoToken() {
        return paytmSsoToken;
    }

    public void setPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getCardPreAuthType() {
        return cardPreAuthType;
    }

    public void setCardPreAuthType(String cardPreAuthType) {
        this.cardPreAuthType = cardPreAuthType;
    }

    public String getPreAuthBlockSeconds() {
        return preAuthBlockSeconds;
    }

    public void setPreAuthBlockSeconds(String preAuthBlockSeconds) {
        this.preAuthBlockSeconds = preAuthBlockSeconds;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
}
