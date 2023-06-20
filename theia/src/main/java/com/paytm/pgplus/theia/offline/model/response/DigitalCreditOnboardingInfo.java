package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.postpaid.model.FullTnCDetails;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/**
 * This class contains details for the Postpaid onboarding user
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditOnboardingInfo implements Serializable {

    private static final long serialVersionUID = -4689529767302431316L;
    private String kycCode;
    private String kycVersion;
    private String kycSetName;
    private FullTnCDetails fullTnCDetails;
    private List<String> mictLines;

    public String getKycCode() {
        return kycCode;
    }

    public void setKycCode(String kycCode) {
        this.kycCode = kycCode;
    }

    public String getKycVersion() {
        return kycVersion;
    }

    public void setKycVersion(String kycVersion) {
        this.kycVersion = kycVersion;
    }

    public String getKycSetName() {
        return kycSetName;
    }

    public void setKycSetName(String kycSetName) {
        this.kycSetName = kycSetName;
    }

    public FullTnCDetails getFullTnCDetails() {
        return fullTnCDetails;
    }

    public void setFullTnCDetails(FullTnCDetails fullTnCDetails) {
        this.fullTnCDetails = fullTnCDetails;
    }

    public List<String> getMictLines() {
        return mictLines;
    }

    public void setMictLines(List<String> mictLines) {
        this.mictLines = mictLines;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
