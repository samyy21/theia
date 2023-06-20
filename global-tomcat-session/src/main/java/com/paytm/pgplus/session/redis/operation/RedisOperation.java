/**
 *
 */
package com.paytm.pgplus.session.redis.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import com.paytm.pgplus.session.constant.ProjectConstant;
import com.paytm.pgplus.session.redis.connection.RedisClusterClientLettuceService;
import io.lettuce.core.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Pipeline;

import static com.paytm.pgplus.session.config.GlobalSessionConfig.getProperty;

import com.paytm.pgplus.session.redis.connection.RedisConnection;

/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
public final class RedisOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisOperation.class);

    /**
     * @throws OperationNotSupportedException
     */
    private RedisOperation() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Instantiaion not supported.");
    }

    /**
     *
     * @param key
     * @param field
     * @return
     */
    public static List<byte[]> getBinaryValueByKeyFromMap(byte[] key, byte[] field) {
        if (isReadFromCluster()) {
            try {
                List<KeyValue<byte[], byte[]>> binaryKeyValueList = RedisClusterClientLettuceService
                        .getByteConnection().sync().hmget(key, field);
                if (binaryKeyValueList != null && binaryKeyValueList.size() > 0) {
                    List<byte[]> binaryValues = new ArrayList<>(0);
                    for (KeyValue<byte[], byte[]> binaryKeyValue : binaryKeyValueList) {
                        binaryValues.add(binaryKeyValue.getValue());
                    }
                    return binaryValues;

                }
            } catch (Exception e) {
                LOGGER.error("error while fetching binary map value from cluster, {}", e.getMessage());
            }
        }
        if (!isReadFromSentinel()) {
            LOGGER.error("failed to read map value from cluster and property disabled for sentinel");
            return null;
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            return binaryJedis.hmget(key, field);
        } catch (Exception ex) {
            LOGGER.error("Exception occured for jedis ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return null;
    }

    /**
     *
     * @param key
     * @return
     */
    public static Map<byte[], byte[]> getMapByBinaryKey(byte[] key) {
        if (isReadFromCluster()) {
            try {
                return RedisClusterClientLettuceService.getByteConnection().sync().hgetall(key);
            } catch (Exception e) {
                LOGGER.error("error while reading map from binary key in cluster", e);
            }
        }
        if (!isReadFromSentinel()) {
            LOGGER.error("failed to read map value from binary kry in cluster and property disabled for sentinel");
            return null;
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            return binaryJedis.hgetAll(key);
        } catch (Exception ex) {
            LOGGER.error("Exception occured while fetching from redis : ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return null;
    }

    /**
     *
     * @param key
     * @param seconds
     * @return
     */
    public static Long setExpiryOfBinaryKeyInSeconds(byte[] key, int seconds) {
        if (isWriteOnCluster()) {
            try {
                RedisClusterClientLettuceService.getByteConnection().sync().expire(key, seconds);
            } catch (Exception e) {
                LOGGER.error("error while setting expiry in cluster", e);
            }
            if (!isWriteOnSentinel()) {
                return 0l;
            }
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            return binaryJedis.expire(key, seconds);
        } catch (Exception ex) {
            LOGGER.error("Exception occured while fetching from redis : ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return null;
    }

    /**
     *
     * @param key
     * @param seconds
     * @param values
     * @return
     */
    public static List<Object> setBinaryValuesWithExpiryInPipeline(byte[] key, int seconds, Map<byte[], byte[]> values) {
        if (isWriteOnCluster()) {
            try {
                RedisClusterClientLettuceService.getByteConnection().sync().hmset(key, values);
                RedisClusterClientLettuceService.getByteConnection().sync().expire(key, seconds);
            } catch (Exception e) {
                LOGGER.error("error while setting binary values with expiry in pipeline in cluster", e);
            }
            if (!isWriteOnSentinel()) {
                return null;
            }
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            Pipeline pipeline = binaryJedis.pipelined();
            pipeline.hmset(key, values);
            pipeline.expire(key, seconds);
            List<Object> resultList = pipeline.syncAndReturnAll();
            return resultList;
        } catch (Exception ex) {
            LOGGER.error("Exception occured while fetching from redis : ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return null;
    }

    /**
     * @param key
     * @param seconds
     * @param values
     * @return
     */
    public static boolean setBinaryValuesWithExpiry(byte[] key, int seconds, Map<byte[], byte[]> values) {
        boolean isBinaryValueSet = false;
        if (isWriteOnCluster()) {
            try {
                RedisClusterClientLettuceService.getByteConnection().sync().hmset(key, values);
                RedisClusterClientLettuceService.getByteConnection().sync().expire(key, seconds);
                isBinaryValueSet = true;
            } catch (Exception e) {
                LOGGER.error("error while setting binary values with expiry in cluster", e);
            }
        }
        if (!isWriteOnSentinel()) {
            return isBinaryValueSet;
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            String result1 = binaryJedis.hmset(key, values);
            Long result2 = binaryJedis.expire(key, seconds);
            LOGGER.debug("Results of HMSET : {} and Expire : {} ", result1, result2);
            return true;
        } catch (Throwable ex) {
            LOGGER.error("Exception occured while fetching from redis : ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return false;
    }

    /**
     *
     * @param key
     * @param fields
     * @return
     */
    public static Long deleteFieldsOfBinaryKeyFromMap(byte[] key, byte[]... fields) {
        Long value = null;
        if (isWriteOnCluster()) {
            try {
                value = RedisClusterClientLettuceService.getByteConnection().sync().hdel(key, fields);
            } catch (Exception e) {
                LOGGER.error("error while deleting keys from cluster", e);
            }
        }
        if (!isWriteOnSentinel()) {
            return value;
        }
        BinaryJedis binaryJedis = null;
        try {
            binaryJedis = RedisConnection.getJedis();
            return binaryJedis.hdel(key, fields);
        } catch (Exception ex) {
            LOGGER.error("Exception occured while deleting fields from redis : ", ex);
        } finally {
            if (binaryJedis != null) {
                binaryJedis.close();
            }
        }
        return null;
    }

    private static boolean isReadFromCluster() {
        return "true".equals(getProperty(ProjectConstant.Configurations.GLOBAL_TOMCAT_READ_FROM_CLUSTER, "false"));
    }

    private static boolean isWriteOnCluster() {
        return "true".equals(getProperty(ProjectConstant.Configurations.GLOBAL_TOMCAT_WRITE_ON_CLUSTER, "false"));
    }

    private static boolean isReadFromSentinel() {
        return "true".equals(getProperty(ProjectConstant.Configurations.GLOBAL_TOMCAT_READ_FROM_SENTINEL, "true"));
    }

    private static boolean isWriteOnSentinel() {
        return "true".equals(getProperty(ProjectConstant.Configurations.GLOBAL_TOMCAT_WRITE_ON_SENTINEL, "true"));
    }
}
