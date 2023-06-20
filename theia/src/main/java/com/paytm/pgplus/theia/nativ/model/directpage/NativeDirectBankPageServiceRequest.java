package com.paytm.pgplus.theia.nativ.model.directpage;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;

import java.io.Serializable;
import java.util.Map;

public class NativeDirectBankPageServiceRequest implements Serializable {

    private static final long serialVersionUID = 8205218077670176505L;

    private InitiateTransactionRequestBody orderDetail;
    private TransactionResponse transactionResponse;
    private DirectAPIResponse instaProxyResponse;
    private NativeDirectBankPageCacheData cachedBankFormData;
    private GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean;
    private Map<String, String> transactionStatusData;
    private int currentDirectBankPageSubmitRetryCount;
    private int totalAllowedDirectBankPageSubmitRetryCount;
    private int currentDirectBankPageResendOtpRetryCount;
    private int totalAllowedDirectBankPageResendRetryCount;

    public DirectAPIResponse getInstaProxyResponse() {
        return instaProxyResponse;
    }

    public void setInstaProxyResponse(DirectAPIResponse instaProxyResponse) {
        this.instaProxyResponse = instaProxyResponse;
    }

    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }

    public void setTransactionResponse(TransactionResponse transactionResponse) {
        this.transactionResponse = transactionResponse;
    }

    public InitiateTransactionRequestBody getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(InitiateTransactionRequestBody orderDetail) {
        this.orderDetail = orderDetail;
    }

    public NativeDirectBankPageCacheData getCachedBankFormData() {
        return cachedBankFormData;
    }

    public void setCachedBankFormData(NativeDirectBankPageCacheData cachedBankFormData) {
        this.cachedBankFormData = cachedBankFormData;
    }

    public GenericCoreResponseBean<WorkFlowResponseBean> getWorkFlowResponseBean() {
        return workFlowResponseBean;
    }

    public void setWorkFlowResponseBean(GenericCoreResponseBean<WorkFlowResponseBean> responseBean) {
        this.workFlowResponseBean = responseBean;
    }

    public int getTotalAllowedDirectBankPageSubmitRetryCount() {
        return totalAllowedDirectBankPageSubmitRetryCount;
    }

    public void setTotalAllowedDirectBankPageSubmitRetryCount(int totalAllowedDirectBankPageSubmitRetryCount) {
        this.totalAllowedDirectBankPageSubmitRetryCount = totalAllowedDirectBankPageSubmitRetryCount;
    }

    public Map<String, String> getTransactionStatusData() {
        return transactionStatusData;
    }

    public void setTransactionStatusData(Map<String, String> transactionStatusData) {
        this.transactionStatusData = transactionStatusData;
    }

    public int getCurrentDirectBankPageSubmitRetryCount() {
        return currentDirectBankPageSubmitRetryCount;
    }

    public void setCurrentDirectBankPageSubmitRetryCount(int currentDirectBankPageSubmitRetryCount) {
        this.currentDirectBankPageSubmitRetryCount = currentDirectBankPageSubmitRetryCount;
    }

    public int getCurrentDirectBankPageResendOtpRetryCount() {
        return currentDirectBankPageResendOtpRetryCount;
    }

    public void setCurrentDirectBankPageResendOtpRetryCount(int currentDirectBankPageResendOtpRetryCount) {
        this.currentDirectBankPageResendOtpRetryCount = currentDirectBankPageResendOtpRetryCount;
    }

    public int getTotalAllowedDirectBankPageResendRetryCount() {
        return totalAllowedDirectBankPageResendRetryCount;
    }

    public void setTotalAllowedDirectBankPageResendRetryCount(int totalAllowedDirectBankPageResendRetryCount) {
        this.totalAllowedDirectBankPageResendRetryCount = totalAllowedDirectBankPageResendRetryCount;
    }
}
