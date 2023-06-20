/**
 * 
 */
package com.paytm.pgplus.session.model;

/**
 * @createdOn 09-Mar-2016
 * @author kesari
 */
public class MerchantConfig {

    private String midField;
    private String orderIdField;
    private String dataType;

    /**
     * @return
     */
    public String getMidField() {
        return midField;
    }

    /**
     * 
     * @param midField
     */
    public void setMidField(String midField) {
        this.midField = midField;
    }

    /**
     * @return
     */
    public String getOrderIdField() {
        return orderIdField;
    }

    /**
     * @param orderIdField
     */
    public void setOrderIdField(String orderIdField) {
        this.orderIdField = orderIdField;
    }

    /**
     * @return
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantConfig [midField=").append(midField).append(", orderIdField=").append(orderIdField)
                .append(", dataType=").append(dataType).append("]");
        return builder.toString();
    }

}
