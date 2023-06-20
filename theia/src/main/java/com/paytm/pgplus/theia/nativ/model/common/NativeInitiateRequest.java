package com.paytm.pgplus.theia.nativ.model.common;

import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;

import java.io.Serializable;

public class NativeInitiateRequest implements Serializable {

    private static final long serialVersionUID = -5630836401184754137L;

    private InitiateTransactionRequest initiateTxnReq;
    private NativePersistData nativePersistData;
    private boolean orderCreateInInitiate;

    public NativeInitiateRequest() {
    }

    public NativeInitiateRequest(InitiateTransactionRequestBody initiateTransactionRequestBody) {
        this.initiateTxnReq = new InitiateTransactionRequest();
        this.initiateTxnReq.setBody(initiateTransactionRequestBody);
    }

    public NativeInitiateRequest(InitiateTransactionRequest initiateTxnReq, NativePersistData nativePersistData) {
        this.initiateTxnReq = initiateTxnReq;
        this.nativePersistData = nativePersistData;
    }

    public InitiateTransactionRequest getInitiateTxnReq() {
        return initiateTxnReq;
    }

    public void setInitiateTxnReq(InitiateTransactionRequest initiateTxnReq) {
        this.initiateTxnReq = initiateTxnReq;
    }

    public NativePersistData getNativePersistData() {
        return nativePersistData;
    }

    public void setNativePersistData(NativePersistData nativePersistData) {
        this.nativePersistData = nativePersistData;
    }

    public boolean isOrderCreateInInitiate() {
        return orderCreateInInitiate;
    }

    public void setOrderCreateInInitiate(boolean orderCreateInInitiate) {
        this.orderCreateInInitiate = orderCreateInInitiate;
    }
}
