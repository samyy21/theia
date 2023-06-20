/**
 *      
 */
package com.paytm.pgplus.session.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.paytm.pgplus.session.exception.SerializationException;

/**
 * @author fanendra
 * @createdOn 04-Dec-2014
 * @since
 */
public class ProtoStuffSerializer implements Serializer {
    /**
     * Default buffer allocation size while serializing the object.s
     */
    private static final int BUFFER_ALLOCATION_SIZE = 1024;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public byte[] serialize(java.lang.Class<? extends Object> classType, Object obj) throws SerializationException {
        LinkedBuffer buffer = LinkedBuffer.allocate(BUFFER_ALLOCATION_SIZE);
        byte[] data;
        try {
            Schema schema = RuntimeSchema.getSchema(classType);
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializationException("Error while serializing object of class: " + classType, e);
        } finally {
            buffer.clear();
        }
        return data;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object deserialize(Class<? extends Object> classType, byte[] data) throws SerializationException {
        Schema schema = RuntimeSchema.getSchema(classType);
        Object object = null;
        try {
            object = classType.newInstance();
        } catch (InstantiationException e) {
            throw new SerializationException("Unable to de-serialize object of class: " + classType
                    + ". You can check for the class structure.", e);
        } catch (IllegalAccessException e) {
            throw new SerializationException("Unable to de-serialize object of class: " + classType
                    + ". You can check for access modifier for the defult constructor.", e);
        }
        ProtostuffIOUtil.mergeFrom(data, object, schema);
        return object;
    }
}
