package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

import com.paytm.pgplus.biz.enums.AcquirementStatusTypeEnum;

public class BizStatusDetail implements Serializable {

    private static final long serialVersionUID = -3985164281333657670L;

    private AcquirementStatusTypeEnum acquirementStatus;

    private boolean frozen;

    protected BizStatusDetail() {

    }

    public BizStatusDetail(final AcquirementStatusTypeEnum acquirementStatus, final boolean frozen) {
        this.acquirementStatus = acquirementStatus;
        this.frozen = frozen;
    }

    public AcquirementStatusTypeEnum getAcquirementStatus() {
        return acquirementStatus;
    }

    public boolean isFrozen() {
        return frozen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("StatusDetail [acquirementStatus=").append(acquirementStatus).append(", frozen=").append(frozen)
                .append("]");
        return builder.toString();
    }

}
