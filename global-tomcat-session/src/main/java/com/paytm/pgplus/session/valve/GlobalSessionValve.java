/**
 * 
 */
package com.paytm.pgplus.session.valve;

import static com.paytm.pgplus.session.config.CustomMerchantConfig.getMerchantConfig;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.JVM_ROUTE;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.JVM_SESSION_ID;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.JVM_SESSION_ID_IN_URL;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.LOCAL_SESSION_FOUND;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.MID;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.ORDER_ID;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.ROUTE;
import static com.paytm.pgplus.session.constant.ProjectConstant.Attributes.SAVE_TO_REDIS;
import static com.paytm.pgplus.session.constant.ProjectConstant.RequestParams.REQUEST_DATA;
import static com.paytm.pgplus.session.constant.ProjectConstant.RequestParams.REQUEST_MID;
import static com.paytm.pgplus.session.constant.ProjectConstant.RequestParams.REQUEST_ORDER_ID;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.JsonNode;
import com.paytm.pgplus.session.manager.GlobalSessionManager;
import com.paytm.pgplus.session.mapper.CustomJsonMapper;
import com.paytm.pgplus.session.model.MerchantConfig;

/**
 * @createdOn 18-Feb-2016
 * @author kesari
 */
public class GlobalSessionValve extends ValveBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSessionValve.class);
    /**
     * Store current request per thread
     */
    public static ThreadLocal<Request> currentRequest = new ThreadLocal<Request>();
    /**
     * Store current response per thread
     */
    public static ThreadLocal<Response> currentResponse = new ThreadLocal<Response>();

    private GlobalSessionManager sessionManager;
    private static final String SESSION_ID = "requestId";

    /**
	 * 
	 */
    public GlobalSessionValve() {
        super();
    }

    /**
     * 
     * @param sessionManager
     */
    public void setSessionManager(GlobalSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
	 * 
	 */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            long startTime = System.nanoTime();
            String orderId = request.getParameter(REQUEST_ORDER_ID);
            String mid = request.getParameter(REQUEST_MID);
            String route = request.getParameter(ROUTE);
            // To handle Oauth callback response
            if (request.getParameter("state") != null) {
                String[] stateValues = request.getParameter("state").split(":");
                orderId = stateValues[0];
                mid = stateValues[1];
                if (stateValues.length >= 8) {
                    route = stateValues[7];
                }
                if (stateValues.length > 3) {
                    request.setAttribute(JVM_SESSION_ID_IN_URL, stateValues[3]);
                }
            } else {
                request.setAttribute(JVM_SESSION_ID, request.getRequestedSessionId());
            }
            if (StringUtils.isNotBlank(mid)) {
                // If orderId not found then fetch orderId from custom response
                if (StringUtils.isBlank(orderId)) {
                    MerchantConfig merchantConfig = getMerchantConfig(mid);
                    if (merchantConfig != null) {
                        orderId = getCustomFieldFromRequest(request, merchantConfig.getOrderIdField(),
                                merchantConfig.getDataType());
                    }
                }
                request.setAttribute(MID, mid);
                request.setAttribute(ORDER_ID, orderId);
                request.setAttribute(ROUTE, route);
                request.setAttribute(JVM_ROUTE, sessionManager.getJvmRoute());
                request.setAttribute(LOCAL_SESSION_FOUND, Boolean.FALSE);
                request.setAttribute(SAVE_TO_REDIS, Boolean.FALSE);
                MDC.put("MID", mid);
                MDC.put("ORDER_ID", orderId);
                MDC.put("JVM_ROUTE", sessionManager.getJvmRoute());
            }
            request.setAttribute("MID", mid);
            request.setAttribute("ORDER_ID", orderId);
            request.setAttribute("route", route);
            request.setAttribute("jvmRoute", sessionManager.getJvmRoute());
            request.setAttribute("localSessionFound", false);
            request.setAttribute("saveToRedis", false);

            currentRequest.set(request);
            currentResponse.set(response);
            long reqStartTime = System.nanoTime();
            getNext().invoke(request, response);
            long reqEndTimeTaken = System.nanoTime() - reqStartTime;
            sessionManager.afterRequest();
            long totalTimeTaken = System.nanoTime() - startTime;
            LOGGER.warn(
                    "Post - Session : {}, Sessionid : {}, Url : {}, orderId :{}, mid :{}, route : {}, QueryString :{}, ReqTime :{}ms, TotTime : {}ms, ",
                    sessionManager.getCurrentSession(), sessionManager.getCurrentSessionId(), request.getRequestURI(),
                    orderId, mid, route, request.getQueryString(), reqEndTimeTaken / 1000000, totalTimeTaken / 1000000);
        } catch (Throwable th) {
            LOGGER.error("Exception :", th);
            throw th;
        }
        LOGGER.debug("exiting");
    }

    /**
     * 
     * @param request
     * @param fieldName
     * @param dataType
     * @return
     */
    private String getCustomFieldFromRequest(Request request, String fieldName, String dataType) {
        String fieldValue = null;
        if (StringUtils.isBlank(fieldName) || StringUtils.isBlank(dataType)) {
            return fieldValue;
        }
        String[] fieldNameArray = fieldName.split("[.]");
        switch (dataType) {
        case "JSON":
            fieldValue = getJsonFieldData(request.getParameter(fieldNameArray[0]), fieldNameArray);
            break;
        default:
            break;
        }
        return fieldValue;
    }

    /**
     * 
     * @param parameter
     * @param fieldNameArray
     * @return
     */
    private String getJsonFieldData(String jsonData, String[] fieldNameArray) {
        String fieldValue = null;
        if (fieldNameArray.length == 1) {
            return jsonData;
        }
        try {
            JsonNode jsonNode = CustomJsonMapper.stringToJson(jsonData);
            for (int index = 1; index < fieldNameArray.length; index++) {
                String fieldName = fieldNameArray[index];
                jsonNode = jsonNode.get(fieldName);
                if (jsonNode == null) {
                    break;
                }
            }
            if (jsonNode != null) {
                fieldValue = jsonNode.textValue();
            }
        } catch (Exception ex) {
            LOGGER.error("Exception in reading json", ex);
        }
        return fieldValue;
    }

    public static void addCustomAttributesInRequest(Map<String, Object> attributeMap) {

        Set<Entry<String, Object>> entrySet = attributeMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            currentRequest.get().setAttribute(entry.getKey(), entry.getValue());
        }

        Request request = currentRequest.get();
        MDC.clear();
        MDC.put(SESSION_ID,
                request.getAttribute("MID") + "-" + request.getAttribute("ORDER_ID") + "-"
                        + request.getAttribute("jvmRoute"));
    }

}
