package com.paytm.pgplus.theia.models.response;

import java.io.Serializable;
import java.util.List;

import com.paytm.pgplus.theia.models.SuccessRateBean;

/**
 * @author kartik
 * @date 09-03-2017
 */
public class SuccessRateQueryResponseBean implements Serializable {

    private static final long serialVersionUID = 1118779668696148544L;

    private List<SuccessRateBean> successRates;

    public SuccessRateQueryResponseBean(List<SuccessRateBean> successRates) {
        this.successRates = successRates;
    }

    public List<SuccessRateBean> getSuccessRates() {
        return successRates;
    }

    public void setSuccessRates(List<SuccessRateBean> successRates) {
        this.successRates = successRates;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SuccessRateQueryResponseBean [successRates=");
        builder.append(successRates);
        builder.append("]");
        return builder.toString();
    }

}
