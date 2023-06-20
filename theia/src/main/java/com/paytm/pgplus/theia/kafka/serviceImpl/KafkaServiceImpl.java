package com.paytm.pgplus.theia.kafka.serviceImpl;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.kafkabase.exception.KafkaBaseException;
import com.paytm.pgplus.kafkabase.producer.IKafkaBaseProducer;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.kafka.service.IKafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KafkaServiceImpl implements IKafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaServiceImpl.class);

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier("kafkaBaseProducer")
    private IKafkaBaseProducer kafkaBaseProducer;

    @Override
    public void pushData(final String topic, final Object payload) throws KafkaBaseException, FacadeCheckedException {

        if (null != topic) {
            LOGGER.debug("Topic:{} producing : {} ", topic, payload);
            try {
                kafkaBaseProducer.produce(topic, JsonMapper.mapObjectToJson(payload));
            } catch (KafkaBaseException e) {
                statsDUtils.pushException(e.getClass().getSimpleName());
                throw e;
            }
            LOGGER.debug("Topic:{} produced : {} ", topic, payload);
        }
    }

}
