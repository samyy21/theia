package com.paytm.pgplus.biz.core.risk;

import java.io.Serializable;

public class RiskOAuthValidatedData implements Serializable {

    private static final long serialVersionUID = -1088395577072466321L;

    private String stateCode;

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
}
