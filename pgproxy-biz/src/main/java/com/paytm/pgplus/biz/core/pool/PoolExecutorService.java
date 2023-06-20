package com.paytm.pgplus.biz.core.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.facade.boss.models.response.ChargeFeeConsultResponse;

/*
 * @author Santosh and Agrim
 */
@Component
public class PoolExecutorService {

    private ThreadPoolExecutor consultFeeServicePool;

    @PostConstruct
    private void initConsultFeeServicePool() {
        consultFeeServicePool = new ThreadPoolExecutor(BizConstant.MIN_POOLSIZE, BizConstant.MAX_POOLSIZE,
                BizConstant.KEEPALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true),
                new ThreadFactory() {
                    private final AtomicInteger threadCount = new AtomicInteger();

                    @Override
                    public Thread newThread(final Runnable target) {
                        Thread thread = new Thread(target, new StringBuilder("ConsultFeeThread[")
                                .append(threadCount.incrementAndGet()).append("]").toString());
                        thread.setPriority(Thread.MAX_PRIORITY);
                        return thread;
                    }
                });
        consultFeeServicePool.prestartCoreThread();
        consultFeeServicePool.allowCoreThreadTimeOut(true);
    }

    public Future<ChargeFeeConsultResponse> submitJob(final Callable<ChargeFeeConsultResponse> task) throws Exception {
        return (consultFeeServicePool.submit(task));
    }

}
