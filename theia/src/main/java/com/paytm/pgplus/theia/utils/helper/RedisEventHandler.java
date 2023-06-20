package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.cache.redis.cluster.RedisClusterClientLettuceService;
import com.paytm.pgplus.cache.redis.cluster.SessionRedisClusterClientLettuceService;
import com.paytm.pgplus.cache.redis.cluster.TransactionalRedisClusterClientLettuceService;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.EventBus;
import io.lettuce.core.event.cluster.AdaptiveRefreshTriggeredEvent;
import io.lettuce.core.event.connection.ReconnectFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEventHandler.class);
    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("EVENT_LOGGER");
    private static boolean eventSubscribedOnRedisCluster = false;
    private static boolean eventSubscribedOnSessionRedisCluster = false;
    private static boolean eventSubscribedOnTransactionalRedisCluster = false;

    public static void enableRedisEventsOnAllRedis() {
        EVENT_LOGGER.info("Adding events on redis clusters");
        if (RedisClusterClientLettuceService.getSharedRedisClusterClient() != null && !eventSubscribedOnRedisCluster) {
            eventSubscribedOnRedisCluster = true;
            addEventsOnRedisClusterClient(RedisClusterClientLettuceService.getSharedRedisClusterClient(),
                    RedisClusterClientLettuceService.class);
        }
        if (TransactionalRedisClusterClientLettuceService.getSharedTransactionalClusterClient() != null
                && !eventSubscribedOnTransactionalRedisCluster) {
            eventSubscribedOnTransactionalRedisCluster = true;
            addEventsOnRedisClusterClient(
                    TransactionalRedisClusterClientLettuceService.getSharedTransactionalClusterClient(),
                    TransactionalRedisClusterClientLettuceService.class);
        }
        if (SessionRedisClusterClientLettuceService.getSharedSessionClusterClient() != null
                && !eventSubscribedOnSessionRedisCluster) {
            eventSubscribedOnSessionRedisCluster = true;
            addEventsOnRedisClusterClient(SessionRedisClusterClientLettuceService.getSharedSessionClusterClient(),
                    SessionRedisClusterClientLettuceService.class);
        }

    }

    private static void addEventsOnRedisClusterClient(RedisClusterClient redisClusterClient, Class redisClassName) {
        try {
            EVENT_LOGGER.info("Adding events on redis cluster : {}", redisClassName);
            EventBus eventBus = redisClusterClient.getResources().eventBus();
            eventBus.get().subscribe(
                    event -> {
                        if (event instanceof ClusterTopologyChangedEvent) {
                            try {
                                EVENT_LOGGER.info("{} ClusterTopologyChangedEvent before:{} and after :{}",
                                        redisClassName, ((ClusterTopologyChangedEvent) event).before(),
                                        ((ClusterTopologyChangedEvent) event).after());
                            } catch (Exception e) {
                                LOGGER.error("{} ClusterTopologyChangedEvent exception :{}", redisClassName, e);
                            }
                        } else if (event instanceof ReconnectFailedEvent) {
                            try {
                                EVENT_LOGGER.info("{} ReconnectFailedEvent event log attempt :{} and cause:{}",
                                        redisClassName, ((ReconnectFailedEvent) event).getAttempt(),
                                        ((ReconnectFailedEvent) event).getCause());
                            } catch (Exception e) {
                                LOGGER.error("{} ReconnectFailedEvent -  exception :{}", redisClassName, e);
                            }
                        } else if (event instanceof AdaptiveRefreshTriggeredEvent) {
                            try {
                                EVENT_LOGGER.info("{} AdaptiveRefreshTriggeredEvent event log:{}", redisClassName,
                                        event.toString());
                            } catch (Exception e) {
                                LOGGER.error("{} AdaptiveRefreshTriggeredEvent exception :{}", redisClassName, e);
                            }
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("{}:- Exception in subscripting event : {}", redisClassName, e.getMessage());
        }
    }

}
