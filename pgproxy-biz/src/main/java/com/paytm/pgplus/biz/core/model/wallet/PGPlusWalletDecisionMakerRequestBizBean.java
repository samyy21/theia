package com.paytm.pgplus.biz.core.model.wallet;

import java.io.Serializable;

import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;

public class PGPlusWalletDecisionMakerRequestBizBean implements Serializable {

    private static final long serialVersionUID = 4844433343479201524L;

    private ConsultPayViewResponseBizBean consultPayViewResponseBean;

    private LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean;

    private String txnAmount;// From Request

    private String userID; // From UserDetails

    private String orderID;// From Request

    private String paytmMID;

    private boolean gvFlag;

    private String targetPhoneNo;

    private boolean addAndPay;

    /***
     * Added Flag to send 'addMoneyDestination=TRANSIT_BLOCKED_WALLET" in wallet
     * consult request.
     */
    private boolean transitWallet;

    public boolean isAddAndPay() {
        return addAndPay;
    }

    public void setAddAndPay(boolean addAndPay) {
        this.addAndPay = addAndPay;
    }

    public String getPaytmMID() {
        return paytmMID;
    }

    public void setPaytmMID(String paytmMID) {
        this.paytmMID = paytmMID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public ConsultPayViewResponseBizBean getConsultPayViewResponseBean() {
        return consultPayViewResponseBean;
    }

    public void setConsultPayViewResponseBean(ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        this.consultPayViewResponseBean = consultPayViewResponseBean;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public LitePayviewConsultResponseBizBean getLitePayviewConsultResponseBizBean() {
        return litePayviewConsultResponseBizBean;
    }

    public void setLitePayviewConsultResponseBizBean(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        this.litePayviewConsultResponseBizBean = litePayviewConsultResponseBizBean;
    }

    public boolean isGvFlag() {
        return gvFlag;
    }

    public void setGvFlag(boolean gvFlag) {
        this.gvFlag = gvFlag;
    }

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    public boolean isTransitWallet() {
        return transitWallet;
    }

    public void setTransitWallet(boolean transitWallet) {
        this.transitWallet = transitWallet;
    }

    public PGPlusWalletDecisionMakerRequestBizBean(ConsultPayViewResponseBizBean consultPayViewResponseBean,
            String txnAmount, String userID, String orderID, String mID) {
        this.consultPayViewResponseBean = consultPayViewResponseBean;
        this.txnAmount = txnAmount;
        this.userID = userID;
        this.orderID = orderID;
        this.paytmMID = mID;
    }

    public PGPlusWalletDecisionMakerRequestBizBean() {

    }

}
