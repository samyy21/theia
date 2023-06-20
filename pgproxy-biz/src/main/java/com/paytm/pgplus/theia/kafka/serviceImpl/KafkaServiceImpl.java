package com.paytm.pgplus.theia.kafka.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.kafkabase.exception.KafkaBaseException;
import com.paytm.pgplus.kafkabase.producer.IKafkaBaseProducer;
import com.paytm.pgplus.theia.kafka.service.IKafkaService;

@Component
public class KafkaServiceImpl implements IKafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaServiceImpl.class);

    @Autowired
    @Qualifier("kafkaBaseProducer")
    private IKafkaBaseProducer kafkaBaseProducer;

    @Override
    public void pushData(final String topic, final Object payload) throws KafkaBaseException, FacadeCheckedException {

        if (null != topic) {
            LOGGER.debug("Topic:{} producing : {} ", topic, payload);
            kafkaBaseProducer.produce(topic, JsonMapper.mapObjectToJson(payload));
            LOGGER.debug("Topic:{} produced : {} ", topic, payload);
        }
    }

}
