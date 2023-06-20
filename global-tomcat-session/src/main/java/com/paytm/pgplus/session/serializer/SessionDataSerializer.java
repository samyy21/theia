/**
 * 
 */
package com.paytm.pgplus.session.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.session.exception.SerializationException;

/**
 * @createdOn 20-Feb-2016
 * @author kesari
 */
public final class SessionDataSerializer implements ISessionDataSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionDataSerializer.class);

    /**
     * Default Fst Serializer configuration for Fast Binary configuration
     */
    private static final FSTConfiguration FSTCONFIG = FSTConfiguration.createFastBinaryConfiguration();
    /**
     * Buffer size to write while serializing/deserializing data
     */
    private static final int BUFFER_SIZE = 64;

    /**
     * private constructor to avoid outside visibility
     */
    private SessionDataSerializer() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.paytm.pgplus.session.serializer.ISessionDataSerializer#serialize(
     * java.lang.Object)
     */
    @Override
    public byte[] serialize(final Object serializableObject) throws SerializationException {
        try {
            LOGGER.debug("Object to be serialized is : {}", serializableObject);
            return FSTCONFIG.asByteArray(serializableObject);
        } catch (Exception ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.paytm.pgplus.session.serializer.ISessionDataSerializer#serialize(
     * java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> byte[] serialize(final Class<T> clazz, final Object serializableObject) throws SerializationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        FSTObjectOutput out = FSTCONFIG.getObjectOutput(byteArrayOutputStream);
        byte[] responseData = null;
        try {
            out.writeObject(serializableObject, clazz);
            out.flush();
            responseData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            throw new SerializationException("IO Exception occured during FST Serialization ", ex);
        } catch (Exception ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            throw new SerializationException("Exception occured during FST Serialization ", ex);
        }
        return responseData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.paytm.pgplus.session.serializer.ISessionDataSerializer#deSerialize
     * (byte[])
     */
    @Override
    public Object deSerialize(final byte[] serializedData) throws SerializationException {
        try {
            Object deserializedObject = FSTCONFIG.asObject(serializedData);
            LOGGER.debug("Deserialized Data is : {} ", deserializedObject);
            return deserializedObject;
        } catch (Exception ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            throw new SerializationException("Exception occured during FST Serialization ", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.paytm.pgplus.session.serializer.ISessionDataSerializer#deSerialize
     * (java.lang.Class, byte[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deSerialize(final Class<T> clazz, final byte[] serializedData) throws SerializationException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedData);
        FSTObjectInput in = FSTCONFIG.getObjectInput(byteArrayInputStream);
        T result = null;
        try {
            result = (T) in.readObject(clazz);
            byteArrayInputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            throw new SerializationException("IO Exception occured during FST De-Serialization ", ex);
        } catch (Exception ex) {
            LOGGER.error("Exception occured during FST Serialization ", ex);
            throw new SerializationException("Exception occured during FST De-Serialization ", ex);
        }
        return result;
    }

    /**
     * Nested class to create Singleton Instance
     * 
     * @createdOn 12-Mar-2016
     * @author kesari
     */
    private final static class SessionDataSerializerUnitGenerator {
        /**
         * Singleton instance
         */
        private static final SessionDataSerializer INSTANCE = new SessionDataSerializer();
    }

    /**
     * Method to create Singleton Instance
     * 
     * @return
     */
    public static SessionDataSerializer getInstance() {
        return SessionDataSerializerUnitGenerator.INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone not supported.");
    }

}
