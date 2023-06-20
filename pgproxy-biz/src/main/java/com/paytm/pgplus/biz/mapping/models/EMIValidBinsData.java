/**
 * 
 */
package com.paytm.pgplus.biz.mapping.models;

import java.io.Serializable;
import java.util.List;

/**
 * @author naman
 *
 */
public class EMIValidBinsData implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3274475659100848330L;

    List<String> validBins;

    public EMIValidBinsData(List<String> validBins) {
        this.validBins = validBins;
    }

    public List<String> getValidBins() {
        return validBins;
    }

    public void setValidBins(List<String> validBins) {
        this.validBins = validBins;
    }

}
