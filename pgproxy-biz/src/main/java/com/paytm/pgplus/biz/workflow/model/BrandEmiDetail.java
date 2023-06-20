package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.List;

public class BrandEmiDetail implements Serializable {

    private static final long serialVersionUID = -4286629941267795366L;

    private List<EmiChannelDetails> emiChannelDetails;

    @Override
    public String toString() {
        return "BrandEmiDetail{" + "emiChannelDetails=" + emiChannelDetails + '}';
    }

    public List<EmiChannelDetails> getEmiChannelDetails() {
        return emiChannelDetails;
    }

    public void setEmiChannelDetails(List<EmiChannelDetails> emiChannelDetails) {
        this.emiChannelDetails = emiChannelDetails;
    }

}
