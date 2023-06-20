package com.paytm.pgplus.session.oldpg;

import com.paytm.pgplus.session.constant.ProjectConstant;
import com.paytm.pgplus.session.exception.SerializationException;
import com.paytm.pgplus.session.redis.operation.RedisOperation;
import com.paytm.pgplus.session.serializer.ProtoStuffSerializer;
import com.paytm.pgplus.session.serializer.SerializerWrapper;
import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RedisSessionManager extends ManagerBase implements Lifecycle {

    protected RedisSessionHandlerValve handlerValve;

    private ThreadLocal<RedisSession> currentSession = new ThreadLocal<RedisSession>();
    private ThreadLocal<String> currentSessionId = new ThreadLocal<>();
    private ThreadLocal<byte[]> currentSerializedSessionId = new ThreadLocal<byte[]>();
    private ThreadLocal<byte[]> currentSerializedMidOrderId = new ThreadLocal<byte[]>();

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private static ProtoStuffSerializer protoStuffSerializer = new ProtoStuffSerializer();
    public static final String REDIS_DELIM = ":";
    public static final String PG_LOGOUT_COOKIE = "pgloc";
    public final static boolean useLocalSessionStorage = true;
    private static Properties projectProperties = new Properties();
    private static Properties parentProperties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionManager.class);

    static {
        // configure();
    }

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
        LOGGER.info("Entering");
        super.startInternal();
        setState(LifecycleState.STARTING);

        Boolean attachedToValve = false;
        for (Valve valve : getContainer().getPipeline().getValves()) {
            if (valve instanceof RedisSessionHandlerValve) {
                this.handlerValve = (RedisSessionHandlerValve) valve;
                this.handlerValve.setRedisSessionManager(this);
                attachedToValve = true;
                break;
            }
        }

        if (!attachedToValve) {
            throw new LifecycleException("Unable to attach to session handling valve");
        }

        initializeDatabaseConnection();
        setDistributable(true);

        LOGGER.info("Exiting");
    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);
        super.stopInternal();
    }

    @Override
    public Session createSession(String requestedSessionId) {

        if (!isValidRequestForSession())
            return null;
        LOGGER.info("Entering");
        RedisSession session = null;
        try {
            String jvmSessionIdInUrl = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute(
                    "jvmSessionIdInUrl");
            if (jvmSessionIdInUrl != null) {

                session = (RedisSession) findSession(jvmSessionIdInUrl);
                return session;
            }
            String jvmSessionId = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute(
                    ProjectConstant.Attributes.JVM_SESSION_ID);
            String sessionId = generateSessionId(jvmSessionId);

            if (null != sessionId) {
                session = (RedisSession) createEmptySession();
                currentSession.set(session);
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);
        }
        if (session != null) {
            LOGGER.info("Exiting. Session Id = {}", session.getId());
        }
        return session;
    }

    private void setParametersForNewSession(RedisSession redisSession) {

        redisSession.setNew(true);
        redisSession.setValid(true);
        redisSession.setCreationTime(System.currentTimeMillis());
        redisSession.setMaxInactiveInterval(getMaxInactiveInterval());
        redisSession.setId(currentSessionId.get() + "." + getSessionIdSuffix());
        redisSession.tellNew();
        try {
            byte[] serializedSessionId = currentSerializedSessionId.get();
            if (serializedSessionId == null) {
                serializedSessionId = getSerializedRedisKey();
            }
            redisSession.setSerializedSessionId(serializedSessionId);

            byte[] serializedMidAndOrderId = currentSerializedMidOrderId.get();
            if (serializedMidAndOrderId == null) {
                serializedMidAndOrderId = getSerializedRedisMapKey();

            }
            redisSession.setSerializedMerchantIdAndOrderId(serializedMidAndOrderId);

        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

    }

    @Override
    public Session createEmptySession() {

        RedisSession redisSession = new RedisSession(this);
        setParametersForNewSession(redisSession);
        return redisSession;
    }

    @Override
    public void add(Session session) {
        super.add(session);
    }

    @Override
    public Session findSession(String id) throws IOException {

        if (!isValidRequestForSession())
            return null;
        LOGGER.info("Entering");
        Session session = null;
        try {
            String jvmSessionId = id.split("\\.")[0];
            RedisSessionHandlerValve.currentRequest.get().setAttribute(ProjectConstant.Attributes.JVM_SESSION_ID,
                    jvmSessionId);
            String sessionId = generateSessionId(jvmSessionId);
            Session existingLocalsession = null;
            if (useLocalSessionStorage) {

                String previousRequestRoute = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute(
                        "route");
                LOGGER.info("previous request sent on server {}", previousRequestRoute);

                existingLocalsession = super.findSession(sessionId);
                if (getJvmRoute() != null && getJvmRoute().equals(previousRequestRoute)) {
                    boolean userLoggedOut = hasUserLoggedOut();

                    if (!userLoggedOut) {
                        session = existingLocalsession;
                        LOGGER.info("Local session found = {}", session);
                        RedisSessionHandlerValve.currentRequest.get().setAttribute("localSessionFound", true);
                    }
                }
            }
            if (session == null) {
                LOGGER.info("no local session available");
                session = loadSessionFromRedis();
            }
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        currentSession.set((RedisSession) session);
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

            SerializerWrapper serializerWrapper = new SerializerWrapper();
            serializerWrapper.setData(sessionAttributes);
            byte[] serializedSessionData = protoStuffSerializer.serialize(SerializerWrapper.class, serializerWrapper);

            Map<byte[], byte[]> sessionDataMap = new HashMap<byte[], byte[]>();

            sessionDataMap.put(currentSession.get().getSerializedMerchantIdAndOrderId(), serializedSessionData);
            // redisCacheService.setBinaryValuesInHashWithExpiryInSeconds(currentSession.get().getSerializedSessionId(),
            // currentSession.get().getMaxInactiveInterval(), sessionDataMap);
            long start = System.currentTimeMillis();
            List<Object> resultList = RedisOperation.setBinaryValuesWithExpiryInPipeline(currentSession.get()
                    .getSerializedSessionId(), currentSession.get().getMaxInactiveInterval(), sessionDataMap);
            // redisCacheService.setBinaryValuesInHash(currentSession.get().getSerializedSessionId(),
            // sessionDataMap);
            // redisCacheService.setExpiryOfBinaryKeyInSeconds(currentSession.get().getSerializedSessionId(),
            // currentSession.get().getMaxInactiveInterval());
            long end = System.currentTimeMillis();
            LOGGER.info("Redis write time = {} ms result of set map with expiry = {}", (end - start), resultList);

        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        LOGGER.info("Exiting");
    }

    protected Session loadSessionFromRedis() {

        LOGGER.info("Entering");
        RedisSession session = null;
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

                session = (RedisSession) createEmptySession();

                session.setLoadSessionFromRedis(true);
                for (String key : sessionDataMap.keySet()) {
                    session.setAttribute(key, sessionDataMap.get(key));
                }

                session.setLoadSessionFromRedis(false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        LOGGER.info("Exiting");
        return session;
    }

    protected void deleteSessionFromRedis() {

        LOGGER.info("Entering");
        try {
            RedisOperation.deleteFieldsOfBinaryKeyFromMap(currentSession.get().getSerializedSessionId(),
                    new byte[][] { currentSession.get().getSerializedMerchantIdAndOrderId() });
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        LOGGER.info("Exiting");
    }

    protected void removeAttributesFromAllSessions() {

        LOGGER.info("Entering");
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
                List<Object> resultList = RedisOperation.setBinaryValuesWithExpiryInPipeline(currentSession.get()
                        .getSerializedSessionId(), currentSession.get().getMaxInactiveInterval(), newSerializedMap);
                LOGGER.info("result of set map with expiry = {}", resultList);
                // redisCacheService.setBinaryValuesInHash(currentSession.get().getSerializedSessionId(),
                // newSerializedMap);
                // redisCacheService.setExpiryOfBinaryKeyInSeconds(currentSession.get().getSerializedSessionId(),
                // currentSession.get().getMaxInactiveInterval());
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);
        }

        LOGGER.info("Exiting");
    }

    protected String generateSessionId(String jvmSessionId) {

        if (jvmSessionId == null) {
            String[] sessionId = super.generateSessionId().split("\\.");
            jvmSessionId = sessionId[0];

        }
        currentSessionId.set(jvmSessionId);
        String sessionId = jvmSessionId + "." + getSessionIdSuffix();

        return sessionId;
    }

    @Override
    public void remove(Session session) {
        remove(session, false);
    }

    protected void afterRequest() {

        if (!isValidRequestForSession())
            return;

        if (!currentSession.get().getRemovedAttributes().isEmpty()) {
            removeAttributesFromAllSessions();
            currentSession.get().getRemovedAttributes().clear();

            Cookie cookie = new Cookie(PG_LOGOUT_COOKIE, "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            RedisSessionHandlerValve.currentResponse.get().addCookie(cookie);

        } else if ((Boolean) RedisSessionHandlerValve.currentRequest.get().getAttribute("saveToRedis")) {
            saveSessionToRedis();

        } else if ((Boolean) RedisSessionHandlerValve.currentRequest.get().getAttribute("localSessionFound")) {

            try {
                byte[] serializedSessionId = getSerializedRedisKey();
                RedisOperation.setExpiryOfBinaryKeyInSeconds(serializedSessionId, currentSession.get()
                        .getMaxInactiveInterval());

            } catch (Exception e) {
                LOGGER.error("Exception", e);
            }
        }

        currentSerializedMidOrderId.remove();
        currentSerializedSessionId.remove();
        currentSession.remove();
        currentSessionId.remove();
        RedisSessionHandlerValve.currentRequest.remove();
        RedisSessionHandlerValve.currentResponse.remove();

    }

    private Boolean hasUserLoggedOut() {

        Cookie[] cookies = RedisSessionHandlerValve.currentRequest.get().getCookies();
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

        Request request = RedisSessionHandlerValve.currentRequest.get();
        if (request != null) {
            String mid = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("MID");
            String orderId = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("ORDER_ID");
            if (mid != null && !mid.trim().isEmpty() && orderId != null && !orderId.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void initializeDatabaseConnection() throws LifecycleException {

        LOGGER.info("Entering");
        try {
            String host = projectProperties.getProperty("redis.host");
            int port = Integer.valueOf(projectProperties.getProperty("redis.port"));
            String sentinelEnable = projectProperties.getProperty("is.sentinel.enable");
            boolean isSentinelEnable = false;
            if (sentinelEnable != null
                    && ("y".equalsIgnoreCase(sentinelEnable) || "yes".equalsIgnoreCase(sentinelEnable))) {
                isSentinelEnable = true;
            }
            String sentinelServers = null;
            if (isSentinelEnable) {
                sentinelServers = projectProperties.getProperty("redis.sentinel.servers");
            }
        } catch (Exception e) {
            LOGGER.info("Exception in connecting to redis", e);
            throw new LifecycleException("Error connecting to Redis", e);
        }
        LOGGER.info("Exiting");
    }

    private String getSessionIdSuffix() {

        String sessionIdSuffix = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("MID")
                + (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("ORDER_ID");
        String jvmRoute = getJvmRoute();
        if (jvmRoute != null && !jvmRoute.trim().isEmpty()) {
            sessionIdSuffix += "." + jvmRoute;
        }
        return sessionIdSuffix;
    }

    private static void loadParentProperties() {

        try {
            String parentPropertiesPath = projectProperties.getProperty("parent.properties.path");
            String parentPropertiesName = projectProperties.getProperty("parent.properties.file.name");
            String platformName = System.getProperty("platform_name");
            if (platformName != null) {
                parentPropertiesPath = parentPropertiesPath + "/" + platformName + "/" + parentPropertiesName;
            } else {
                parentPropertiesPath = parentPropertiesPath + "/" + parentPropertiesName;
            }

            InputStream fis = new FileInputStream(new File(parentPropertiesPath));
            parentProperties.load(fis);
            fis.close();
        } catch (Exception ex) {
            LOGGER.error("Exception occurred", ex);
        }
    }

    private byte[] getSerializedRedisKey() throws SerializationException {

        SerializerWrapper serializerWrapper = new SerializerWrapper();
        serializerWrapper.setData(currentSessionId.get());
        byte[] serializedSessionId = protoStuffSerializer.serialize(SerializerWrapper.class, serializerWrapper);
        return serializedSessionId;
    }

    private byte[] getSerializedRedisMapKey() throws SerializationException {

        SerializerWrapper serializerWrapper = new SerializerWrapper();
        String mid = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("MID");
        String orderId = (String) RedisSessionHandlerValve.currentRequest.get().getAttribute("ORDER_ID");
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
}
