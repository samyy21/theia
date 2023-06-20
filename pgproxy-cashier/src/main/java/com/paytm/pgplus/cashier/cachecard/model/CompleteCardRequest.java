/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import com.paytm.pgplus.facade.enums.InstNetworkType;

/**
 * @author amit.dubey
 *
 */
public class CompleteCardRequest implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5429761736945994616L;

    private boolean savedDataRequest;
    private BankCardRequest bankCardRequest;
    private BinCardRequest binCardRequest;
    private IMPSCardRequest impsCardRequest;
    private InstNetworkType instNetworkType;
    private VPACardRequest vpaCardRequest;
    private boolean directInstService;

    /**
     * @param instNetworkType
     */
    public CompleteCardRequest(InstNetworkType instNetworkType) {
        this.instNetworkType = instNetworkType;
    }

    /**
     * @return the bankCardRequest
     */
    public BankCardRequest getBankCardRequest() {
        return bankCardRequest;
    }

    /**
     * @param bankCardRequest
     *            the bankCardRequest to set
     */
    public void setBankCardRequest(BankCardRequest bankCardRequest) {
        this.bankCardRequest = bankCardRequest;
    }

    /**
     * @return the binCardRequest
     */
    public BinCardRequest getBinCardRequest() {
        return binCardRequest;
    }

    /**
     * @param binCardRequest
     *            the binCardRequest to set
     */
    public void setBinCardRequest(BinCardRequest binCardRequest) {
        this.binCardRequest = binCardRequest;
    }

    /**
     * @return the impsCardRequest
     */
    public IMPSCardRequest getImpsCardRequest() {
        return impsCardRequest;
    }

    /**
     * @param impsCardRequest
     *            the impsCardRequest to set
     */
    public void setImpsCardRequest(IMPSCardRequest impsCardRequest) {
        this.impsCardRequest = impsCardRequest;
    }

    /**
     * @return the instNetworkType
     */
    public InstNetworkType getInstNetworkType() {
        return instNetworkType;
    }

    /**
     * @return the savedDataRequest
     */
    public boolean isSavedDataRequest() {
        return savedDataRequest;
    }

    /**
     * @param savedDataRequest
     *            the savedDataRequest to set
     */
    public void setSavedDataRequest(boolean savedDataRequest) {
        this.savedDataRequest = savedDataRequest;
    }

    public VPACardRequest getVpaCardRequest() {
        return vpaCardRequest;
    }

    public void setVpaCardRequest(VPACardRequest vpaCardRequest) {
        this.vpaCardRequest = vpaCardRequest;
    }

    // return direct gateway supported or not
    public boolean isDirectInstService() {
        return directInstService;
    }

    public void setDirectInstService(boolean directInstService) {
        this.directInstService = directInstService;
    }

}
