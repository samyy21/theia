package com.paytm.pgplus.theia.models;

//import com.paytm.pgplus.aoaSubscriptionClient.model.SubscriptionDetails;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaMandateCallbackResponse;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.payloadvault.subscription.response.ProcessedMandateResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedBmResponse implements Serializable {

    private static final long serialVersionUID = -8997937432049941612L;

    private String subscriptionId;

    private Boolean isAccepted = Boolean.FALSE;

    private String acceptedRefNo;

    private com.paytm.pgplus.common.model.ResultInfo resultInfo;

    private String rejectedBy;

    private String mid;

    private String merchantRedirectionUrl;

    private String merchantCustId;

    private String orderId;

    private String mandateType;

    private String txnAmount;

    // private SubscriptionDetails subscriptionDetails;

    private String gatewayCode;

    private boolean isAoa;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public String getAcceptedRefNo() {
        return acceptedRefNo;
    }

    public com.paytm.pgplus.common.model.ResultInfo getResultInfo() {
        return resultInfo;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public String getMid() {
        return mid;
    }

    public String getMerchantRedirectionUrl() {
        return merchantRedirectionUrl;
    }

    public String getMerchantCustId() {
        return merchantCustId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMandateType() {
        return mandateType;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    // public SubscriptionDetails getSubscriptionDetails() {
    // return subscriptionDetails;
    // }

    // public void setSubscriptionDetails(SubscriptionDetails
    // subscriptionDetails) {
    // this.subscriptionDetails = subscriptionDetails;
    // }

    public String getGatewayCode() {
        return gatewayCode;
    }

    public void setGatewayCode(String gatewayCode) {
        this.gatewayCode = gatewayCode;
    }

    public boolean isAoa() {
        return isAoa;
    }

    public void setAoa(boolean aoa) {
        isAoa = aoa;
    }

    public ProcessedBmResponse(ProcessedMandateResponse processedMandateResponse) {
        this.subscriptionId = processedMandateResponse.getSubscriptionId();
        this.isAccepted = BooleanUtils.isTrue(processedMandateResponse.getIsAccepted());
        this.acceptedRefNo = processedMandateResponse.getAcceptedRefNo();
        this.rejectedBy = processedMandateResponse.getRejectedBy();
        this.mid = processedMandateResponse.getMid();
        this.resultInfo = new com.paytm.pgplus.common.model.ResultInfo(processedMandateResponse.getResultInfo()
                .getStatus(), processedMandateResponse.getResultInfo().getCode(), processedMandateResponse
                .getResultInfo().getMessage());
        this.merchantCustId = processedMandateResponse.getMerchantCustId();
        this.orderId = processedMandateResponse.getOrderId();
        this.mandateType = MandateMode.E_MANDATE.name();
        this.txnAmount = processedMandateResponse.getTxnAmount();

        this.merchantRedirectionUrl = processedMandateResponse.getMandateCallbackUrl();
    }

    // public ProcessedBmResponse(AoaMandateCallbackResponse
    // aoaMandateCallbackResponse){
    // this.subscriptionId = aoaMandateCallbackResponse.getSubscriptionId();
    // this.mid = aoaMandateCallbackResponse.getMid();
    // this.isAccepted =
    // Boolean.valueOf(aoaMandateCallbackResponse.getIsAccepted());
    // this.orderId = aoaMandateCallbackResponse.getOrderId();
    // this.rejectedBy = aoaMandateCallbackResponse.getRejectionDesc();
    //
    // if(aoaMandateCallbackResponse.getSubscriptionDetails() != null) {
    // this.mandateType =
    // aoaMandateCallbackResponse.getSubscriptionDetails().getMandateType();
    // this.resultInfo = new
    // com.paytm.pgplus.common.model.ResultInfo(aoaMandateCallbackResponse.getSubscriptionDetails().getStatus(),
    // aoaMandateCallbackResponse.getRejectionCode(), aoaMandateCallbackResponse
    // .getRejectionDesc());
    // }
    // this.setSubscriptionDetails(aoaMandateCallbackResponse.getSubscriptionDetails());
    // this.gatewayCode = aoaMandateCallbackResponse.getGatewayCode();
    // this.isAoa = true;
    // }
}
