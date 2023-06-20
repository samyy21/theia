package com.paytm.pgplus.utils;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Naman on 05/06/17.
 */
public class PropertiesUtil {

    private final Properties PROPERTIES;

    private PropertiesUtil() throws Exception {
        PROPERTIES = loadProperties("project_biz_test.properties");
    }

    private static Properties loadProperties(final String fileName) throws FacadeCheckedException {
        try {
            final Properties properties = new Properties();
            final InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
            properties.load(in);
            return properties;
        } catch (final Throwable t) {
            throw new FacadeCheckedException("Error in loading properties file.", t);
        }
    }

    private static class SingletonHolder {
        private static PropertiesUtil instance;
        static {
            PropertiesUtil tmp = null;
            try {
                tmp = new PropertiesUtil();
            } catch (final Throwable t) {
                throw new ExceptionInInitializerError(t);
            }
            instance = tmp;
        }
    }

    public static final PropertiesUtil getInstance() {
        return SingletonHolder.instance;
    }

    public static final Properties getProperties() {
        return (Properties) getInstance().PROPERTIES.clone();
    }

}
