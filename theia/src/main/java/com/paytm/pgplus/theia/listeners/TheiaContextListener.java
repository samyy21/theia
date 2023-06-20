// This File is the sole property of Paytm(One97 Communications Limited)
package com.paytm.pgplus.theia.listeners;

import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.common.util.EagerLoadable;
import com.paytm.pgplus.common.util.ReflectionUtil;
import com.paytm.pgplus.httpclient.config.ClientContextContainer;
import com.paytm.pgplus.requestidclient.IdManager;
import com.paytm.pgplus.requestidclient.enums.Groups;
import com.paytm.pgplus.requestidclient.models.SubscriberMetaInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.helper.RedisEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author amitdubey
 * @date Nov 8, 2016
 */

@Component
public class TheiaContextListener implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaContextListener.class);
    public static final int SYSTEM_ERROR = 80;

    @Autowired
    @Qualifier("clientContextContainer")
    private ClientContextContainer clientContextContainer;

    private ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        try {
            if (context == null) {
                final SubscriberMetaInfo subscriberMetaInfo = new SubscriberMetaInfo(5, 50, 2000, Groups.PGPROXY, 10L,
                        TimeUnit.MILLISECONDS, ConfigurationUtil.getProperty("requestidservice.endpoint"));

                ReflectionUtil.loadAnnoatedClass("com.paytm.pgplus", EagerLoadable.class);
                IdManager.initialize(subscriberMetaInfo);

                LOGGER.info("Theia context listener getting loaded ");
                initializeRedisClients();
                initializeEventsOnRedisCluster();
                context = applicationContext;
            }
            ErrorCodeConstants.getAlipayResponseImmutableMap();

            // } catch (final InstanceAlreadyExistsException e) {
            // LOGGER.error("Instance already exist: ", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Exception occured while loading the class dynamically: ", e);
            System.exit(SYSTEM_ERROR);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while context initializing for listener: ", e);
        }

    }

    private void initializeRedisClients() {
        // RedisClientService.getInstance();
        // RedisClientLettuceService.getInstance();
    }

    private void initializeEventsOnRedisCluster() {
        RedisEventHandler.enableRedisEventsOnAllRedis();
    }

}