/**
 *
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.pgproxycommon.models.MappingUserData;

/**
 * @author vaishakh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingResponseData {

    private String sourceId;
    private String type;
    private MappingMerchantData merchantData;
    private MappingUserData userData;

    /**
     * @return the sourceId
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the merchantData
     */
    public MappingMerchantData getMerchantData() {
        return merchantData;
    }

    /**
     * @return the userData
     */
    public MappingUserData getUserData() {
        return userData;
    }

    public MappingResponseData(@JsonProperty("sourceId") String sourceId, @JsonProperty("type") String type,
            @JsonProperty("merchantData") MappingMerchantData merchantData,
            @JsonProperty("userData") MappingUserData userData) {
        this.sourceId = sourceId;
        this.type = type;
        this.merchantData = merchantData;
        this.userData = userData;
    }
}
