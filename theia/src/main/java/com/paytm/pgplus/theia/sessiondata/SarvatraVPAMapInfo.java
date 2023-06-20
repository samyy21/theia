package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;
import java.util.Map;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.theia.models.MerchantDetails;

/**
 * @author vivek kumar updated by santosh chourasia
 * @date 19-Dec-2017
 */
public class SarvatraVPAMapInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Tag(value = 1)
    private Map<String, String> sarvatraVpaMapInfo;
    @Tag(value = 2)
    private MerchantDetails merchantDetails;
    @Tag(value = 3)
    private UserProfileSarvatra userProfileSarvatra;
    @Tag(value = 4)
    private Map<String, PaytmBanksVpaDefaultDebitCredit> bankInfo;
    @Tag(value = 5)
    private MerchantDetails addMoneyMerchantDetails;

    public Map<String, String> getSarvatraVpaMapInfo() {
        return sarvatraVpaMapInfo;
    }

    public void setSarvatraVpaMapInfo(Map<String, String> sarvatraVpaMapInfo) {
        this.sarvatraVpaMapInfo = sarvatraVpaMapInfo;
    }

    public UserProfileSarvatra getUserProfileSarvatra() {
        return userProfileSarvatra;
    }

    public void setUserProfileSarvatra(UserProfileSarvatra userProfileSarvatra) {
        this.userProfileSarvatra = userProfileSarvatra;
    }

    public Map<String, PaytmBanksVpaDefaultDebitCredit> getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(Map<String, PaytmBanksVpaDefaultDebitCredit> bankInfo) {
        this.bankInfo = bankInfo;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
    }

    public MerchantDetails getAddMoneyMerchantDetails() {
        return addMoneyMerchantDetails;
    }

    public void setAddMoneyMerchantDetails(MerchantDetails addMoneyMerchantDetails) {
        this.addMoneyMerchantDetails = addMoneyMerchantDetails;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SarvatraVPAMapInfo [sarvatraVpaMapInfo=").append(sarvatraVpaMapInfo)
                .append(", userProfileSarvatra=").append(userProfileSarvatra).append(", bankInfo=").append(bankInfo)
                .append(", merchantDetails=").append(merchantDetails).append(", addMoneyMerchantDetails=")
                .append(addMoneyMerchantDetails).append("]");
        return builder.toString();
    }

}
