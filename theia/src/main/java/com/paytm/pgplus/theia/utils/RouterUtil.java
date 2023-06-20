package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.model.RouteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RouterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterUtil.class);

    public Routes getRoute(String mid, String orderId, String api) {
        return getRoute(mid, orderId, null, null, api);
    }

    public Routes getRoute(String mid, String orderId, String acquirementId, String cashierRequestId, String api) {
        return Routes.PG2;
    }

    public RouteResponse getRouteResponse(String mid, String orderId, String acquirementId, String cashierRequestId,
            String api) {
        RouteResponse routeResponse = new RouteResponse();
        routeResponse.setName(Routes.PG2);
        return routeResponse;
    }

}
