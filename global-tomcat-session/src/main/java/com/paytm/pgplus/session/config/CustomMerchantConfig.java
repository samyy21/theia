/**
 * 
 */
package com.paytm.pgplus.session.config;

import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.MERCHANT_CONFIG_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.paytm.pgplus.session.exception.GlobalSessionException;
import com.paytm.pgplus.session.mapper.CustomJsonMapper;
import com.paytm.pgplus.session.model.MerchantConfig;

/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
@SuppressWarnings("unused")
public final class CustomMerchantConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMerchantConfig.class);

    /**
     * Store the state of configuration files
     */
    private static Boolean initialized = Boolean.FALSE;

    /**
     * Custom merchant configuration cache
     */
    private static final ConcurrentHashMap<String, MerchantConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    /**
     * A Json tree structure to store all the merchant request data
     * configuration
     */
    private static JsonNode merchantJsonConfig = null;

    /**
     * 
     * @throws OperationNotSupportedException
     */
    private CustomMerchantConfig() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Instantiaion not supported.");
    }

    static {
        try {
            loadCustomMerchantConfig();
        } catch (GlobalSessionException ex) {
            LOGGER.error("Exception in loading custom merchant mapping from file : " + MERCHANT_CONFIG_FILE, ex);
            System.exit(0);
        }
    }

    /**
     * Load request data structure from configuration for custom merchants and
     * populate into cache
     * 
     * @throws GlobalSessionException
     * 
     */
    private static void loadCustomMerchantConfig() throws GlobalSessionException {
        initMerchantConfig();
        if (merchantJsonConfig == null) {
            throw new GlobalSessionException("Merchant config not initialized");
        }
        // put all merchant configurations in cache
        for (Iterator<Entry<String, JsonNode>> iterator = merchantJsonConfig.fields(); iterator.hasNext();) {
            Entry<String, JsonNode> node = iterator.next();
            MerchantConfig config;
            try {
                config = CustomJsonMapper.toJavaObject(node.getValue(), MerchantConfig.class);
                if (config != null) {
                    CONFIG_CACHE.put(node.getKey(), config);
                }
            } catch (Exception ex) {
                LOGGER.error("Exception in parsing merchant configurations : ", ex);
                System.exit(0);
            }
        }
        initialized = Boolean.TRUE;
    }

    /**
     * Load request data structure from configuration for custom merchants
     */
    private static void initMerchantConfig() {
        if (!initialized) {
            synchronized (initialized) {
                if (!initialized) {
                    InputStream inStream = null;
                    try {
                        inStream = Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(MERCHANT_CONFIG_FILE);
                        merchantJsonConfig = CustomJsonMapper.readTree(inStream);
                    } catch (Exception ex) {
                        LOGGER.error("Exception in reading merchant config file : " + MERCHANT_CONFIG_FILE, ex);
                        System.exit(0);
                    } finally {
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (IOException ex) {
                                LOGGER.error("Exception in reading merchant config file : " + MERCHANT_CONFIG_FILE, ex);
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetch Custom Merchant request data structure for a given mid
     * 
     * @param mid
     * @return
     */
    public static MerchantConfig getMerchantConfig(final String mid) {
        // TODO Not loading Config for now. In Wait for proper implementation
        // TODO Done as part of PGP-1077
        return CONFIG_CACHE.get(mid);
    }

}
