package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.PaymentOfferDetails;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedVPA extends UPI {

    private static final long serialVersionUID = 4495509181109368213L;
    private VPADetails vpaDetails;

    public SavedVPA() {
    }

    public VPADetails getVpaDetails() {
        return this.vpaDetails;
    }

    public void setVpaDetails(VPADetails vpaDetails) {
        this.vpaDetails = vpaDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavedVPA{");
        sb.append("vpaDetails=").append(vpaDetails);
        sb.append('}');
        return sb.toString();
    }
}
