/**
 * 
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

/**
 * @author amitdubey
 *
 */
public class CashierResponseCodeDetails implements Serializable {
    /**
		 * 
		 */
    private static final long serialVersionUID = -1493956643390952993L;

    private String paytmResponseCode;
    private boolean retry;
    private String alipayResponseCode;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the paytmResponseCode
     */
    public String getPaytmResponseCode() {
        return paytmResponseCode;
    }

    /**
     * @param paytmResponseCode
     *            the paytmResponseCode to set
     */
    public void setPaytmResponseCode(String paytmResponseCode) {
        this.paytmResponseCode = paytmResponseCode;
    }

    /**
     * @return the alipayResponseCode
     */
    public String getAlipayResponseCode() {
        return alipayResponseCode;
    }

    /**
     * @param alipayResponseCode
     *            the alipayResponseCode to set
     */
    public void setAlipayResponseCode(String alipayResponseCode) {
        this.alipayResponseCode = alipayResponseCode;
    }

    /**
     * @return the retry
     */
    public boolean isRetry() {
        return retry;
    }

    /**
     * @param retry
     *            the retry to set
     */
    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ paytmResponseCode : ").append(paytmResponseCode).append(" retry : ").append(retry)
                .append(" alipayResponseCode : ").append(alipayResponseCode).append(" errorMessage : ")
                .append(errorMessage).append(" } ");

        return sb.toString();
    }
}
