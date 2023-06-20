/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

/**
 * @author vaishakh
 *
 */
public class MappingRequestData {

    private final MappingServiceRequestHeader head;
    private final MappingRequestBody body;

    public MappingRequestData(final MappingServiceRequestHeader head, final MappingRequestBody body) {
        this.head = head;
        this.body = body;
    }

    /**
     * @return the head
     */
    public MappingServiceRequestHeader getHead() {
        return head;
    }

    /**
     * @return the body
     */
    public MappingRequestBody getBody() {
        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MappingRequestData [head=").append(head).append(", body=").append(body).append("]");
        return builder.toString();
    }

}
