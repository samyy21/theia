package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;

public interface SequenceGenerator {
    long getNext();
}