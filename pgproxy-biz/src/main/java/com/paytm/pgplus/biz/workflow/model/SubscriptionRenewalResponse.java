package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by prashant on 4/6/16.
 */
public class SubscriptionRenewalResponse {

    @JsonProperty("TXNID")
    private String txnId;

    @JsonProperty("ORDERID")
    private String orderId;

    @JsonProperty("TXNAMOUNT")
    private String txnAmount;

    @JsonProperty("RESPCODE")
    private String respCode;

    @JsonProperty("RESPMSG")
    private String respMsg;

    @JsonProperty("MID")
    private String mid;

    @JsonProperty("SUBS_ID")
    private String subsId;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("MERC_UNQ_REF")
    private String merchantUniqueRefernce;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("CARDINDEXNO")
    private String cardIndexNo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("CARDHASH")
    private String cardHash;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSubsId() {
        return subsId;
    }

    public void setSubsId(String subsId) {
        this.subsId = subsId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMerchantUniqueRefernce() {
        return merchantUniqueRefernce;
    }

    public void setMerchantUniqueRefernce(String merchantUniqueRefernce) {
        this.merchantUniqueRefernce = merchantUniqueRefernce;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    @Override
    public String toString() {
        return "SubscriptionRenewalResponse{" + "txnId='" + txnId + '\'' + ", orderId='" + orderId + '\''
                + ", txnAmount='" + txnAmount + '\'' + ", respCode='" + respCode + '\'' + ", respMsg='" + respMsg
                + '\'' + ", mid='" + mid + '\'' + ", subsId='" + subsId + '\'' + ", status='" + status + '\''
                + ", merchantUniqueRefernce='" + merchantUniqueRefernce + '\'' + ", cardIndexNo='" + cardIndexNo + '\''
                + ", cardHash='" + cardHash + '\'' + '}';
    }
}
