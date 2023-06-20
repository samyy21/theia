package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;

/**
 * Created by rahulverma on 23/8/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceStatusInfo extends StatusInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 872234347671231L;

    private String userAccountExist;
    private String merchantAccept;

    public BalanceStatusInfo(String userAccountExist, String merchantAccept, String status, String msg) {
        super(status, msg);
        this.userAccountExist = userAccountExist;
        this.merchantAccept = merchantAccept;
    }

    public String getUserAccountExist() {
        return userAccountExist;
    }

    public void setUserAccountExist(String userAccountExist) {
        this.userAccountExist = userAccountExist;
    }

    public String getMerchantAccept() {
        return merchantAccept;
    }

    public void setMerchantAccept(String merchantAccept) {
        this.merchantAccept = merchantAccept;
    }

}
