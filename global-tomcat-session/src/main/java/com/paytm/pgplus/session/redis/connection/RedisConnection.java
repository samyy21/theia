package com.paytm.pgplus.session.redis.connection;

import static com.paytm.pgplus.session.config.GlobalSessionConfig.getProperty;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_CLUSTER_NAME;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_HOST;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_MAX_IDLE;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_MAX_TOTAL;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_MAX_WAIT;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_MIN_IDLE;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_PASSWORD;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_PORT;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_SENTINEL_ENABLED;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.REDIS_SENTINEL_SERVERS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

/**
 * @author kesari
 *
 */
/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
public final class RedisConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConnection.class);

    /**
	 * 
	 */
    private JedisPool jedisPool = null;
    /**
	 * 
	 */
    private JedisSentinelPool jedisSentinelPool = null;
    /**
	 * 
	 */
    private boolean connInitialized = Boolean.FALSE;

    /**
	 * 
	 */
    private RedisConnection() {
        initializeRedisConnection();
    }

    static {
        getInstance();
    }

    /**
	 * 
	 */
    public void initializeRedisConnection() {
        if (connInitialized) {
            return;
        }
        try {
            boolean sentinelEnabled = Boolean.valueOf(getProperty(REDIS_SENTINEL_ENABLED));
            String sentinelServers = getProperty(REDIS_SENTINEL_SERVERS);
            if (sentinelEnabled) {
                Set<String> sentinels = new HashSet<String>(Arrays.asList(sentinelServers.split(",")));
                jedisSentinelPool = new JedisSentinelPool(getProperty(REDIS_CLUSTER_NAME, "mymaster"), sentinels,
                        getJedisPoolConfig(), getProperty(REDIS_PASSWORD, null));
            } else {
                jedisPool = new JedisPool(getJedisPoolConfig(), getProperty(REDIS_HOST),
                        Integer.valueOf(getProperty(REDIS_PORT)), Protocol.DEFAULT_TIMEOUT, getProperty(REDIS_PASSWORD,
                                null), Protocol.DEFAULT_DATABASE);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception in connecting to redis", ex);
        }
        connInitialized = Boolean.TRUE;

    }

    /**
     * 
     * @return
     */
    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(getProperty(REDIS_MAX_TOTAL)));
        poolConfig.setMaxIdle(Integer.parseInt(getProperty(REDIS_MAX_IDLE)));
        poolConfig.setMinIdle(Integer.parseInt(getProperty(REDIS_MIN_IDLE)));
        poolConfig.setMaxWaitMillis(Integer.parseInt(getProperty(REDIS_MAX_WAIT)));
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    /**
     * 
     * @return
     */
    public static Jedis getJedis() {
        RedisConnection conn = getInstance();
        if (Boolean.valueOf(getProperty(REDIS_SENTINEL_ENABLED))) {
            if (conn.jedisSentinelPool == null) {
                conn.connInitialized = false;
                conn.initializeRedisConnection();
            }
            return conn.jedisSentinelPool.getResource();
        } else {
            if (conn.jedisPool == null) {
                conn.connInitialized = false;
                conn.initializeRedisConnection();
            }
            return conn.jedisPool.getResource();
        }
    }

    /**
     * Inner static class to generate singleton instance
     * 
     * @createdOn 12-Mar-2016
     * @author kesari
     */
    private static class RedisConnectionFactoryUnitGenerator {
        /**
         * Singleton instance of RedisConnection
         */
        private static final RedisConnection redisConnectionFactory = new RedisConnection();
    }

    /**
     * Method generate singleton instance
     * 
     * @return
     */
    private static RedisConnection getInstance() {
        return RedisConnectionFactoryUnitGenerator.redisConnectionFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("RedisConnectionFactory can not be cloned");
    }
}
