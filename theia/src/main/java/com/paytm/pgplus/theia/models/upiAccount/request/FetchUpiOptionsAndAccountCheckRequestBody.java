package com.paytm.pgplus.theia.models.upiAccount.request;

import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class FetchUpiOptionsAndAccountCheckRequestBody {

    @NotNull(message = "cannot be null")
    private String mid;

    @NotNull(message = "cannot be null")
    @Mask
    private String mobileNumber;

    @NotNull(message = "cannot be null")
    private String deviceId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
