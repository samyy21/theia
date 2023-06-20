/**
 *
 */
package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;
import java.math.BigDecimal;

import com.dyuproject.protostuff.Tag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author vaishakhnair
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubWalletInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1868761235226193149L;

    @Tag(value = 1)
    private BigDecimal subWalletBalance;
    @Tag(value = 2)
    private String displayMessage;
    @Tag(value = 3)
    private String expiryDate;
    @Tag(value = 4)
    private String issuerMetadata;
    @Deprecated
    private String imageUrl;
    @Tag(value = 5)
    private String id;
    @Tag(value = 6)
    private String walletType;
    @Tag(value = 7)
    private String subWalletType;
    @Tag(value = 8)
    private String webLogo;
    @Tag(value = 9)
    private String wapLogo;
    @Tag(value = 10)
    private String status;

    /**
     * @return the subWalletBalance
     */
    public BigDecimal getSubWalletBalance() {
        return subWalletBalance;
    }

    /**
     * @param subWalletBalance
     *            the subWalletBalance to set
     */
    public void setSubWalletBalance(BigDecimal subWalletBalance) {
        this.subWalletBalance = subWalletBalance;
    }

    /**
     * @return the displayMessage
     */
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * @param displayMessage
     *            the displayMessage to set
     */
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    /**
     * @return the expiryDate
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * @param expiryDate
     *            the expiryDate to set
     */
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return the issuerMetadata
     */
    public String getIssuerMetadata() {
        return issuerMetadata;
    }

    /**
     * @param issuerMetadata
     *            the issuerMetadata to set
     */
    public void setIssuerMetadata(String issuerMetadata) {
        this.issuerMetadata = issuerMetadata;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the walletType
     */
    public String getWalletType() {
        return walletType;
    }

    /**
     * @param walletType
     *            the walletType to set
     */
    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    /**
     * @return the subWalletType
     */
    public String getSubWalletType() {
        return subWalletType;
    }

    /**
     * @param subWalletType
     *            the subWalletType to set
     */
    public void setSubWalletType(String subWalletType) {
        this.subWalletType = subWalletType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubWalletInfo [subWalletBalance=").append(subWalletBalance).append(", displayMessage=")
                .append(displayMessage).append(", expiryDate=").append(expiryDate).append(", issuerMetadata=")
                .append(issuerMetadata).append(", imageUrl=").append(imageUrl).append(", id=").append(id)
                .append(", walletType=").append(walletType).append(", subWalletType=").append(subWalletType)
                .append(", webLogo=").append(webLogo).append(", wapLogo=").append(wapLogo).append("]");
        return builder.toString();
    }

    /**
     * @return the webLogo
     */
    public String getWebLogo() {
        return webLogo;
    }

    /**
     * @param webLogo
     *            the webLogo to set
     */
    public void setWebLogo(String webLogo) {
        this.webLogo = webLogo;
    }

    /**
     * @return the wapLogo
     */
    public String getWapLogo() {
        return wapLogo;
    }

    /**
     * @param wapLogo
     *            the wapLogo to set
     */
    public void setWapLogo(String wapLogo) {
        this.wapLogo = wapLogo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
