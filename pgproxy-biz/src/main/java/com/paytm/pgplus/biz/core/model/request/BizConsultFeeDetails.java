/**
 * 
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

import com.paytm.pgplus.common.enums.EPayMethod;

/**
 * @author namanjain
 *
 */
public class BizConsultFeeDetails implements Serializable {

    private static final long serialVersionUID = 8360280686723528073L;

    private EPayMethod payMethod;

    private String baseTransactionAmount;

    private String feeAmount;

    private String taxAmount;

    private String totalTransactionAmount;

    public EPayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(EPayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public String getBaseTransactionAmount() {
        return baseTransactionAmount;
    }

    public void setBaseTransactionAmount(String baseTransactionAmount) {
        this.baseTransactionAmount = baseTransactionAmount;
    }

    public String getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(String feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

}
