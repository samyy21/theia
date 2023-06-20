package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.cashier.redis.IPgProxyCashierTransactionalRedisUtil;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.model.RouteQuery;
import com.paytm.pgplus.facade.paymentrouter.model.RouteResponse;
import com.paytm.pgplus.facade.paymentrouter.service.impl.cache.AbstractRouteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("cashierRouteRedisCache")
public class RouteRedisCache extends AbstractRouteCache {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long routeExpiryTime = Long.parseLong(ConfigurationUtil.getTheiaProperty(
            "route.redis.expiry.seconds", Constants.ROUTE_REDIS_EXPIRY_TIME_IN_SECONDS_DEFAULT));

    @Autowired
    @Qualifier("pgProxyCashierTransactionalRedisUtil")
    private IPgProxyCashierTransactionalRedisUtil redisBridge;

    @Override
    public RouteResponse getRoute(RouteQuery routeQuery) throws FacadeCheckedException {
        RouteResponse routeResponse = (RouteResponse) redisBridge.get(getKey(routeQuery));
        if (routeResponse == null) {
            logger.info("routeResponse not found in redis against routeQuery {}", routeQuery);
            routeResponse = super.getRoute(routeQuery);
            if (routeResponse.getName() != null)
                redisBridge.set(getKey(routeQuery), routeResponse, routeExpiryTime);
        } else {
            logger.info("routeResponse found in redis {}", routeResponse);
        }
        return routeResponse;
    }
}
