/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author vaishakh
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingMerchantData {

    private String paytmId;
    private String paytmWalletId;
    private String alipayId;
    private String alipayWalletId;
    private String officialName;

    /**
     * @return the paytmId
     */
    public String getPaytmId() {
        return paytmId;
    }

    /**
     * @return the paytmWalletId
     */
    public String getPaytmWalletId() {
        return paytmWalletId;
    }

    /**
     * @return the alipayId
     */
    public String getAlipayId() {
        return alipayId;
    }

    /**
     * @return the alipayWalletId
     */
    public String getAlipayWalletId() {
        return alipayWalletId;
    }

    /**
     * @return the officialName
     */
    public String getOfficialName() {
        return officialName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MappingMerchantData [paytmId=").append(paytmId).append(", paytmWalletId=")
                .append(paytmWalletId).append(", alipayId=").append(alipayId).append(", alipayWalletId=")
                .append(alipayWalletId).append(", officialName=").append(officialName).append("]");
        return builder.toString();
    }
}
