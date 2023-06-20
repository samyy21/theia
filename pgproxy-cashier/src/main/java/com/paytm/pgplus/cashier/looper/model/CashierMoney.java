/**
 * 
 */
package com.paytm.pgplus.cashier.looper.model;

import java.io.Serializable;

/**
 * @author amit.dubey
 *
 */
public class CashierMoney implements Serializable {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = 1L;

    private String currencyType;
    private String amount;

    /**
     * @param currencyType
     * @param amount
     */
    public CashierMoney(String currencyType, String amount) {
        this.currencyType = currencyType;
        this.amount = amount;
    }

    /**
     * @return the currencyType
     */
    public String getCurrencyType() {
        return currencyType;
    }

    /**
     * @return the amount
     */
    public String getAmount() {
        return amount;
    }

}
