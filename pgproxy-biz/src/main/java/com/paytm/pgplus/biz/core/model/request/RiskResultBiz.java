package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class RiskResultBiz implements Serializable {

    private static final long serialVersionUID = 20L;

    private String result;

    private String verificationMethod;

    private String verificationPriority;

    private String riskInfo;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationPriority() {
        return verificationPriority;
    }

    public void setVerificationPriority(String verificationPriority) {
        this.verificationPriority = verificationPriority;
    }

    public String getRiskInfo() {
        return riskInfo;
    }

    public void setRiskInfo(String riskInfo) {
        this.riskInfo = riskInfo;
    }

}
