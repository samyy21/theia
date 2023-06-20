/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

import java.io.Serializable;
import java.util.List;

/**
 * @author riteshkumarsharma
 *
 */
public class EntityIgnoreParamsDataResponse implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2358406464459297357L;

    private List<EntityIgnoreParamsData> paramsList;
    private boolean isSuccessfullyProcessed;

    public EntityIgnoreParamsDataResponse() {

    }

    public EntityIgnoreParamsDataResponse(List<EntityIgnoreParamsData> paramsList) {
        this.paramsList = paramsList;
        this.isSuccessfullyProcessed = true;
    }

    public List<EntityIgnoreParamsData> getParamsList() {
        return paramsList;
    }

    public void setParamsList(List<EntityIgnoreParamsData> paramsList) {
        this.paramsList = paramsList;
    }

    public boolean isSuccessfullyProcessed() {
        return isSuccessfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean isSuccessfullyProcessed) {
        this.isSuccessfullyProcessed = isSuccessfullyProcessed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityIgnoreParamsDataResponse [paramsList=");
        builder.append(paramsList);
        builder.append(", isSuccessfullyProcessed=");
        builder.append(isSuccessfullyProcessed);
        builder.append("]");
        return builder.toString();
    }

}