package com.paytm.pgplus.theia.models;

import java.util.List;

public class CacheInvalidationRequest {

    private List<String> cacheNames;

    /**
     * @return the cacheNames
     */
    public List<String> getCacheNames() {
        return cacheNames;
    }

    /**
     * @param cacheNames
     *            the cacheNames to set
     */
    public void setCacheNames(List<String> cacheNames) {
        this.cacheNames = cacheNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CacheInvalidationRequest [cacheNames=").append(cacheNames).append("]");
        return builder.toString();
    }

}
