package com.paytm.pgplus.biz.exception;

/**
 * Created by rahulverma on 3/5/18.
 */
public class BizMerchantVelocityBreachedException extends RuntimeException {

    private String limitType;
    private String limitDuration;
    private boolean isAddnPayTransaction = false;

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public String getLimitDuration() {
        return limitDuration;
    }

    public void setLimitDuration(String limitDuration) {
        this.limitDuration = limitDuration;
    }

    public boolean isAddnPayTransaction() {
        return isAddnPayTransaction;
    }

    public void setAddnPayTransaction(boolean addnPayTransaction) {
        isAddnPayTransaction = addnPayTransaction;
    }

    public BizMerchantVelocityBreachedException() {
    }

    public BizMerchantVelocityBreachedException(String s) {
        super(s);
    }

    public BizMerchantVelocityBreachedException(String message, String limitType, String limitDuration) {
        super(message);
        this.limitType = limitType;
        this.limitDuration = limitDuration;
    }

}
