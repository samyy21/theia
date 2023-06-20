package com.paytm.pgplus.session.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.session.manager.GlobalSessionManager;
import com.paytm.pgplus.session.valve.GlobalSessionValve;

public class GlobalSession extends StandardSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSession.class);
    /**
	 * 
	 */
    private static final long serialVersionUID = -6669288275457388051L;
    private byte[] serializedSessionId;
    private String sessionId;
    private byte[] serializedMerchantIdAndOrderId;
    private String MerchantIdAndOrderId;
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

    public GlobalSession(Manager manager) {
        super(manager);
    }

    @Override
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        if (!loadSessionFromRedis) {
            GlobalSessionValve.currentRequest.get().setAttribute("saveToRedis", true);
        }
    }

    @Override
    public Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        GlobalSessionValve.currentRequest.get().setAttribute("saveToRedis", true);
        return value;
    }

    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        GlobalSessionValve.currentRequest.get().setAttribute("saveToRedis", true);
    }

    public void removeAttributeFromAllSessions(String name) {
        super.removeAttribute(name);
        removedAttributes.add(name);
    }

    @Override
    public void setId(String id) {
        LOGGER.debug("setting id : {}", id);
        super.setId(id);
        this.id = id;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        LOGGER.debug("Invalidating session : {}", getId());
        GlobalSessionManager sessionManager = (GlobalSessionManager) manager;
        sessionManager.deleteSessionFromRedis();
        GlobalSessionValve.currentRequest.get().setAttribute("saveToRedis", false);
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId
     *            the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the merchantIdAndOrderId
     */
    public String getMerchantIdAndOrderId() {
        return MerchantIdAndOrderId;
    }

    /**
     * @param merchantIdAndOrderId
     *            the merchantIdAndOrderId to set
     */
    public void setMerchantIdAndOrderId(String merchantIdAndOrderId) {
        MerchantIdAndOrderId = merchantIdAndOrderId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GlobalSession [sessionId=" + sessionId + ", MerchantIdAndOrderId=" + MerchantIdAndOrderId
                + ", loadSessionFromRedis=" + loadSessionFromRedis + ", removedAttributes=" + removedAttributes + "]";
    }

}
