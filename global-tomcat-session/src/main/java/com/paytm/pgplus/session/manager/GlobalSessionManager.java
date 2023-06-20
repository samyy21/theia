package com.paytm.pgplus.session.manager;

import com.paytm.pgplus.session.constant.ProjectConstant;
import com.paytm.pgplus.session.exception.SerializationException;
import com.paytm.pgplus.session.model.GlobalSession;
import com.paytm.pgplus.session.redis.connection.RedisClusterClientLettuceService;
import com.paytm.pgplus.session.redis.connection.RedisConnection;
import com.paytm.pgplus.session.redis.operation.RedisOperation;
import com.paytm.pgplus.session.serializer.ProtoStuffSerializer;
import com.paytm.pgplus.session.serializer.Serializer;
import com.paytm.pgplus.session.serializer.SerializerWrapper;
import com.paytm.pgplus.session.valve.GlobalSessionValve;
import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.*;

import static com.paytm.pgplus.session.config.GlobalSessionConfig.getProperty;
import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.*;

public class GlobalSessionManager extends ManagerBase implements Lifecycle {

    protected GlobalSessionValve handlerValve;

    private static ThreadLocal<GlobalSession> currentSession = new ThreadLocal<GlobalSession>();
    private static ThreadLocal<String> currentSessionId = new ThreadLocal<>();
    private static ThreadLocal<byte[]> currentSerializedSessionId = new ThreadLocal<byte[]>();
    private static ThreadLocal<byte[]> currentSerializedMidOrderId = new ThreadLocal<byte[]>();

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private static Serializer protoStuffSerializer = new ProtoStuffSerializer();
    public static final String REDIS_DELIM = ":";
    public static final String PG_LOGOUT_COOKIE = "pgloc";
    public final static boolean useLocalSessionStorage = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSessionManager.class);

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    protected synchronized void startInternal() throws LifecycleException {
        try {
            LOGGER.info("Entering");
            super.startInternal();
            setState(LifecycleState.STARTING);
            Boolean attachedToValve = false;
            for (Valve valve : getContainer().getPipeline().getValves()) {
                if (valve instanceof GlobalSessionValve) {
                    this.handlerValve = (GlobalSessionValve) valve;
                    this.handlerValve.setSessionManager(this);
                    attachedToValve = true;
                    break;
                }
            }
            if (!attachedToValve) {
                throw new LifecycleException("Unable to attach to session handling valve");
            }
            setDistributable(true);
            if ("true".equals(getProperty(GLOBAL_TOMCAT_CONNECT_CLUSTER, "false"))) {
                RedisClusterClientLettuceService.connectToCluster();
            }
            if ("true".equals(getProperty(GLOBAL_TOMCAT_CONNECT_SENTINEL, "true"))) {
                RedisConnection.getJedis();
            }
            LOGGER.info("Exiting");
        } catch (Throwable th) {
            LOGGER.error("Exception : ", th);
        }
    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        LOGGER.info("Entering");
        setState(LifecycleState.STOPPING);
        super.stopInternal();
        LOGGER.info("Exiting");
    }

    @Override
    public Session createSession(String requestedSessionId) {
        LOGGER.info("Entering");
        boolean isValidRequestForSession = isValidRequestForSession();
        LOGGER.debug("requestedSessionId : {}, isValidRequestForSession :{}", requestedSessionId,
                isValidRequestForSession);
        if (!isValidRequestForSession)
            return null;
        GlobalSession session = null;
        try {
            String jvmSessionIdInUrl = (String) GlobalSessionValve.currentRequest.get().getAttribute(
                    ProjectConstant.Attributes.JVM_SESSION_ID_IN_URL);
            LOGGER.debug("jvmSessionIdInUrl :{}", jvmSessionIdInUrl);
            if (jvmSessionIdInUrl != null) {
                session = (GlobalSession) findSession(jvmSessionIdInUrl);
                return session;
            }
            String jvmSessionId = (String) GlobalSessionValve.currentRequest.get().getAttribute(
                    ProjectConstant.Attributes.JVM_SESSION_ID);
            LOGGER.debug("jvmSessionId :{}", jvmSessionId);
            String sessionId = generateSessionId(jvmSessionId);
            if (null != sessionId) {
                session = (GlobalSession) createEmptySession();
            }
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
        if (session != null) {
            LOGGER.info("Exiting. Session Id = {}", session.getId());
        }
        return session;
    }

    private void setParametersForNewSession(GlobalSession globalSession) {
        LOGGER.info("Entering");
        globalSession.setNew(true);
        globalSession.setValid(true);
        globalSession.setCreationTime(System.currentTimeMillis());
        globalSession.setMaxInactiveInterval(getMaxInactiveInterval());
        globalSession.setId(currentSessionId.get() + "." + getSessionIdSuffix());
        globalSession.tellNew();
        try {
            globalSession.setSerializedSessionId(getSerializedRedisKey());
            globalSession.setSerializedMerchantIdAndOrderId(getSerializedRedisMapKey());
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
    }

    @Override
    public Session createEmptySession() {
        LOGGER.info("Entering");
        GlobalSession globalSession = new GlobalSession(this);
        setParametersForNewSession(globalSession);
        // GlobalSessionValve.currentRequest.get().setAttribute("saveToRedis",
        // true);
        currentSession.set(globalSession);
        return globalSession;
    }

    @Override
    public void add(Session session) {
        super.add(session);
    }

    @Override
    public Session findSession(String id) throws IOException {
        Session session = null;
        try {
            if (currentSession.get() != null) {
                return currentSession.get();
            }
            LOGGER.info("Entering findSession for id : {}", id);
            if (!isValidRequestForSession())
                return null;
            String jvmSessionId = id.split("\\.")[0];
            GlobalSessionValve.currentRequest.get().setAttribute(ProjectConstant.Attributes.JVM_SESSION_ID,
                    jvmSessionId);
            String sessionId = generateSessionId(jvmSessionId);
            Session existingLocalsession = null;
            if (useLocalSessionStorage) {
                String previousRequestRoute = (String) GlobalSessionValve.currentRequest.get().getAttribute("route");
                LOGGER.info("previous request sent on server {}", previousRequestRoute);
                existingLocalsession = super.findSession(sessionId);
                if (getJvmRoute() != null && getJvmRoute().equals(previousRequestRoute)) {
                    boolean userLoggedOut = hasUserLoggedOut();
                    if (!userLoggedOut) {
                        session = existingLocalsession;
                        LOGGER.info("Local session found = {}", session);
                        GlobalSessionValve.currentRequest.get().setAttribute("localSessionFound", true);
                    }
                }
            }
            if (session == null) {
                session = loadSessionFromRedis();
            }
            LOGGER.info("Redis Session :{}", session);
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
        currentSession.set((GlobalSession) session);
        LOGGER.info("Exiting");
        return session;
    }

    protected void saveSessionToRedis() {
        LOGGER.info("Entering");
        try {
            Map<String, Object> sessionAttributes = new HashMap<String, Object>();
            for (Enumeration<String> enumerator = currentSession.get().getAttributeNames(); enumerator
                    .hasMoreElements();) {
                String key = enumerator.nextElement();
                sessionAttributes.put(key, currentSession.get().getAttribute(key));
            }
            LOGGER.debug("sessionAttributes :{}", sessionAttributes);
            SerializerWrapper serializerWrapper = new SerializerWrapper();
            serializerWrapper.setData(sessionAttributes);
            byte[] serializedSessionData = protoStuffSerializer.serialize(SerializerWrapper.class, serializerWrapper);
            Map<byte[], byte[]> sessionDataMap = new HashMap<byte[], byte[]>();
            sessionDataMap.put(currentSession.get().getSerializedMerchantIdAndOrderId(), serializedSessionData);
            long start = System.currentTimeMillis();
            LOGGER.info("Redis key : {}", currentSessionId.get());
            boolean result = RedisOperation.setBinaryValuesWithExpiry(currentSession.get().getSerializedSessionId(),
                    currentSession.get().getMaxInactiveInterval(), sessionDataMap);
            long end = System.currentTimeMillis();
            LOGGER.info("Redis write time = {} ms , result : {}", (end - start), result);
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
        LOGGER.info("Exiting");
    }

    protected Session loadSessionFromRedis() {
        GlobalSession session = null;
        try {
            byte[] serializedSessionId = getSerializedRedisKey();
            byte[] serializedMapKey = getSerializedRedisMapKey();
            long start = System.currentTimeMillis();
            List<byte[]> serializedSessionDataList = RedisOperation.getBinaryValueByKeyFromMap(serializedSessionId,
                    serializedMapKey);
            long end = System.currentTimeMillis();
            LOGGER.info("Redis read time = {} ms", (end - start));
            byte[] serializedSessionData = null;
            if (serializedSessionDataList != null && !serializedSessionDataList.isEmpty()) {
                serializedSessionData = serializedSessionDataList.get(0);
            }
            if (serializedSessionData != null) {
                LOGGER.info("found session data from redis");
                currentSerializedSessionId.set(serializedSessionId);
                currentSerializedMidOrderId.set(serializedMapKey);
                SerializerWrapper serializerWrapper = (SerializerWrapper) protoStuffSerializer.deserialize(
                        SerializerWrapper.class, serializedSessionData);
                Map<String, Object> sessionDataMap = (Map<String, Object>) serializerWrapper.getData();
                session = (GlobalSession) createEmptySession();
                session.setLoadSessionFromRedis(true);
                for (String key : sessionDataMap.keySet()) {
                    session.setAttribute(key, sessionDataMap.get(key));
                }
                session.setLoadSessionFromRedis(false);
            }
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
        return session;
    }

    public void deleteSessionFromRedis() {
        try {
            LOGGER.info("Deleting Session : {}", currentSessionId.get());
            RedisOperation.deleteFieldsOfBinaryKeyFromMap(currentSession.get().getSerializedSessionId(),
                    new byte[][] { currentSession.get().getSerializedMerchantIdAndOrderId() });
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
    }

    protected void removeAttributesFromAllSessions() {
        try {
            Map<byte[], byte[]> serializedMap = RedisOperation.getMapByBinaryKey(currentSerializedSessionId.get());
            if (serializedMap != null) {
                Map<byte[], byte[]> newSerializedMap = new HashMap<byte[], byte[]>();
                for (Map.Entry<byte[], byte[]> entry : serializedMap.entrySet()) {

                    SerializerWrapper serializerWrapper = (SerializerWrapper) protoStuffSerializer.deserialize(
                            SerializerWrapper.class, entry.getValue());
                    Map<String, String> sessionDataMap = (Map<String, String>) serializerWrapper.getData();
                    Iterator<Map.Entry<String, String>> iterator = sessionDataMap.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<String, String> attribute = iterator.next();
                        if (currentSession.get().getRemovedAttributes().contains(attribute.getKey())) {
                            iterator.remove();
                        }
                    }
                    byte[] newSerializedSessionData = protoStuffSerializer.serialize(SerializerWrapper.class,
                            serializerWrapper);
                    newSerializedMap.put(entry.getKey(), newSerializedSessionData);
                }
                RedisOperation.setBinaryValuesWithExpiry(currentSession.get().getSerializedSessionId(), currentSession
                        .get().getMaxInactiveInterval(), newSerializedMap);
                // redisCacheService.setBinaryValuesInHash(currentSession.get().getSerializedSessionId(),
                // newSerializedMap);
                // redisCacheService.setExpiryOfBinaryKeyInSeconds(currentSession.get().getSerializedSessionId(),
                // currentSession.get().getMaxInactiveInterval());
            }
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        }
    }

    protected String generateSessionId(String jvmSessionId) {
        if (jvmSessionId == null) {
            String[] sessionId = super.generateSessionId().split("\\.");
            jvmSessionId = sessionId[0];
        }
        currentSessionId.set(jvmSessionId);
        String sessionId = jvmSessionId + "." + getSessionIdSuffix();
        LOGGER.info("currentSessionId : {}, sessionId :{}", currentSessionId, sessionId);
        return sessionId;
    }

    @Override
    public void remove(Session session) {
        remove(session, false);
    }

    public void afterRequest() {
        try {
            if (!isValidRequestForSession() || currentSession.get() == null)
                return;
            if (!currentSession.get().getRemovedAttributes().isEmpty()) {
                removeAttributesFromAllSessions();
                currentSession.get().getRemovedAttributes().clear();
                Cookie cookie = new Cookie(PG_LOGOUT_COOKIE, "");
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                GlobalSessionValve.currentResponse.get().addCookie(cookie);
            } else if ((Boolean) GlobalSessionValve.currentRequest.get().getAttribute("saveToRedis")) {
                saveSessionToRedis();
            } else if ((Boolean) GlobalSessionValve.currentRequest.get().getAttribute("localSessionFound")) {
                byte[] serializedSessionId = getSerializedRedisKey();
                RedisOperation.setExpiryOfBinaryKeyInSeconds(serializedSessionId, currentSession.get()
                        .getMaxInactiveInterval());
            }
            LOGGER.info("AfterRequest :{}, currentSessionId :{}", currentSession.get(), currentSessionId.get());
        } catch (Throwable e) {
            LOGGER.error("Exception", e);
        } finally {
            currentSerializedMidOrderId.remove();
            currentSerializedSessionId.remove();
            currentSession.remove();
            currentSessionId.remove();
            GlobalSessionValve.currentRequest.remove();
            GlobalSessionValve.currentResponse.remove();
        }
    }

    private Boolean hasUserLoggedOut() {
        Cookie[] cookies = GlobalSessionValve.currentRequest.get().getCookies();
        if ((cookies == null) || (cookies.length == 0))
            return true;
        boolean userLoggedOut = false;
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(PG_LOGOUT_COOKIE)) {
                userLoggedOut = true;
            }
        }
        LOGGER.info("user logout flag = {}", userLoggedOut);
        return userLoggedOut;
    }

    private boolean isValidRequestForSession() {
        Request request = GlobalSessionValve.currentRequest.get();
        if (request != null) {
            String mid = (String) GlobalSessionValve.currentRequest.get().getAttribute("MID");
            String orderId = (String) GlobalSessionValve.currentRequest.get().getAttribute("ORDER_ID");
            LOGGER.debug("mid : {}, orderId :{}", mid, orderId);
            if (mid != null && !mid.trim().isEmpty() && orderId != null && !orderId.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String getSessionIdSuffix() {
        String sessionIdSuffix = (String) GlobalSessionValve.currentRequest.get().getAttribute("MID")
                + (String) GlobalSessionValve.currentRequest.get().getAttribute("ORDER_ID");
        String jvmRoute = getJvmRoute();
        if (jvmRoute != null && !jvmRoute.trim().isEmpty()) {
            sessionIdSuffix += "." + jvmRoute;
        }
        return sessionIdSuffix;
    }

    private byte[] getSerializedRedisKey() throws SerializationException {
        SerializerWrapper serializerWrapper = new SerializerWrapper();
        serializerWrapper.setData(currentSessionId.get());
        byte[] serializedSessionId = protoStuffSerializer.serialize(SerializerWrapper.class, serializerWrapper);
        return serializedSessionId;
    }

    private byte[] getSerializedRedisMapKey() throws SerializationException {

        SerializerWrapper serializerWrapper = new SerializerWrapper();
        String mid = (String) GlobalSessionValve.currentRequest.get().getAttribute("MID");
        String orderId = (String) GlobalSessionValve.currentRequest.get().getAttribute("ORDER_ID");
        serializerWrapper.setData(mid + REDIS_DELIM + orderId);
        byte[] serializedMapKey = protoStuffSerializer.serialize(SerializerWrapper.class, serializerWrapper);
        return serializedMapKey;

    }

    @Override
    public void load() throws ClassNotFoundException, IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unload() throws IOException {
        // TODO Auto-generated method stub

    }

    public String getCurrentSessionId() {
        return currentSessionId.get();
    }

    public GlobalSession getCurrentSession() {
        return currentSession.get();
    }

}
