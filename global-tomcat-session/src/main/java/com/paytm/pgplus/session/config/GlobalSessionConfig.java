/**
 * 
 */
package com.paytm.pgplus.session.config;

import static com.paytm.pgplus.session.constant.ProjectConstant.Configurations.SESSION_CONFIG_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
public final class GlobalSessionConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSessionConfig.class);

    /**
     * MUTEX object for locking during synchronization
     */
    private static final Object MUTEX = new Object();

    /**
     * Properties to load and store configurations
     */
    private static Properties configProps = null;

    static {
        // Initialize Project configurations
        initProjectConfig();
    }

    /**
     * 
     * @throws OperationNotSupportedException
     */
    private GlobalSessionConfig() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Instantiaion not supported.");
    }

    /**
     * Initialize Project configurations
     */
    private static void initProjectConfig() {
        if (configProps == null) {
            synchronized (MUTEX) {
                if (configProps == null) {
                    InputStream inStream = null;
                    try {
                        configProps = new Properties();
                        inStream = Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(SESSION_CONFIG_FILE);
                        configProps.load(inStream);
                        LOGGER.info("Initialized Project configuration from file : {}", SESSION_CONFIG_FILE);
                        for (String propName : configProps.stringPropertyNames()) {
                            LOGGER.debug("Loaded Property :: {}  with value :: {}", propName,
                                    configProps.getProperty(propName));
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Exception in reading proprty file : " + SESSION_CONFIG_FILE, ex);
                        System.exit(0);
                    } finally {
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (IOException ex) {
                                LOGGER.error("Exception in reading proprty file : " + SESSION_CONFIG_FILE, ex);
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * {@code null} if the property is not found.
     * 
     * @param key
     * @return
     */
    public static String getProperty(final String key) {
        return getProperty(key, null);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(final String key, final String defaultValue) {
        if (configProps == null) {
            initProjectConfig();
        }
        synchronized (MUTEX) {
            return configProps.getProperty(key, defaultValue);
        }
    }
}
