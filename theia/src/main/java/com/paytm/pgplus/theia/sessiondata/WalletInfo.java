/**
 *
 */
package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;
import java.util.List;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;

/**
 * @createdOn 28-Mar-2016
 * @author kesari
 */
public class WalletInfo implements Serializable {

    private static final long serialVersionUID = 4320942428882317534L;

    @Tag(value = 1)
    private boolean walletFailed;
    @Tag(value = 2)
    private boolean walletEnabled;
    @Tag(value = 3)
    private String walletType;
    @Tag(value = 4)
    private Double walletBalance;
    @Tag(value = 5)
    private boolean walletInactive;
    @Tag(value = 6)
    private String walletFailedMsg;
    @Tag(value = 7)
    private boolean walletOnly;
    @Tag(value = 8)
    private boolean areSubWalletsEnabled;
    @Tag(value = 9)
    private List<SubWalletInfo> subWalletDetails;
    @Tag(value = 10)
    private Double paytmWalletAmount;
    @Tag(value = 11)
    private Double giftVoucherAmount;
    @Tag(value = 12)
    private String displayName;

    public boolean isWalletEnabled() {
        return walletEnabled;
    }

    public void setWalletEnabled(boolean walletEnabled) {
        this.walletEnabled = walletEnabled;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public boolean isWalletFailed() {
        return walletFailed;
    }

    public void setWalletFailed(boolean walletFailed) {
        this.walletFailed = walletFailed;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    /**
     * @return the walletInactive
     */
    public boolean isWalletInactive() {
        return walletInactive;
    }

    /**
     * @param walletInactive
     *            the walletInactive to set
     */
    public void setWalletInactive(boolean walletInactive) {
        this.walletInactive = walletInactive;
    }

    /**
     * @return the walletFailedMsg
     */
    public String getWalletFailedMsg() {
        return walletFailedMsg;
    }

    /**
     * @param walletFailedMsg
     *            the walletFailedMsg to set
     */
    public void setWalletFailedMsg(String walletFailedMsg) {
        this.walletFailedMsg = walletFailedMsg;
    }

    /**
     * @return the walletOnly
     */
    public boolean isWalletOnly() {
        return walletOnly;
    }

    /**
     * @param walletOnly
     *            the walletOnly to set
     */
    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    /**
     * @return the areSubWalletsEnabled
     */
    public boolean isAreSubWalletsEnabled() {
        return areSubWalletsEnabled;
    }

    /**
     * @param areSubWalletsEnabled
     *            the areSubWalletsEnabled to set
     */
    public void setAreSubWalletsEnabled(boolean areSubWalletsEnabled) {
        this.areSubWalletsEnabled = areSubWalletsEnabled;
    }

    /**
     * @return the subWalletDetails
     */
    public List<SubWalletInfo> getSubWalletDetails() {
        return subWalletDetails;
    }

    /**
     * @param subWalletDetails
     *            the subWalletDetails to set
     */
    public void setSubWalletDetails(List<SubWalletInfo> subWalletDetails) {
        this.subWalletDetails = subWalletDetails;
    }

    public Double getPaytmWalletAmount() {
        return paytmWalletAmount;
    }

    public void setPaytmWalletAmount(Double paytmWalletAmount) {
        this.paytmWalletAmount = paytmWalletAmount;
    }

    public Double getGiftVoucherAmount() {
        return giftVoucherAmount;
    }

    public void setGiftVoucherAmount(Double giftVoucherAmount) {
        this.giftVoucherAmount = giftVoucherAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WalletInfo [walletFailed=").append(walletFailed).append(", walletEnabled=")
                .append(walletEnabled).append(", walletType=").append(walletType).append(", walletBalance=")
                .append(walletBalance).append(", walletInactive=").append(walletInactive).append(", walletFailedMsg=")
                .append(walletFailedMsg).append(", walletOnly=").append(walletOnly).append(", areSubWalletsEnabled=")
                .append(areSubWalletsEnabled).append(", subWalletDetails=").append(subWalletDetails)
                .append(", paytmWalletAmount=").append(paytmWalletAmount).append(", giftVoucherAmount=")
                .append(giftVoucherAmount).append(", displayName=").append(displayName).append("]");
        return builder.toString();
    }

}
