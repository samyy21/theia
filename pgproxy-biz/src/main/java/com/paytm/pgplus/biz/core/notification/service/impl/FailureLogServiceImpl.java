package com.paytm.pgplus.biz.core.notification.service.impl;

import com.paytm.pgplus.biz.core.notification.service.IFailureLogService;
import com.paytm.pgplus.biz.core.model.request.FailureLogBean;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.kafkabase.v2.config.NewKafkaBeanCreationCondition;
import com.paytm.pgplus.kafkabase.v2.model.GenericKafkaObject;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ROUTE_CACHE_CARD_FOR_ALL_PAYMODES;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_STATSD_FOR_DWH_KAFKA;
import static com.paytm.pgplus.stats.constant.StatsDConstants.*;

@Conditional(NewKafkaBeanCreationCondition.class)
@Service("failureLogServiceImpl")
public class FailureLogServiceImpl implements IFailureLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailureLogServiceImpl.class);
    private static final String TOPIC = "THEIA_FAILURE_LOG";

    @Autowired
    @Qualifier("awsAnalyticsPaymentRequestKafkaTemplate")
    private KafkaTemplate<String, GenericKafkaObject> kafkaTemplate;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public void pushFailureLogToKafka(FailureLogBean failureLogBean) {
        GenericKafkaObject genericKafkaObject = new GenericKafkaObject(failureLogBean, TOPIC);
        LOGGER.info("Sending failure log to kafka {}", genericKafkaObject);
        if (ff4jUtils.isFeatureEnabledOnMid(failureLogBean.getMid(), ENABLE_STATSD_FOR_DWH_KAFKA, false)) {
            statsDUtils.pushResponse("DWH_kafka_failure", buildFacadeStatsDExceptionLogRequest(failureLogBean, TOPIC));
        }
        kafkaTemplate.send(TOPIC, genericKafkaObject);
    }

    private Map<String, String> buildFacadeStatsDExceptionLogRequest(FailureLogBean failureLogBean, String topic) {

        Map<String, String> requestLogMap = new HashMap<>();
        requestLogMap.put("MID", failureLogBean.getMid());
        requestLogMap.put("Topic", topic);
        requestLogMap.put("TheiaApiName", failureLogBean.getTheiaApiName());
        requestLogMap.put("TheiaErrorCode", failureLogBean.getTheiaErrorCode());
        requestLogMap.put("TheiaErrorMessage", failureLogBean.getTheiaErrorMessage());
        requestLogMap.put("PaymentMode", failureLogBean.getPaymentMode());
        requestLogMap.put("ChannelCode", failureLogBean.getChannelCode());
        requestLogMap.put("ApiName", failureLogBean.getApiName());
        requestLogMap.put("ApiErrorCode", failureLogBean.getApiErrorCode());
        requestLogMap.put("ApiErrorMessage", failureLogBean.getApiErrorMessage());
        return requestLogMap;
    }
}
