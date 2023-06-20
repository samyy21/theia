package com.paytm.pgplus.biz.core.notification.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizPayRequest;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayRequestBean;
import com.paytm.pgplus.biz.core.notification.service.IPayRequestNotifierService;
import com.paytm.pgplus.kafkabase.v2.config.NewKafkaBeanCreationCondition;
import com.paytm.pgplus.kafkabase.v2.model.GenericKafkaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Conditional(NewKafkaBeanCreationCondition.class)
@Service("payRequestNotifierService")
public class PayRequestNotifierService implements IPayRequestNotifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayRequestNotifierService.class);
    private static final String TOPIC = "CREATEORDERANDPAY_PAY_DWH";

    @Autowired
    @Qualifier("awsAnalyticsPaymentRequestKafkaTemplate")
    private KafkaTemplate<String, GenericKafkaObject> kafkaTemplate;

    @Override
    public void pushPayRequestToKafkaTopic(BizPayRequest bizPayRequest) {
        GenericKafkaObject genericKafkaObject = new GenericKafkaObject(bizPayRequest, TOPIC);
        LOGGER.info("Sending pay request to kafka {}", genericKafkaObject);
        kafkaTemplate.send(TOPIC, genericKafkaObject);
    }

    @Override
    public void pushCopRequestToKafkaTopic(CreateOrderAndPayRequestBean createOrderAndPayRequestBean) {
        GenericKafkaObject genericKafkaObject = new GenericKafkaObject(createOrderAndPayRequestBean, TOPIC);
        LOGGER.info("Sending cop request to kafka {}", genericKafkaObject);
        kafkaTemplate.send(TOPIC, genericKafkaObject);
    }

    @Override
    public void pushPayloadToKafkaTopic(Object request) {
        LOGGER.info("Sending request to kafka topic {}", request);
        GenericKafkaObject genericKafkaObject = new GenericKafkaObject(request, TOPIC);
        LOGGER.info("Sending request to kafka {}", genericKafkaObject);
        kafkaTemplate.send(TOPIC, genericKafkaObject);
    }
}
