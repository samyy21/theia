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

import com.paytm.pgplus.session.exception.SerializationException;

/**
 * 
 * @createdOn 21-Jun-2016
 * @author kesari
 */
public class FSTSerializer implements Serializer {
    /**
     * Default buffer size for {@link ByteArrayOutputStream} created while
     * serializing an object.
     */
    private static final int BUFFER_SIZE = 64;
    /**
	 * 
	 */
    private final FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration();

    /**
     * 
     * @param classType
     * @param obj
     * @return
     * @throws SerializationException
     */
    @Override
    public byte[] serialize(Class<? extends Object> classType, Object obj) throws SerializationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        FSTObjectOutput out = conf.getObjectOutput(byteArrayOutputStream);
        byte[] responseData = null;
        try {
            out.writeObject(obj, Object.class);
            out.flush();
            responseData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new SerializationException("IO Exception occured during FST Serialization ", e);
        } catch (Exception e) {
            throw new SerializationException("Exception occured during FST Serialization ", e);
        }
        return responseData;
    }

    /**
     * 
     * @param data
     * @param classType
     * @return
     * @throws SerializationException
     */
    @Override
    public Object deserialize(Class<? extends Object> classType, byte[] data) throws SerializationException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        FSTObjectInput in = conf.getObjectInput(byteArrayInputStream);
        Object result = null;
        try {
            result = (Object) in.readObject(Object.class);
            byteArrayInputStream.close();
        } catch (IOException e) {
            throw new SerializationException("IO Exception occured during FST De-Serialization ", e);
        } catch (Exception e) {
            throw new SerializationException("Exception occured during FST De-Serialization ", e);
        }
        return result;
    }

    /**
     * 
     * @param obj
     * @return
     * @throws SerializationException
     */
    public byte[] doSerialize(Object obj) throws SerializationException {
        try {
            return conf.asByteArray(obj);
        } catch (Exception e) {
            throw new SerializationException("Exception occured during FST Serialization ", e);
        }
    }

    /**
     * 
     * @param data
     * @return
     * @throws SerializationException
     */
    public Object doDeserialize(byte[] data) throws SerializationException {
        try {
            return conf.asObject(data);
        } catch (Exception e) {
            throw new SerializationException("Exception occured during FST De-Serialization ", e);
        }
    }
}
