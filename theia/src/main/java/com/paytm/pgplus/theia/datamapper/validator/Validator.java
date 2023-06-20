package com.paytm.pgplus.theia.datamapper.validator;

import com.paytm.pgplus.theia.exceptions.TheiaServiceException;

/**
 * 
 * @author ruchikagarg
 *
 * @param <T>
 */
public interface Validator<T> {

    void validate(T obj) throws TheiaServiceException;
}
