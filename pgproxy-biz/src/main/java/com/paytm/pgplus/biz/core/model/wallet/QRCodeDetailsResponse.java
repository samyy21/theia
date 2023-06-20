package com.paytm.pgplus.biz.core.model.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.enums.QrType;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QRCodeDetailsResponse implements Serializable {

    private static final long serialVersionUID = -3302754084855559978L;

    private String path;

    private String encryptedData;

    private boolean isQREnabled;

    private QrType qrType;

    public QRCodeDetailsResponse() {
    }

    public QRCodeDetailsResponse(String path, String encryptedData, boolean isQREnabled, QrType qrType) {
        this.path = path;
        this.encryptedData = encryptedData;
        this.isQREnabled = isQREnabled;
        this.qrType = qrType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public boolean getIsQREnabled() {
        return isQREnabled;
    }

    public void setIsQREnabled(boolean isQREnabled) {
        this.isQREnabled = isQREnabled;
    }

    public QrType getQrType() {
        return qrType;
    }

    public void setQrType(QrType qrType) {
        this.qrType = qrType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QRCodeDetailsResponse{");
        sb.append("path='").append(path).append('\'');
        sb.append(", encryptedData='").append(encryptedData).append('\'');
        sb.append(", isQREnabled=").append(isQREnabled);
        sb.append(", qrType=").append(qrType);
        sb.append('}');
        return sb.toString();
    }
}
