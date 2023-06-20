package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.core.model.ActiveSubscriptionBeanBiz;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class PayOption implements Serializable {

    private final static long serialVersionUID = -7901232103779768471L;

    @JsonProperty("paymentModes")
    private List<PayMethod> payMethods = null;

    @JsonProperty("savedInstruments")
    private List<PayChannelBase> savedInstruments = null;

    @JsonProperty("userProfileSarvatra")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1Filter.class)
    private UserProfileSarvatra userProfileSarvatra;

    @JsonProperty("activeSubscriptions")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1NullFilter.class)
    private List<ActiveSubscriptionBeanBiz> activeSubscriptions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("upiProfile")
    private UserProfileSarvatraV4 upiProfileV4;

    private List<SavedMandateBank> savedMandateBanks;

    public PayOption() {
        super();
    }

    public PayOption(List<PayMethod> paymentModes) {
        super();
        this.payMethods = paymentModes;
    }

    public List<PayMethod> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<PayMethod> payMethods) {
        this.payMethods = payMethods;
    }

    public List<PayChannelBase> getSavedInstruments() {
        return savedInstruments;
    }

    public void setSavedInstruments(List<PayChannelBase> savedInstruments) {
        this.savedInstruments = savedInstruments;
    }

    public UserProfileSarvatra getUserProfileSarvatra() {
        return userProfileSarvatra;
    }

    public void setUserProfileSarvatra(UserProfileSarvatra userProfileSarvatra) {
        this.userProfileSarvatra = userProfileSarvatra;
    }

    public List<ActiveSubscriptionBeanBiz> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(List<ActiveSubscriptionBeanBiz> activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public UserProfileSarvatraV4 getUpiProfileV4() {
        return upiProfileV4;
    }

    public void setUpiProfileV4(UserProfileSarvatraV4 upiProfileV4) {
        this.upiProfileV4 = upiProfileV4;
    }

    public List<SavedMandateBank> getSavedMandateBanks() {
        return savedMandateBanks;
    }

    public void setSavedMandateBanks(List<SavedMandateBank> savedMandateBanks) {
        this.savedMandateBanks = savedMandateBanks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("paymentModes", payMethods)
                .append("savedPayInstruments", savedInstruments).append("activeSubscriptions", activeSubscriptions)
                .append("userProfileSarvatra", userProfileSarvatra).append("savedMandateBanks", savedMandateBanks)
                .toString();
    }

}
