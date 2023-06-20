package com.paytm.pgplus.theia.redis.impl;

import com.paytm.pgplus.redis.IRedisBridge;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("theiaSessionRedisUtil")
public class TheiaSessionRedisUtil implements ITheiaSessionRedisUtil {

    @Autowired
    @Qualifier("sessionRedisBridge")
    private IRedisBridge sessionRedisBridge;

    @Override
    public void set(String key, Object value, long ttl) {
        sessionRedisBridge.set(key, value, ttl);
    }

    @Override
    public Object get(String key) {
        return sessionRedisBridge.get(key);
    }

    @Override
    public void del(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            sessionRedisBridge.del(keys[i]);
        }

    }

    @Override
    public void hset(String key, String field, Object value, long ttlInSeconds) {
        sessionRedisBridge.hset(key, field, value, ttlInSeconds);
    }

    @Override
    public Object hget(String key, String field) {
        return sessionRedisBridge.hget(key, field);
    }

    @Override
    public void hdel(String key, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            sessionRedisBridge.hdel(key, fields[i]);
        }
    }

    @Override
    public boolean setnx(String key, Object value, long ttlInSeconds) {
        return sessionRedisBridge.setnx(key, value, ttlInSeconds);
    }

    @Override
    public boolean expire(String key, long ttlInSeconds) {
        return sessionRedisBridge.expire(key, ttlInSeconds);
    }

    @Override
    public boolean isExist(String key) {
        return sessionRedisBridge.isExist(key);
    }

    @Override
    public Object increment(String key) {
        return sessionRedisBridge.increment(key);
    }

    @Override
    public boolean hsetIfExist(String key, String field, Object value) {
        return sessionRedisBridge.hsetIfExist(key, field, value);
    }

    @Override
    public Long ttl(String key) {
        return sessionRedisBridge.ttl(key);
    }
}
