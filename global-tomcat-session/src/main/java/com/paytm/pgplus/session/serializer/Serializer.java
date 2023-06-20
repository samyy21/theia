/**
 *      
 */
package com.paytm.pgplus.session.serializer;

import com.paytm.pgplus.session.exception.SerializationException;

/**
 * 
 * @createdOn 21-Jun-2016
 * @author kesari
 */
public interface Serializer {
    public byte[] serialize(Class<? extends Object> classType, Object obj) throws SerializationException;

    public Object deserialize(Class<? extends Object> classType, byte[] data) throws SerializationException;
}
