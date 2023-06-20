package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.model.RouteQuery;
import com.paytm.pgplus.facade.paymentrouter.model.RouteResponse;
import com.paytm.pgplus.facade.paymentrouter.service.IRouteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RouteUtil {

    @Autowired
    @Qualifier("cashierRouteRedisCache")
    IRouteClient routeClient;

    public Routes getRoute(String mid, String orderId, String acquirementId, String cashierRequestId, String api)
            throws FacadeCheckedException {
        RouteResponse routeResponse = makeRouteQuery(mid, orderId, acquirementId, cashierRequestId, api);
        return routeResponse.getName();
    }

    public RouteResponse getRouteResponse(String mid, String orderId, String acquirementId, String cashierRequestId,
            String api) throws FacadeCheckedException {
        RouteResponse routeResponse = makeRouteQuery(mid, orderId, acquirementId, cashierRequestId, api);
        return routeResponse;
    }

    private RouteResponse makeRouteQuery(String mid, String orderId, String acquirementId, String cashierRequestId,
            String api) throws FacadeCheckedException {
        RouteResponse routeResponse;
        RouteQuery routeQuery = new RouteQuery();
        routeQuery.setMid(mid);
        routeQuery.setOrderId(orderId);
        routeQuery.setCahsierRequestId(cashierRequestId);
        routeQuery.setAcquirementId(acquirementId);
        routeQuery.setApi(api);
        routeResponse = routeClient.getRoute(routeQuery);
        return routeResponse;
    }
}
