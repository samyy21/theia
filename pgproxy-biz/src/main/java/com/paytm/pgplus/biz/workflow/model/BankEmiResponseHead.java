package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BankEmiResponseHead implements Serializable {

    private static final long serialVersionUID = -3968475989121000034L;
    private String responseTimestamp;

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @Override
    public String toString() {
        return "BankEmiResponseHead{" + "responseTimestamp='" + responseTimestamp + '\'' + '}';
    }
}
