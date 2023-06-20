package com.paytm.pgplus.theia.datamapper;

import com.paytm.pgplus.theia.datamapper.dto.BasePayMethodDTO;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;

/**
 * 
 * @author ruchikagarg
 * @param <T>
 */
public abstract class PayMethodBuilder<T extends BasePayMethodDTO> {

    protected T obj;

    public PayMethodBuilder<T> setObj(T obj) {
        this.obj = obj;
        return this;
    }

    public abstract void build() throws RequestValidationException, PaymentRequestProcessingException;

}