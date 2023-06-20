package com.paytm.pgplus.theia.redis;

public interface ITheiaSessionRedisUtil {

    void set(String key, Object value, long ttl);

    Object get(String key);

    void del(String... keys);

    void hset(String key, String field, Object value, long ttlInSeconds);

    Object hget(String key, String field);

    void hdel(String key, String... fields);

    boolean setnx(String key, Object value, long ttlInSeconds);

    boolean expire(String key, long ttlInSeconds);

    boolean isExist(String key);

    Object increment(String key);

    boolean hsetIfExist(String key, String field, Object value);

    Long ttl(String key);
}
