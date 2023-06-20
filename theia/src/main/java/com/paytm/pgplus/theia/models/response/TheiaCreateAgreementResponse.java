package com.paytm.pgplus.theia.models.response;

import com.paytm.pgplus.facade.acquiring.enums.AgreementStatus;

import java.io.Serializable;
import java.util.Date;

public class TheiaCreateAgreementResponse implements Serializable {

    private static final long serialVersionUID = 7127896237283132877L;

    private String agreementId;
    private String merchantName;
    private String merchantAgreementId;
    private Date cancelTime;
    private Date createdTime;
    private AgreementStatus status;
    private String cancelReason;
    private String description;
    private Date activateTime;

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantAgreementId() {
        return merchantAgreementId;
    }

    public void setMerchantAgreementId(String merchantAgreementId) {
        this.merchantAgreementId = merchantAgreementId;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public AgreementStatus getStatus() {
        return status;
    }

    public void setStatus(AgreementStatus status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getActivateTime() {
        return activateTime;
    }

    public void setActivateTime(Date activateTime) {
        this.activateTime = activateTime;
    }
}
