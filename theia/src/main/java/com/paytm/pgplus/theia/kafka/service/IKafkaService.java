package com.paytm.pgplus.theia.kafka.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.kafkabase.exception.KafkaBaseException;

public interface IKafkaService {
    void pushData(String topic, Object payload) throws KafkaBaseException, FacadeCheckedException;
}
