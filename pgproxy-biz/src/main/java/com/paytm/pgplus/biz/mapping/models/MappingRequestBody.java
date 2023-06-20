/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

public class MappingRequestBody {

    private final String id;
    private final String type;

    public MappingRequestBody(final String id, final String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MappingRequestBody [id=").append(id).append(", type=").append(type).append("]");
        return builder.toString();
    }

}
