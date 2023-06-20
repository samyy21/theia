package com.paytm.pgplus.theia.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.user.models.UserDocument;
import com.paytm.pgplus.facade.user.models.request.UserKycData;

/**
 * @author kartik
 * @date 06-Mar-2018
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycRequestModel implements Serializable {

    private static final long serialVersionUID = -6029131011982673L;

    private List<UserDocument> documents;
    private UserKycData data;

    public List<UserDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<UserDocument> documents) {
        this.documents = documents;
    }

    public UserKycData getData() {
        return data;
    }

    public void setData(UserKycData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("KycRequestModel [documents=").append(documents).append(", data=").append(data).append("]");
        return builder.toString();
    }

}
