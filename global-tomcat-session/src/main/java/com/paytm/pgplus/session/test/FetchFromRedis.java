/**
 * 
 */
package com.paytm.pgplus.session.test;

import java.util.List;
import java.util.Map;

import com.paytm.pgplus.session.redis.operation.RedisOperation;
import com.paytm.pgplus.session.serializer.ISessionDataSerializer;
import com.paytm.pgplus.session.serializer.SessionDataSerializer;

/**
 * @createdOn 18-Jun-2016
 * @author kesari
 */
public class FetchFromRedis {

    private static ISessionDataSerializer serializer = SessionDataSerializer.getInstance();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Please provide mid and order id");
            return;
        }
        String mId = args[0];
        String orderId = args[1];
        System.out.println("Fetching data for mid :" + mId + ", OrderId :" + orderId);

        byte[] redisKey = serializer.serialize(mId + ":" + orderId);

        show(redisKey);
    }

    private static void show(byte[] redisKey) {
        List<byte[]> serializedSessionDataList = RedisOperation.getBinaryValueByKeyFromMap(redisKey, redisKey);
        byte[] serializedSessionData = null;
        if (serializedSessionDataList != null && !serializedSessionDataList.isEmpty()) {
            serializedSessionData = serializedSessionDataList.get(0);
        }
        if (serializedSessionData != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionDataMap = (Map<String, Object>) serializer.deSerialize(serializedSessionData);
            System.out.println("Redis data :" + sessionDataMap);
        } else {
            System.out.println("No data found in redis");
        }
    }
}
