package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.theia.test.testflow.AbstractPaymentServiceTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MerchantThemeLoadBalancerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantThemeLoadBalancerTest.class);

    @Autowired
    private MerchantThemeLoadBalancer merchantThemeLoadBalancer;

    @Test
    public void testGetTheme() {
        System.out.println();
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 600; i++) {
            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    // this logic is for alternatively selecting channel between
                    // WEB and WAP
                    /*
                     * String channel = "WAP"; String myString =
                     * Thread.currentThread().getName(); if
                     * (Integer.valueOf(myString
                     * .substring(myString.indexOf("thread") + 7,
                     * myString.length())) % 2 == 0) channel = "WEB";
                     */
                    System.out.println(Thread.currentThread().getName() + " "
                            + merchantThemeLoadBalancer.getTheme("WEB", "ppblll26336908808088"));

                    // System.out.println(Thread.currentThread().getName() + " "
                    // +
                    // CacheManager.getCacheManager().increment(THEMECOUNTER_CACHE,
                    // THEMECOUNTER_WEB));
                }
            });
            try {
                threadPool.awaitTermination(1000l, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("error: ", e);
            }
            System.out.printf("");

        }
    }
}
