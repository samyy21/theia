package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateVpaResponseFromBank implements Serializable {

    private static final long serialVersionUID = -4367062182331480094L;

    private String SeqNo;
    private String BankRRN;
    private String UserProfile;
    private String success;
    private String response;
    private String MobileAppData;
    private String message;
    private String UpiTranlogId;
    private String status;
    private String respMessage;
    private String respCode;

    public String getSeqNo() {
        return SeqNo;
    }

    public void setSeqNo(String SeqNo) {
        this.SeqNo = SeqNo;
    }

    public String getBankRRN() {
        return BankRRN;
    }

    public void setBankRRN(String BankRRN) {
        this.BankRRN = BankRRN;
    }

    public String getUserProfile() {
        return UserProfile;
    }

    public void setUserProfile(String UserProfile) {
        this.UserProfile = UserProfile;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMobileAppData() {
        return MobileAppData;
    }

    public void setMobileAppData(String MobileAppData) {
        this.MobileAppData = MobileAppData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUpiTranlogId() {
        return UpiTranlogId;
    }

    public void setUpiTranlogId(String UpiTranlogId) {
        this.UpiTranlogId = UpiTranlogId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRespMessage() {
        return respMessage;
    }

    public void setRespMessage(String respMessage) {
        this.respMessage = respMessage;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValidateVpaResponseFromBank [SeqNo=");
        builder.append(SeqNo);
        builder.append(", BankRRN=");
        builder.append(BankRRN);
        builder.append(", UserProfile=");
        builder.append(UserProfile);
        builder.append(", success=");
        builder.append(success);
        builder.append(", response=");
        builder.append(response);
        builder.append(", MobileAppData=");
        builder.append(MobileAppData);
        builder.append(", message=");
        builder.append(message);
        builder.append(", UpiTranlogId=");
        builder.append(UpiTranlogId);
        builder.append(", status=");
        builder.append(status);
        builder.append(", respMessage=");
        builder.append(respMessage);
        builder.append(", respCode=");
        builder.append(respCode);
        builder.append("]");
        return builder.toString();
    }

}
