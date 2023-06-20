package com.paytm.pgplus.theia.models.upiAccount.request;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.customaspects.annotations.Mask;
import org.hibernate.validator.constraints.NotBlank;

@Deprecated
public class CheckUPIAccountRequestBody {

    @NotBlank
    private String mid;
    @NotBlank
    @Mask
    private String mobileNumber;
    @NotBlank
    private String deviceId;

    public CheckUPIAccountRequestBody() {
    }

    public CheckUPIAccountRequestBody(String mid, String mobileNumber, String deviceId) {
        this.mid = mid;
        this.mobileNumber = mobileNumber;
        this.deviceId = deviceId;
    }

    public CheckUPIAccountRequestBody(String mid, String mobileNo) {
        this.mid = mid;
        this.mobileNumber = mobileNo;
    }

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
        return new MaskToStringBuilder(this).setExcludeFieldNames("mobileNumber").toString();
    }
}
