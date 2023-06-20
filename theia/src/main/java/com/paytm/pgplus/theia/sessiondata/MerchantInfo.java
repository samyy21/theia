/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author kesari
 *
 */
public class MerchantInfo implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2304263046544702331L;

    // Paytm MID
    @Tag(value = 1)
    private String mid;
    @Tag(value = 2)
    private String alipayMid;
    @Tag(value = 3)
    private String merchantName;
    @Tag(value = 4)
    private String merchantImage;

    // Added by naman
    @Tag(value = 5)
    private String merchantWalletType;

    /*
     * True if wallet status is other than active
     */
    @Tag(value = 6)
    private boolean merchantWalletFailed;
    @Tag(value = 7)
    private String merchantCategoryCode;
    @Tag(value = 8)
    private String internalMid;
    @Tag(value = 9)
    private int numberOfRetries;
    @Tag(value = 10)
    private boolean merchantStoreCardPref;

    private boolean useNewImagePath;

    public String getAlipayMid() {
        return alipayMid;
    }

    public void setAlipayMid(String alipayMid) {
        this.alipayMid = alipayMid;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public boolean isMerchantRetryEnabled() {
        return numberOfRetries > 0;
    }

    public String getMerchantWalletType() {
        return merchantWalletType;
    }

    public void setMerchantWalletType(String merchantWalletType) {
        this.merchantWalletType = merchantWalletType;
    }

    public boolean isMerchantWalletFailed() {
        return merchantWalletFailed;
    }

    public void setMerchantWalletFailed(boolean merchantWalletFailed) {
        this.merchantWalletFailed = merchantWalletFailed;
    }

    /**
     * @return the mid
     */
    public String getMid() {
        return mid;
    }

    /**
     * @param mid
     *            the mid to set
     */
    public void setMid(String mid) {
        this.mid = mid;
    }

    /**
     * @return the merchantName
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * @param merchantName
     *            the merchantName to set
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    /**
     * @return the merchantImage
     */
    public String getMerchantImage() {
        return merchantImage;
    }

    /**
     * @param merchantImage
     *            the merchantImage to set
     */
    public void setMerchantImage(String merchantImage) {
        this.merchantImage = merchantImage;
    }

    /**
     * @return the internalMid
     */
    public String getInternalMid() {
        return internalMid;
    }

    /**
     * @param internalMid
     *            the internalMid to set
     */
    public void setInternalMid(String internalMid) {
        this.internalMid = internalMid;
    }

    /**
     * @return the numberOfRetries
     */
    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    /**
     * @param numberOfRetries
     *            the numberOfRetries to set
     */
    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public boolean isMerchantStoreCardPref() {
        return merchantStoreCardPref;
    }

    public void setMerchantStoreCardPref(boolean merchantStoreCardPref) {
        this.merchantStoreCardPref = merchantStoreCardPref;
    }

    public boolean isUseNewImagePath() {
        return useNewImagePath;
    }

    public void setUseNewImagePath(boolean useNewImagePath) {
        this.useNewImagePath = useNewImagePath;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantInfo [mid=").append(mid).append(", alipayMid=").append(alipayMid)
                .append(", merchantName=").append(merchantName).append(", merchantImage=").append(merchantImage)
                .append(", merchantWalletType=").append(merchantWalletType).append(", merchantWalletFailed=")
                .append(merchantWalletFailed).append(", merchantCategoryCode=").append(merchantCategoryCode)
                .append(", internalMid=").append(internalMid).append(", numberOfRetries=").append(numberOfRetries)
                .append(", merchantStoreCardPref=").append(merchantStoreCardPref).append(", useNewImagePath=")
                .append(useNewImagePath).append("]");
        return builder.toString();
    }

}
