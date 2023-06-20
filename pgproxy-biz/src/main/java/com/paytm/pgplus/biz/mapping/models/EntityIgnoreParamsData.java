/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

/**
 * @author riteshkumarsharma
 *
 */
import java.io.Serializable;

public class EntityIgnoreParamsData implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6375994718600416713L;

    private Long entityId;

    private String fieldName;

    public EntityIgnoreParamsData() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityIgnoreParamsData [entityId=");
        builder.append(entityId);
        builder.append(", fieldName=");
        builder.append(fieldName);
        builder.append("]");
        return builder.toString();
    }

}
