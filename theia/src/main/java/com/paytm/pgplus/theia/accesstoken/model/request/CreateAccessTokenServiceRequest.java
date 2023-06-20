package com.paytm.pgplus.theia.accesstoken.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.enums.ProductType;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccessTokenServiceRequest implements Serializable {

    private static final long serialVersionUID = 8528612697025628635L;

    private String mid;
    private String referenceId;

    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String paytmSsoToken;

    private String custId;

    private NativePersistData nativePersistData;

    private UserInfo userInfo;

    private EPreAuthType preAuthType;

    private Long preAuthBlockSeconds;

    private ProductType productType;

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

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

    public void setPaytmSsoToken(String paytmSSoToken) {
        this.paytmSsoToken = paytmSSoToken;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public NativePersistData getNativePersistData() {
        return nativePersistData;
    }

    public void setNativePersistData(NativePersistData nativePersistData) {
        this.nativePersistData = nativePersistData;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public EPreAuthType getPreAuthType() {
        return preAuthType;
    }

    public void setPreAuthType(EPreAuthType preAuthType) {
        this.preAuthType = preAuthType;
    }

    public Long getPreAuthBlockSeconds() {
        return preAuthBlockSeconds;
    }

    public void setPreAuthBlockSeconds(Long preAuthBlockSeconds) {
        this.preAuthBlockSeconds = preAuthBlockSeconds;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
