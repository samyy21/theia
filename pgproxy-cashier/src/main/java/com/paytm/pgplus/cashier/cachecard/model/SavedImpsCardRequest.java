/**
 * 
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author amit.dubey
 *
 */
public class SavedImpsCardRequest implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 112431412313412L;

    /** savedCardId */
    private String savedCardId;

    /** otp number */
    private String otp;

    private String instNetworkCode;

    /**
     * @param savedCardId
     * @param otp
     * @throws CashierCheckedException
     */
    public SavedImpsCardRequest(String savedCardId, String otp, String instNetworkCode) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(savedCardId, "savedCardId");
        this.savedCardId = savedCardId;

        BeanParameterValidator.validateInputStringParam(otp, "OTP");
        this.otp = otp;

        BeanParameterValidator.validateInputStringParam(instNetworkCode, "instNetworkCode");
        this.instNetworkCode = instNetworkCode;
    }

    /**
     * @return the savedCardId
     */
    public String getSavedCardId() {
        return savedCardId;
    }

    /**
     * @return the otp
     */
    public String getOtp() {
        return otp;
    }

    /**
     * @return the instNetworkCode
     */
    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavedImpsCardRequest [savedCardId=");
        builder.append(savedCardId);
        builder.append(", otp=");
        builder.append(otp);
        builder.append(", instNetworkCode=");
        builder.append(instNetworkCode);
        builder.append("]");
        return builder.toString();
    }
}