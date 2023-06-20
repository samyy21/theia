package com.paytm.pgplus.session.oldpg;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.session.redis.operation.RedisOperation;

public class RedisSessionHandlerValve extends ValveBase {

    private static final String SESSION_ID = "requestId";
    private RedisSessionManager manager;
    public static ThreadLocal<Request> currentRequest = new ThreadLocal<Request>();
    public static ThreadLocal<Response> currentResponse = new ThreadLocal<Response>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionHandlerValve.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setRedisSessionManager(RedisSessionManager manager) {

        this.manager = manager;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        LOGGER.info("Entering. url = {} {}", request.getRequestURI(), request.getQueryString());
        String orderId = request.getParameter("ORDER_ID");
        String mid = request.getParameter("MID");
        String route = request.getParameter("route");

        // if (StringUtils.isEmpty(orderId) &&
        // (("dmrcsm68048285579320".equalsIgnoreCase(mid) ||
        // "ANiwVi73906171009995".equalsIgnoreCase(mid)))) {
        // orderId = getOrderFromJson(request.getParameter("data"));
        // }
        //
        LOGGER.info("ORder id is   ::" + orderId);
        // Below Code for Generic Txn
        // if(StringUtils.isEmpty(orderId) && !StringUtils.isEmpty(mid)){
        // LOGGER.info("Entering ");
        // String list = RedisOperation.getValueByKey("MidOrderIdMapping");//
        // maps (Mid )to (specific order ID Param Name)
        // // format Mid1:Order1|Mid2:Order2|Mid3:Order3
        // LOGGER.info("List  "+list);
        // String[] pairs = list.split("\\|");
        // LOGGER.info("MID is:  "+mid);
        // for(String pair:pairs){
        // LOGGER.info("PAIR  ="+pair);
        // if(pair.startsWith(mid)){
        // String orderParamName = pair.split(":")[1];// dont handle error ,
        // Redis must be correct
        // orderId=request.getParameter(orderParamName);
        // break;
        // }
        // }
        // }
        LOGGER.info("Exiting ORder id is   ::" + orderId);

        if (request.getParameter("state") != null) {

            String[] stateValues = request.getParameter("state").split(":");
            orderId = stateValues[0];
            mid = stateValues[1];
            if (stateValues.length >= 8) {
                route = stateValues[7];
            }

            if (request.getCookies() == null) {
                request.setAttribute("jvmSessionIdInUrl", stateValues[4]);
            }
        }

        if (mid != null) {

            if (orderId != null) {
                MDC.put(SESSION_ID,
                        new StringBuilder(mid).append("-").append(orderId).append("-").append(manager.getJvmRoute())
                                .toString());
                request.setAttribute("ORDER_ID", orderId);
            }
            request.setAttribute("MID", mid);
            request.setAttribute("route", route);
            request.setAttribute("jvmRoute", manager.getJvmRoute());
            request.setAttribute("localSessionFound", false);
            request.setAttribute("saveToRedis", false);
            currentRequest.set(request);
            currentResponse.set(response);
        }
        getNext().invoke(request, response);
        manager.afterRequest();

    }

    // private static String getOrderFromJson(String data) {
    // if (StringUtils.isNotBlank(data)) {
    // try {
    // JsonNode jsonNode = mapper.readTree(data);
    // JsonNode jsonNode2 = jsonNode.get("merchantTxnId");
    // LOGGER.info("Json Node is :{} ", jsonNode2);
    // return jsonNode2.getTextValue();
    // } catch (JsonProcessingException e) {
    // LOGGER.error("Exception occured in session : {}", e);
    // } catch (IOException e) {
    // LOGGER.error("Exception occured in session : {}", e);
    // }
    // }
    // return null;
    // }

    public static void addCustomAttributesInRequest(Map<String, Object> attributeMap) {

        Set<Entry<String, Object>> entrySet = attributeMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            currentRequest.get().setAttribute(entry.getKey(), entry.getValue());
        }

        Request request = currentRequest.get();
        MDC.put(SESSION_ID,
                request.getAttribute("MID") + "-" + request.getAttribute("ORDER_ID") + "-"
                        + request.getAttribute("jvmRoute"));
    }

}
