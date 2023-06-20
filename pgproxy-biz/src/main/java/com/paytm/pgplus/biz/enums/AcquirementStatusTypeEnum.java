package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum AcquirementStatusTypeEnum {

    INIT("INIT"), SUCCESS("SUCCESS"), CLOSED("CLOSED"), PAYING("PAYING"), ;

    String statusType;

    private AcquirementStatusTypeEnum(final String statusType) {
        this.statusType = statusType;
    }

    /**
     * @return the statusDetail
     */
    public String getStatusType() {
        return statusType;
    }

    public static AcquirementStatusTypeEnum getAcquirementStatusEnumByStatusType(final String statusDetail) {
        for (final AcquirementStatusTypeEnum enumStatusDetail : AcquirementStatusTypeEnum.values()) {
            if (statusDetail.equals(enumStatusDetail.getStatusType())) {
                return enumStatusDetail;
            }
        }
        return null;
    }

}
