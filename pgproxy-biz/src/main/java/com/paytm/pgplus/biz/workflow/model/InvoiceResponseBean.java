/**
 * 
 */
package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class InvoiceResponseBean implements Serializable {

    private static final long serialVersionUID = -2993251646329678563L;

    private String transID;
    private String invoiceLink;
    private String status;
    private String errorCode;
    private String errorMessage;

    public InvoiceResponseBean() {

    }

    public InvoiceResponseBean(String transID, String invoiceLink) {
        this.transID = transID;
        this.invoiceLink = invoiceLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String transID) {
        this.transID = transID;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public void setInvoiceLink(String invoiceLink) {
        this.invoiceLink = invoiceLink;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InvoiceResponseBean{");
        sb.append("transID='").append(transID).append('\'');
        sb.append(", invoiceLink='").append(invoiceLink).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", errorCode='").append(errorCode).append('\'');
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
