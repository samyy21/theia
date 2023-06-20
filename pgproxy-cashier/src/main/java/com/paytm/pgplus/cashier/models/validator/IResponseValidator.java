package com.paytm.pgplus.cashier.models.validator;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;

public interface IResponseValidator<T> {

    /*
     * @param T input class
     * 
     * @throws CashierCheckedException
     */
    void validInput(T input) throws CashierCheckedException;

    default public boolean isEmpty(Object obj) {
        return obj == null;
    }
}
