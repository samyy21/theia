package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;

import java.io.Serializable;

public class CreateOrderAndPayResponseBean implements Serializable {

    private static final long serialVersionUID = -5756555544556562524L;

    private String merchantTransId;
    private String acquirementId;
    private String requestId;
    private String cashierRequestId;
    private SecurityPolicyResult securityPolicyResult;

    public CreateOrderAndPayResponseBean(String acquirementId, String cashierRequestId) {
        super();
        this.acquirementId = acquirementId;
        this.cashierRequestId = cashierRequestId;
    }

    public CreateOrderAndPayResponseBean(String acquirementId, String cashierRequestId,
            SecurityPolicyResult securityPolicyResult) {
        super();
        this.acquirementId = acquirementId;
        this.cashierRequestId = cashierRequestId;
        this.securityPolicyResult = securityPolicyResult;
    }

    public String getMerchantTransId() {
        return merchantTransId;
    }

    public void setMerchantTransId(String merchantTransId) {
        this.merchantTransId = merchantTransId;
    }

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public SecurityPolicyResult getSecurityPolicyResult() {
        return securityPolicyResult;
    }
}
