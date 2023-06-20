package com.paytm.pgplus.theia.viewmodel;

import java.io.Serializable;

public class DebitCardDropDownItem implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -5360092309979843330L;
    private Long entityId;
    private Long bankId;
    private String label;
    private Long sortOrder;
    private String bankCode;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    @Override
    public String toString() {
        return "DebitCardDropDownItem [entityId=" + entityId + ", bankId=" + bankId + ", label=" + label
                + ", sortOrder=" + sortOrder + "]";
    }
}
