package com.paytm.pgplus.theia.services;

public abstract class InternalPaymentRetryService<T, RQ, RS> {

    public abstract RS retryBankFormFetchWithPayment(RQ request, T workFlow);

    public abstract boolean isInternalPaymentRetryRequired(RS initiatePaymentResponse);

    public abstract void setRetryParams(RQ request, long currentRetryCount);

}
