/**
 * 
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class BizPayResponse implements Serializable {

    private static final long serialVersionUID = -6582773197681289410L;

    private String cashierRequestID;

    private SecurityPolicyResult securityPolicyResult;

    public String getCashierRequestID() {
        return cashierRequestID;
    }

    public void setCashierRequestID(String cashierRequestID) {
        this.cashierRequestID = cashierRequestID;
    }

    public BizPayResponse(String cashierRequestID) {
        this.cashierRequestID = cashierRequestID;
    }

    public BizPayResponse(String cashierRequestID, SecurityPolicyResult securityPolicyResult) {
        this.cashierRequestID = cashierRequestID;
        this.securityPolicyResult = securityPolicyResult;
    }

    public void setSecurityPolicyResult(SecurityPolicyResult securityPolicyResult) {
        this.securityPolicyResult = securityPolicyResult;
    }

    public SecurityPolicyResult getSecurityPolicyResult() {
        return securityPolicyResult;
    }

    @Override
    public String toString() {
        return "BizPayResponse{" + "cashierRequestID='" + cashierRequestID + '\'' + ", securityPolicyResult="
                + securityPolicyResult + '}';
    }
}
