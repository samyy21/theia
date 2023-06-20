package com.paytm.pgplus.session.oldpg;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

public class RedisSession extends StandardSession {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6669288275457388051L;
    private byte[] serializedSessionId;
    private byte[] serializedMerchantIdAndOrderId;
    private boolean loadSessionFromRedis;
    private List<String> removedAttributes = new ArrayList<String>();

    public List<String> getRemovedAttributes() {
        return removedAttributes;
    }

    public boolean isLoadSessionFromRedis() {
        return loadSessionFromRedis;
    }

    public void setLoadSessionFromRedis(boolean loadSessionFromRedis) {
        this.loadSessionFromRedis = loadSessionFromRedis;
    }

    public byte[] getSerializedMerchantIdAndOrderId() {
        return serializedMerchantIdAndOrderId;
    }

    public void setSerializedMerchantIdAndOrderId(byte[] serializedMerchantIdAndOrderId) {
        this.serializedMerchantIdAndOrderId = serializedMerchantIdAndOrderId;
    }

    public byte[] getSerializedSessionId() {
        return serializedSessionId;
    }

    public void setSerializedSessionId(byte[] serializedSessionId) {
        this.serializedSessionId = serializedSessionId;
    }

    public RedisSession(Manager manager) {
        super(manager);
    }

    @Override
    public void setAttribute(String name, Object value) {

        super.setAttribute(name, value);
        if (!loadSessionFromRedis) {
            RedisSessionHandlerValve.currentRequest.get().setAttribute("saveToRedis", true);
        }

    }

    @Override
    public Object getAttribute(String name) {

        Object value = super.getAttribute(name);
        return value;
    }

    @Override
    public void removeAttribute(String name) {

        super.removeAttribute(name);
        RedisSessionHandlerValve.currentRequest.get().setAttribute("saveToRedis", true);

    }

    public void removeAttributeFromAllSessions(String name) {

        super.removeAttribute(name);
        removedAttributes.add(name);

    }

    @Override
    public void setId(String id) {
        super.setId(id);
        this.id = id;
    }

    @Override
    public void invalidate() {

        RedisSessionManager sessionManager = (RedisSessionManager) manager;
        sessionManager.deleteSessionFromRedis();
        RedisSessionHandlerValve.currentRequest.get().setAttribute("saveToRedis", false);
        super.invalidate();

    }

}
