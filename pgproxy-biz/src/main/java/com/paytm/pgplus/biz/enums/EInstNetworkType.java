package com.paytm.pgplus.biz.enums;

import java.security.InvalidParameterException;

import org.apache.commons.lang.StringUtils;

/**
 * @author manojpal
 *
 */
public enum EInstNetworkType {

    ISOCARD("ISOCARD"), IMPS("IMPS"), IFSC("IFSC"), ;

    String networkType;

    private EInstNetworkType(String networkType) {
        this.networkType = networkType;
    }

    /**
     * @return the networkType
     */
    public String getNetworkType() {
        return networkType;
    }

    /**
     * This method is used to fetch the InstNetworkType, given the network Type.
     * 
     * @param networkType
     * @return
     */
    public static EInstNetworkType getInstNetworkTypeByNetworkType(String networkType) throws InvalidParameterException {
        if (!StringUtils.isBlank(networkType)) {
            for (EInstNetworkType instNetworkType : EInstNetworkType.values()) {
                if (networkType.equals(instNetworkType.getNetworkType())) {
                    return instNetworkType;
                }
            }
        }
        throw new InvalidParameterException("Given value of Network Type is not recognized by the system");
    }

}
