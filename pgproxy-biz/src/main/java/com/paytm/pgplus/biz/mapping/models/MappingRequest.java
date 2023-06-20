/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

/**
 * @author vaishakh
 *
 */
public class MappingRequest {

    private final MappingRequestData request;
    private final String signature;

    public MappingRequest(final MappingRequestData request, final String signature) {
        this.request = request;
        this.signature = signature;
    }

    /**
     * @return the request
     */
    public MappingRequestData getRequest() {
        return request;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MappingRequest [request=").append(request).append(", signature=").append(signature).append("]");
        return builder.toString();
    }

}
