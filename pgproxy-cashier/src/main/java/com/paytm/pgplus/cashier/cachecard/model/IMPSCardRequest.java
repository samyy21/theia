/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author Himanshu Sardana
 *
 */
public class IMPSCardRequest implements Serializable {
    /**
     * serial version UID
     */
    private static final long serialVersionUID = 3927807306586165127L;

    @NotBlank(message = "{notblank}")
    @Length(max = 32, message = "{lengthlimit}")
    private final String mmid;

    @NotBlank
    private final String otp;

    @NotBlank
    @Length(max = 64, message = "instNetworkCode cannot be more than 32 characters")
    private final String instNetworkCode;

    @NotBlank
    @Length(max = 32, message = "holderMobileNo cannot be more than 32 characters")
    private final String holderMobileNo;

    /**
     * IMPS CARD REQUEST
     *
     * @param mmid
     * @param otp
     * @param instNetworkCode
     * @param holderMobileNo
     * @throws CashierCheckedException
     */
    public IMPSCardRequest(final String mmid, final String otp, final String instNetworkCode,
            final String holderMobileNo) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(mmid, "mmid");
        this.mmid = mmid;

        BeanParameterValidator.validateInputStringParam(otp, "otp");
        this.otp = otp;

        BeanParameterValidator.validateInputStringParam(instNetworkCode, "instNetworkCode");
        this.instNetworkCode = instNetworkCode;

        BeanParameterValidator.validateInputStringParam(holderMobileNo, "holderMobileNo");
        this.holderMobileNo = holderMobileNo;
    }

    public String getMmid() {
        return mmid;
    }

    public String getOtp() {
        return otp;
    }

    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    public String getHolderMobileNo() {
        return holderMobileNo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IMPSCardRequest [mmid=");
        builder.append(mmid);
        builder.append(", otp=");
        builder.append(otp);
        builder.append(", instNetworkCode=");
        builder.append(instNetworkCode);
        builder.append(", holderMobileNo=");
        builder.append(holderMobileNo);
        builder.append("]");
        return builder.toString();
    }
}