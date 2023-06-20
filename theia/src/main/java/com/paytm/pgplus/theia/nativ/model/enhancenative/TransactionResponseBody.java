package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.payloadvault.theia.response.AdditionalParam;
import com.paytm.pgplus.payloadvault.theia.response.ChildTransaction;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

import java.util.List;

/**
 * Created by rahulverma on 7/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponseBody extends BaseResponseBody {
    private static final long serialVersionUID = -8344751234368062L;

    private String mid;
    private String orderId;

    private String txnId;
    private String transactionStatus;
    private String bankTxnId;
    private String rrn;
    private String authCode;
    private String responseCode;
    private String responseMsg;
    private String txnAmount;
    private String paymentMode;
    private String gateway;
    private String currency = "INR";
    private String custId;
    private String binNumber;
    private String lastFourDigits;
    private String merchUniqueReference;
    private String udf1;
    private String udf2;
    private String udf3;
    private String additionalInfo;
    private String txnDate;
    private String callbackUrl;
    private List<ChildTransaction> childTxnList;
    private String subsId;
    // Fields Added for PromoCode
    private String promoResponseCode;
    private String promoCode;
    private String promoApplyResultStatus;
    private String bankName;

    private String maskedCardNo;
    private String cardIndexNo;
    private String requestType;
    private AdditionalParam additionalParam;
    private String txnToken;
    private String userEmail;
    private String userPhone;
    private String cardHash;

    public TransactionResponseBody() {
    }

    public TransactionResponseBody(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public static TransactionResponseBody getTransactionResponse(
            com.paytm.pgplus.payloadvault.theia.response.TransactionResponse transactionResponse) {
        TransactionResponseBody responseBody = new TransactionResponseBody();
        responseBody.mid = transactionResponse.getMid();
        responseBody.orderId = transactionResponse.getOrderId();
        responseBody.txnId = transactionResponse.getTxnId();
        responseBody.transactionStatus = transactionResponse.getTransactionStatus();
        responseBody.bankTxnId = transactionResponse.getBankTxnId();
        responseBody.rrn = transactionResponse.getRrn();
        responseBody.authCode = transactionResponse.getAuthCode();
        responseBody.responseCode = transactionResponse.getResponseCode();
        responseBody.responseMsg = transactionResponse.getResponseMsg();
        responseBody.txnAmount = transactionResponse.getTxnAmount();
        responseBody.paymentMode = transactionResponse.getPaymentMode();
        responseBody.gateway = transactionResponse.getGateway();
        responseBody.currency = transactionResponse.getCurrency();
        responseBody.custId = transactionResponse.getCustId();
        responseBody.binNumber = transactionResponse.getBinNumber();
        responseBody.lastFourDigits = transactionResponse.getLastFourDigits();
        responseBody.merchUniqueReference = transactionResponse.getMerchUniqueReference();
        responseBody.udf1 = transactionResponse.getUdf1();
        responseBody.udf2 = transactionResponse.getUdf2();
        responseBody.udf3 = transactionResponse.getUdf3();
        responseBody.additionalInfo = transactionResponse.getAdditionalInfo();
        responseBody.txnDate = transactionResponse.getTxnDate();
        responseBody.callbackUrl = transactionResponse.getCallbackUrl();
        responseBody.childTxnList = transactionResponse.getChildTxnList();
        responseBody.subsId = transactionResponse.getSubsId();
        responseBody.promoResponseCode = transactionResponse.getPromoResponseCode();
        responseBody.promoCode = transactionResponse.getPromoCode();
        responseBody.promoApplyResultStatus = transactionResponse.getPromoApplyResultStatus();
        responseBody.bankName = transactionResponse.getBankName();
        responseBody.maskedCardNo = transactionResponse.getMaskedCardNo();
        responseBody.cardIndexNo = transactionResponse.getCardIndexNo();
        responseBody.cardHash = transactionResponse.getCardHash();
        responseBody.requestType = transactionResponse.getRequestType();
        responseBody.additionalParam = transactionResponse.getAdditionalParam();
        responseBody.txnToken = transactionResponse.getTxnToken();
        responseBody.userEmail = transactionResponse.getUserEmail();
        responseBody.userPhone = transactionResponse.getUserPhone();
        if (!ExternalTransactionStatus.TXN_FAILURE.name().equals(transactionResponse.getTransactionStatus())) {
            com.paytm.pgplus.common.model.ResultInfo resultInfo = OfflinePaymentUtils.resultInfoForSuccess();
            responseBody.setResultInfo(new ResultInfo(resultInfo.getResultStatus(), resultInfo.getResultCodeId(),
                    resultInfo.getResultMsg(), resultInfo.isRedirect()));
        } else {
            responseBody.setResultInfo(new ResultInfo("F", transactionResponse.getResponseCode(), transactionResponse
                    .getResponseMsg(), true));
        }

        return responseBody;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getBankTxnId() {
        return bankTxnId;
    }

    public void setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getMerchUniqueReference() {
        return merchUniqueReference;
    }

    public void setMerchUniqueReference(String merchUniqueReference) {
        this.merchUniqueReference = merchUniqueReference;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public List<ChildTransaction> getChildTxnList() {
        return childTxnList;
    }

    public void setChildTxnList(List<ChildTransaction> childTxnList) {
        this.childTxnList = childTxnList;
    }

    public String getSubsId() {
        return subsId;
    }

    public void setSubsId(String subsId) {
        this.subsId = subsId;
    }

    public String getPromoResponseCode() {
        return promoResponseCode;
    }

    public void setPromoResponseCode(String promoResponseCode) {
        this.promoResponseCode = promoResponseCode;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoApplyResultStatus() {
        return promoApplyResultStatus;
    }

    public void setPromoApplyResultStatus(String promoApplyResultStatus) {
        this.promoApplyResultStatus = promoApplyResultStatus;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    public void setMaskedCardNo(String maskedCardNo) {
        this.maskedCardNo = maskedCardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public AdditionalParam getAdditionalParam() {
        return additionalParam;
    }

    public void setAdditionalParam(AdditionalParam additionalParam) {
        this.additionalParam = additionalParam;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @Override
    public String toString() {
        return "TransactionResponseBody{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\'' + ", txnId='" + txnId
                + '\'' + ", transactionStatus='" + transactionStatus + '\'' + ", bankTxnId='" + bankTxnId + '\''
                + ", rrn='" + rrn + '\'' + ", authCode='" + authCode + '\'' + ", responseCode='" + responseCode + '\''
                + ", responseMsg='" + responseMsg + '\'' + ", txnAmount='" + txnAmount + '\'' + ", paymentMode='"
                + paymentMode + '\'' + ", gateway='" + gateway + '\'' + ", currency='" + currency + '\'' + ", custId='"
                + custId + '\'' + ", binNumber='" + binNumber + '\'' + ", lastFourDigits='" + lastFourDigits + '\''
                + ", merchUniqueReference='" + merchUniqueReference + '\'' + ", udf1='" + udf1 + '\'' + ", udf2='"
                + udf2 + '\'' + ", udf3='" + udf3 + '\'' + ", additionalInfo='" + additionalInfo + '\'' + ", txnDate='"
                + txnDate + '\'' + ", callbackUrl='" + callbackUrl + '\'' + ", childTxnList=" + childTxnList
                + ", subsId='" + subsId + '\'' + ", promoResponseCode='" + promoResponseCode + '\'' + ", promoCode='"
                + promoCode + '\'' + ", promoApplyResultStatus='" + promoApplyResultStatus + '\'' + ", bankName='"
                + bankName + '\'' + ", maskedCardNo='" + maskedCardNo + '\'' + ", cardIndexNo='" + cardIndexNo + '\''
                + ", requestType='" + requestType + '\'' + ", additionalParam=" + additionalParam + ", txnToken='"
                + txnToken + '\'' + ", userEmail='" + userEmail + '\'' + ", userPhone='" + userPhone + '\'' + '}';
    }
}
