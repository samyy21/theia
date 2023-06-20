package com.paytm.pgplus.theia.offline.cache;

/**
 * Created by rahulverma on 16/9/17.
 */

import com.paytm.pgplus.cache.util.CacheProperty;
import com.paytm.pgplus.cache.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;

import java.util.*;

//@Configuration
//@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

    // @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        String redisURI = getProperty(Constants.REDIS_URL);
        LOGGER.debug("Redis URI Detected as : {} ", redisURI);
        String[] splitRedisURI = splitRedisURI(redisURI);
        String scheme = splitRedisURI[0];
        String clusterInfo = splitRedisURI[1];

        JedisConnectionFactory redisConnectionFactory = null;
        if (isSentinelScheme(scheme)) {
            redisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(clusterInfo),
                    jedisPoolConfig());
            redisConnectionFactory.setShardInfo(jedisShardInfo(null));
        } else {
            redisConnectionFactory = new JedisConnectionFactory(jedisShardInfo(clusterInfo));
        }

        return redisConnectionFactory;

    }

    // @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    // @Bean(name = "springRedisCM")
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        // default time in sec.
        cacheManager.setDefaultExpiration(900);
        return cacheManager;
    }

    /*
     * @Bean(name = "cashierInfoResponseCacheManager") public CacheManager
     * cashierInfoResponseCacheManager(RedisTemplate redisTemplate) {
     * RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate); //
     * default time in sec. cacheManager.setDefaultExpiration(900); return
     * cacheManager; }
     */

    /*
     * @Bean public KeyGenerator customKeyGenerator() { return new
     * KeyGenerator() {
     * 
     * @Override public Object generate(Object o, Method method, Object...
     * objects) { StringBuilder sb = new StringBuilder();
     * sb.append(o.getClass().getName()); sb.append(method.getName()); for
     * (Object obj : objects) { sb.append(obj.toString()); } return
     * sb.toString(); } }; }
     */

    private String[] splitRedisURI(String redisURI) {
        String[] schemeSplit = redisURI.split("://");
        if (schemeSplit.length != 2) {
            throw new IllegalArgumentException(redisURI + " is not a valid Redis URI");
        }
        return schemeSplit;
    }

    private boolean isSentinelScheme(String scheme) {
        boolean isSentinelScheme = false;
        if (scheme.equals("redis-sentinel")) {
            isSentinelScheme = true;
        } else if (!scheme.equals("redis")) {
            throw new IllegalArgumentException(scheme + " is not a valid Redis Scheme");
        }
        LOGGER.debug("Using is Sentinel Enabled as : {}", isSentinelScheme);
        return isSentinelScheme;
    }

    private RedisSentinelConfiguration redisSentinelConfiguration(String clusterInfo) {
        String clusterName = clusterName(clusterInfo);
        String[] addresses = clusterAddresses(clusterInfo);
        Set<String> sentinels = new HashSet<String>(Arrays.asList(addresses));
        List<RedisNode> sentinelNodes = new ArrayList<>();
        for (String sentinel : sentinels) {
            String[] hostAndPort = sentinel.split(":");
            String host = hostAndPort[0];
            int port = 26379;
            if (hostAndPort.length == 2) {
                port = Integer.parseInt(hostAndPort[1]);
            }
            sentinelNodes.add(new RedisNode(host, port));
        }

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration().master(clusterName);
        redisSentinelConfiguration.setSentinels(sentinelNodes);
        return redisSentinelConfiguration;
    }

    private JedisShardInfo jedisShardInfo(String clusterInfo) {
        JedisShardInfo jedisShardInfo = null;
        if (!StringUtils.isEmpty(clusterInfo)) {
            String[] addresses = clusterAddresses(clusterInfo);
            String[] hostAndPort = addresses[0].split(":");
            String host = hostAndPort[0];
            int port = 6379;
            if (hostAndPort.length == 2) {
                port = Integer.parseInt(hostAndPort[1]);
            }
            jedisShardInfo = new JedisShardInfo(host, port);
        } else
            jedisShardInfo = new JedisShardInfo("localhost", 6379);

        jedisShardInfo.setPassword(getProperty(Constants.REDIS_PASSWORD, null));
        jedisShardInfo.setConnectionTimeout(Protocol.DEFAULT_TIMEOUT);
        return jedisShardInfo;
    }

    private String[] clusterAddresses(String clusterInfo) {
        String[] clusterSplit = clusterInfo.split("#");
        if (clusterSplit.length > 2) {
            throw new IllegalArgumentException(clusterInfo + " is not a valid Redis URI");
        }
        String[] addresses = clusterSplit[0].split("/")[0].split(",");
        return addresses;
    }

    private String clusterName(String clusterInfo) {
        String[] clusterSplit = clusterInfo.split("#");
        if (clusterSplit.length > 2) {
            throw new IllegalArgumentException(clusterInfo + " is not a valid Redis URI");
        }
        String clusterName = Constants.DEFAULT_SENTINEL_CLUSTER_NAME;
        if (clusterSplit.length == 2) {
            clusterName = clusterSplit[1];
        }
        LOGGER.debug("Using Redis Cluster name as : {} ", clusterName);
        return clusterName;
    }

    private static JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(getProperty(Constants.REDIS_MAX_TOTAL, "500")));
        poolConfig.setMaxIdle(Integer.parseInt(getProperty(Constants.REDIS_MAX_IDLE, "100")));
        poolConfig.setMinIdle(Integer.parseInt(getProperty(Constants.REDIS_MIN_IDLE, "10")));
        poolConfig.setMaxWaitMillis(Integer.parseInt(getProperty(Constants.REDIS_MAX_WAIT, "2000")));
        poolConfig.setFairness(true);
        poolConfig.setBlockWhenExhausted(false);
        poolConfig.setTestOnCreate(true);
        return poolConfig;
    }

    private static String getProperty(String propertyName, String defaultValue) {
        return CacheProperty.getProperties().getProperty(propertyName, defaultValue);
    }

    private static String getProperty(String propertyName) {
        return CacheProperty.getProperties().getProperty(propertyName);
    }

}
