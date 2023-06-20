package com.paytm.pgplus.theia.nativ.model.directpage;

import com.paytm.pgplus.common.bankForm.model.BankForm;

import java.io.Serializable;

public class NativeDirectBankPageCacheData implements Serializable {
    private static final long serialVersionUID = 8629675512437773253L;

    private BankForm bankForm;

    /*
     * This is needed for /v1/transactionStatus
     */
    private String cashierRequestId;
    private String transId;
    private String merchantId;

    public BankForm getBankForm() {
        return bankForm;
    }

    public void setBankForm(BankForm bankForm) {
        this.bankForm = bankForm;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}
