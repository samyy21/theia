package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pgplus.cache.redis.RedisClientJedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Created by charuaggarwal on 7/7/17.
 */

@Component
public class TestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    void cleanUpRedisBeforeTest() {
        Jedis jedis = RedisClientJedisService.getInstance().getJedis();
        try {
            Set<String> jedisKeys = jedis.keys("USER_CARDS*");
            for (String key : jedisKeys) {
                try {
                    LOG.info("Deleting Key in redis: {}", key);
                    RedisClientJedisService.getInstance().delete(key);
                } catch (Exception e) {
                    LOG.error("Error occured while deleting key from redis!", e);
                }
            }
        } catch (Exception e) {
            LOG.error("Redis cleanup failure due to an exception! Some tests might fail: ", e);
        }
    }
}
