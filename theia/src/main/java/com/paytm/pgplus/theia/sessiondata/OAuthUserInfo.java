/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;
import java.util.List;

/**
 * @createdOn 03-Apr-2016
 * @author kesari
 */
public class OAuthUserInfo implements Serializable {

    private static final long serialVersionUID = -3712982781941515277L;

    @Tag(value = 1)
    private String userName;
    @Tag(value = 2)
    private String emailId;
    @Tag(value = 3)
    private String mobileNumber;
    @Tag(value = 4)
    private String userID;

    /** External user id */
    @Tag(value = 5)
    private String payerUserID;
    @Tag(value = 6)
    private String payerAccountNumber;
    @Tag(value = 7)
    private boolean isKYC;
    @Tag(value = 8)
    private String paytmToken;

    @Tag(value = 9)
    private List<String> userTypes;

    public String getPayerAccountNumber() {
        return payerAccountNumber;
    }

    public void setPayerAccountNumber(String payerAccountNumber) {
        this.payerAccountNumber = payerAccountNumber;
    }

    public String getPayerUserID() {
        return payerUserID;
    }

    public void setPayerUserID(String payerUserID) {
        this.payerUserID = payerUserID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    /**
     * @return the walletType
     */
    public boolean isKYC() {
        return isKYC;
    }

    /**
     * @param walletType
     *            the walletType to set
     */
    public void setKYC(boolean walletType) {
        this.isKYC = walletType;
    }

    public String getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(String paytmToken) {
        this.paytmToken = paytmToken;
    }

    public List<String> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<String> userTypes) {
        this.userTypes = userTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OAuthUserInfo [userName=").append(userName).append(", userID=").append(userID)
                .append(", payerUserID=").append(payerUserID).append(", payerAccountNumber=")
                .append(payerAccountNumber).append(", walletType=").append(isKYC).append(", paytmToken=")
                .append(paytmToken).append("]");
        return builder.toString();
    }

}
