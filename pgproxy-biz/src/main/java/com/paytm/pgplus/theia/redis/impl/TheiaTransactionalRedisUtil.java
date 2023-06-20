package com.paytm.pgplus.theia.redis.impl;

import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.redis.IRedisBridge;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("theiaTransactionalRedisUtil")
public class TheiaTransactionalRedisUtil implements ITheiaTransactionalRedisUtil {

    @Autowired
    @Qualifier("transactionalRedisBridge")
    private IRedisBridge transactionalRedisBridge;

    @Override
    public void set(String key, Object value, long ttl) {
        transactionalRedisBridge.set(key, value, ttl);
    }

    @Override
    public void set(String key, Object value) {
        transactionalRedisBridge.set(key, value,
                Constants.TransactionalRedisCluster.TRANSACTIONAL_REDIS_DEFAULT_EXPIRE_TIME_IN_SECONDS);
    }

    @Override
    public Object get(String key) {
        return transactionalRedisBridge.get(key);
    }

    @Override
    public void del(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            transactionalRedisBridge.del(keys[i]);
        }

    }

    @Override
    public void hset(String key, String field, Object value, long ttlInSeconds) {
        transactionalRedisBridge.hset(key, field, value, ttlInSeconds);
    }

    @Override
    public Object hget(String key, String field) {
        return transactionalRedisBridge.hget(key, field);
    }

    @Override
    public void hdel(String key, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            transactionalRedisBridge.hdel(key, fields[i]);
        }
    }

    @Override
    public boolean setnx(String key, Object value, long ttlInSeconds) {
        return transactionalRedisBridge.setnx(key, value, ttlInSeconds);
    }

    @Override
    public boolean expire(String key, long ttlInSeconds) {
        return transactionalRedisBridge.expire(key, ttlInSeconds);
    }

    @Override
    public boolean isExist(String key) {
        return transactionalRedisBridge.isExist(key);
    }

    @Override
    public Object increment(String key) {
        return transactionalRedisBridge.increment(key);
    }

    @Override
    public boolean hsetIfExist(String key, String field, Object value) {
        return transactionalRedisBridge.hsetIfExist(key, field, value);
    }

    @Override
    public Long ttl(String key) {
        return transactionalRedisBridge.ttl(key);
    }
}
