package com.paytm.pgplus.session.redis.connection;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

import static com.paytm.pgplus.session.config.GlobalSessionConfig.getProperty;

import java.time.Duration;
import java.util.Arrays;

public class RedisClusterClientLettuceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClusterClientLettuceService.class);

    private static StatefulRedisClusterConnection<byte[], byte[]> byteClusterConnection;

    private static RedisClusterClient clusterClient = null;

    public static final String REDIS_CLUSTER_URI = "redis.cluster.uri";
    public static final String TOPOLOGY_REFRESH_ENABLE = "redis.cluster.topologyRefreshOptions.enable";
    public static final String CANCEL_CMNDS_ON_RECONNECT_FAILURE = "redis.cluster.cancelCommandsOnReconnectFailure";
    public static final String ENABLE_PERIODIC_REFRESH = "redis.cluster.enablePeriodicRefresh";
    public static final String PERIODIC_REFRESH_PERIOD = "redis.cluster.periodicRefreshPeriod.seconds";
    public static final String ENABLE_ALL_ADAPTIVE_REFRESH_TRIGGERS = "redis.cluster.enableAllAdaptiveRefreshTriggers";
    public static final String TIMEOUT = "redis.cluster.timeout.millis";
    public static final String REFRESH_TRIGGER_RECONNECT_ATTEMPTS = "redis.cluster.refreshTriggerReconnectAttempts";

    public static final String REDIS_CLUSTER_PASSWORD = "redis.cluster.password";

    public static void connectToCluster() {

        String redisClusterUri = getProperty(REDIS_CLUSTER_URI);
        LOGGER.info("global-tomcat connecting to redisClusterUri: {}", redisClusterUri);

        String[] designSplit = redisClusterUri.split("://");
        if (designSplit.length != 2) {
            throw new IllegalArgumentException(redisClusterUri + " is not a valid Redis URI");
        }

        String[] addresses = designSplit[1].split(",");
        LOGGER.info("global-tomcat nodes participating in redis-cluster: {}", Arrays.toString(addresses));
        RedisURI[] nodes = getRedisURINodes(addresses);
        clusterClient = RedisClusterClient.create(Arrays.asList(nodes));
        setClusterClientOptions(clusterClient);
        byteClusterConnection = clusterClient.connect(new ByteArrayCodec());
    }

    public static RedisURI[] getRedisURINodes(String[] addresses) {
        RedisURI[] nodes = new RedisURI[addresses.length];
        for (int uriIndex = 0; uriIndex < addresses.length; uriIndex++) {
            String[] uriHostAndPort = addresses[uriIndex].split(":");
            String host = uriHostAndPort[0];
            int port = Integer.parseInt(uriHostAndPort[1]);
            long timeout = Long.parseLong(getProperty(TIMEOUT));
            nodes[uriIndex] = RedisURI.create(host, port);
            nodes[uriIndex].setTimeout(Duration.ofMillis(timeout));
            if (StringUtils.isNotBlank(getProperty(REDIS_CLUSTER_PASSWORD))) {
                nodes[uriIndex].setPassword(getProperty(REDIS_CLUSTER_PASSWORD));
            }
        }
        return nodes;
    }

    private static void setClusterClientOptions(RedisClusterClient clusterClient) {
        ClusterTopologyRefreshOptions topologyRefreshOptions = null;

        if (BooleanUtils.toBoolean(getProperty(TOPOLOGY_REFRESH_ENABLE))) {
            ClusterTopologyRefreshOptions.Builder builder = ClusterTopologyRefreshOptions.builder();

            builder.enablePeriodicRefresh(BooleanUtils.toBoolean(getProperty(ENABLE_PERIODIC_REFRESH)));
            builder.refreshPeriod(Duration.ofSeconds(Long.parseLong(getProperty(PERIODIC_REFRESH_PERIOD))));

            builder.refreshTriggersReconnectAttempts(Integer.parseInt(getProperty(REFRESH_TRIGGER_RECONNECT_ATTEMPTS)));

            if (BooleanUtils.toBoolean(getProperty(ENABLE_ALL_ADAPTIVE_REFRESH_TRIGGERS))) {
                builder.enableAllAdaptiveRefreshTriggers();
            }

            topologyRefreshOptions = builder.build();
        }

        clusterClient.setOptions(ClusterClientOptions
                .builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .cancelCommandsOnReconnectFailure(
                        BooleanUtils.toBoolean(getProperty(CANCEL_CMNDS_ON_RECONNECT_FAILURE))).build());
    }

    public static StatefulRedisClusterConnection<byte[], byte[]> getByteConnection() {
        try {
            if (byteClusterConnection == null) {
                synchronized (RedisClusterClientLettuceService.class) {
                    if (byteClusterConnection == null) {
                        LOGGER.info("creating cluster connection in global-tomcat");
                        byteClusterConnection = clusterClient.connect(new ByteArrayCodec());
                    }
                }
            }
            return byteClusterConnection;
        } catch (Exception e) {
            LOGGER.error("global-tomcat Failed to get redis cluster connection! ", e);
        }
        throw new RuntimeException("global-tomcat Failed to get redis cluster connection");
    }

}
