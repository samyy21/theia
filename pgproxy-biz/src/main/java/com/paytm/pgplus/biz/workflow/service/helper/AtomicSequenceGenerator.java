package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AtomicSequenceGenerator implements SequenceGenerator {

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private AtomicLong value = new AtomicLong(1);

    private volatile LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0, 0, 0));

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @Override
    public long getNext() {
        LocalDateTime currentTime = LocalDateTime.now();
        endDate = getRajasthanDateTimeFromRedis();
        if (!currentTime.isBefore(endDate)) {
            synchronized (this) {
                if (!currentTime.isBefore(endDate)) {
                    endDate = setEndDate();
                    value.set(1);
                    updateRajasthanDateTimeInRedis(endDate);
                    updateRajasthanSequenceInRedis(value.longValue());
                }
            }
        }
        long nextValue = 1;
        synchronized (this) {
            // nextValue = value.getAndIncrement();
            nextValue = getRajasthanSequenceFromRedis();
            updateRajasthanSequenceInRedis(nextValue + 1);
        }
        return nextValue;
    }

    private LocalDateTime setEndDate() {
        // 8PM to 8PM except on 30th March, it is till - 1 April 12 am
        if (endDate.getDayOfMonth() == 30 && endDate.getMonthValue() == 3) {
            endDate = endDate.of(Year.now().getValue(), Month.APRIL, 1, 0, 0, 0, 0);
        }
        // for 1st April set to 8PM
        else if (endDate.getDayOfMonth() == 1 && endDate.getMonthValue() == 4 && endDate.getHour() != 20) {
            endDate = endDate.withHour(20);
        }
        // Set to next day 8PM
        else {
            endDate = endDate.plusDays(1);
            endDate = endDate.withHour(20);
        }
        return endDate;
    }

    private LocalDateTime getRajasthanDateTimeFromRedis() {
        if (theiaTransactionalRedisUtil.hget(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_DATETIME_REDIS_KEY) == null) {
            if (endDate == null)
                endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0, 0, 0));
            theiaTransactionalRedisUtil.hset(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                    TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_DATETIME_REDIS_KEY, endDate, 86400);
        }
        return (LocalDateTime) theiaTransactionalRedisUtil.hget(
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_DATETIME_REDIS_KEY);
    }

    private Long getRajasthanSequenceFromRedis() {
        if (theiaTransactionalRedisUtil.hget(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_VALUE_REDIS_KEY) == null)
            theiaTransactionalRedisUtil.hset(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                    TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_VALUE_REDIS_KEY, value.longValue(), 86400);
        return (Long) theiaTransactionalRedisUtil.hget(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_VALUE_REDIS_KEY);
    }

    private void updateRajasthanDateTimeInRedis(LocalDateTime updatedDateTime) {
        theiaTransactionalRedisUtil.hset(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_DATETIME_REDIS_KEY, updatedDateTime, 86400);
    }

    private void updateRajasthanSequenceInRedis(long sequenceValue) {
        theiaTransactionalRedisUtil.hset(TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_REDIS_KEY,
                TheiaConstant.RequestParams.RAJASTHAN_SEQUENCE_VALUE_REDIS_KEY, sequenceValue, 86400);
    }
}