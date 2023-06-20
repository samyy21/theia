/**
 * 
 */
package com.paytm.pgplus.session.constant;

/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
public final class ProjectConstant {

    /**
     * @createdOn 12-Mar-2016
     * @author kesari
     */
    public static final class Attributes {
        /**
         * mid key name in request/session attributes
         */
        public static final String MID = "mid";
        /**
         * mid key name in request/session attributes
         */
        public static final String ORDER_ID = "orderid";
        /**
         * mid key name in request/session attributes
         */
        public static final String ROUTE = "route";
        /**
         * mid key name in request/session attributes
         */
        public static final String JVM_ROUTE = "jvmroute";
        /**
         * mid key name in request/session attributes
         */
        public static final String LOCAL_SESSION_FOUND = "localsessionfound";
        /**
		 * 
		 */
        public static final String JVM_SESSION_ID_IN_URL = "jvmsessionidinurl";
        /**
         * mid key name in request/session attributes
         */
        public static final String SAVE_TO_REDIS = "savetoredis";

        /**
         * globalsessionid key name in request/session attributes
         */
        public static final String GLOBAL_SESSION_ID = "globalsessionid";

        /**
         * mid key name in request/session attributes
         */
        public static final String JVM_SESSION_ID = "jvmsessionid";

        /**
         * cookie name constant
         */
        public static final String LOG_COOKIE = "pgpluslogout";

    }

    /**
     * @createdOn 12-Mar-2016
     * @author kesari
     */
    public static final class RequestParams {
        /**
         * requestdata key name in request/session attributes
         */
        public static final String REQUEST_DATA = "requestdata";
        /**
         * MID key name in request parameter
         */
        public static final String REQUEST_MID = "MID";
        /**
         * ORDER_ID key name in request parameter
         */
        public static final String REQUEST_ORDER_ID = "ORDER_ID";
        /**
         * state key name in request parameter
         */
        public static final String REQUEST_STATE = "state";

    }

    /**
     * @createdOn 12-Mar-2016
     * @author kesari
     */
    public static final class Configurations {
        /**
         * session configuration file name
         */
        public static final String SESSION_CONFIG_FILE = "session.config";
        /**
         * session configuration file name
         */
        public static final String MERCHANT_CONFIG_FILE = "merchant.config";
        /**
         * Redis host configuration in config file
         */
        public static final String REDIS_HOST = "redis.host";
        /**
         * Redis port configuration in config file
         */
        public static final String REDIS_PORT = "redis.port";
        /**
         * Redis sentinel servers configuration in config file
         */
        public static final String REDIS_SENTINEL_SERVERS = "redis.sentinel.servers";
        /**
         * Redis sentinal enabled/disabled configuration in config file Allowed
         * values will be true/false
         */
        public static final String REDIS_SENTINEL_ENABLED = "redis.sentinel.enabled";
        /**
         * Redis max total configuration in config file
         */
        public static final String REDIS_MAX_TOTAL = "redis.max.total";
        /**
         * Redis max indle configuration in config file
         */
        public static final String REDIS_MAX_IDLE = "redis.max.idle";
        /**
         * Redis min idle configuration in config file
         */
        public static final String REDIS_MIN_IDLE = "redis.min.idle";
        /**
         * Redis max wait configuration in config file wait time in milliseconds
         */
        public static final String REDIS_MAX_WAIT = "redis.max.wait";
        /**
         * Local session enabled/disabled configuration in config file Allowed
         * values will be true/false
         */
        public static final String LOCAL_SESSION_ENABLED = "local.session.enabled";

        public static final String REDIS_PASSWORD = "redis.password";

        public static final String REDIS_CLUSTER_NAME = "redis.cluster.name";

        public static final String GLOBAL_TOMCAT_CONNECT_CLUSTER = "global.tomcat.connect.cluster";

        public static final String GLOBAL_TOMCAT_CONNECT_SENTINEL = "global.tomcat.connect.sentinel";

        public static final String GLOBAL_TOMCAT_READ_FROM_CLUSTER = "global.tomcat.read.from.cluster";

        public static final String GLOBAL_TOMCAT_WRITE_ON_CLUSTER = "global.tomcat.write.on.cluster";

        public static final String GLOBAL_TOMCAT_READ_FROM_SENTINEL = "global.tomcat.read.from.sentinel";

        public static final String GLOBAL_TOMCAT_WRITE_ON_SENTINEL = "global.tomcat.write.on.sentinel";

    }

    /**
     * @createdOn 13-Mar-2016
     * @author kesari
     */
    public static final class Separator {
        /**
         * key separator for redis
         */
        public static final String REDIS_KEY_SEPARATOR = ":";
        /**
         * Delimeter used in generation of custom session id It will be used as
         * a separator between mid and orderid
         */
        public static final String MID_ORDERID_SEPARATOR = "_";
        /**
         * Separator used to generate session id It will separate jvm route and
         * mid orderid combination with
         */
        public static final String SESSIONID_SEPARATOR = ".";

    }

    /**
     * Global session prefix
     */
    public static final String GLOBAL_SESSIONID_PREFIX = "14299_";

}
