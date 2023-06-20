/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author vaishakh
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingOuterResponse<T> {

    private MappingResponse<T> response;
    private String signature;

    @JsonProperty("response")
    public MappingResponse<T> getResponse() {
        return response;
    }

    /**
     * @return the signature
     */

    @JsonProperty("signature")
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
        builder.append("MappingOuterResponse [response=").append(response).append(", signature=").append(signature)
                .append("]");
        return builder.toString();
    }

}
