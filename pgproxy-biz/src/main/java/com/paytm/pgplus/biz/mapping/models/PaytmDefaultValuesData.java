/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

import java.io.Serializable;

/**
 * @author riteshkumarsharma
 *
 */
/**
 * 
 */

public class PaytmDefaultValuesData implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2352036092393601154L;

    private String fieldName;

    private String fieldType;

    private Long status;

    public PaytmDefaultValuesData(String fieldName, String fieldType, Long status) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.status = status;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName
     *            the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the fieldType
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType
     *            the fieldType to set
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @return the status
     */
    public Long getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Long status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaytmDefaultValuesData [fieldName=");
        builder.append(fieldName);
        builder.append(", fieldType=");
        builder.append(fieldType);
        builder.append(", status=");
        builder.append(status);
        builder.append("]");
        return builder.toString();
    }

}
