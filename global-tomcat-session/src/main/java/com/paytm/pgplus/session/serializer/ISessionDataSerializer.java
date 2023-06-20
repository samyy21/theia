/**
 * 
 */
package com.paytm.pgplus.session.serializer;

import com.paytm.pgplus.session.exception.SerializationException;

/**
 * @createdOn 20-Feb-2016
 * @author kesari
 */
public interface ISessionDataSerializer {

    /**
     * @param serializableObject
     * @return
     * @throws SerializationException
     */
    byte[] serialize(Object serializableObject) throws SerializationException;

    /**
     * @param serializedData
     * @return
     * @throws SerializationException
     */
    Object deSerialize(byte[] serializedData) throws SerializationException;

    /**
     * @param clazz
     * @param serializableObject
     * @return
     * @throws SerializationException
     */
    <T> byte[] serialize(Class<T> clazz, Object serializableObject) throws SerializationException;

    /**
     * @param clazz
     * @param serializedData
     * @return
     * @throws SerializationException
     */
    <T> T deSerialize(Class<T> clazz, byte[] serializedData) throws SerializationException;

}
