package com.paytm.pgplus.theia.sns.service.impl;

import com.paytm.pgplus.theia.sns.service.SNSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service("snsService")
public class SNSServiceImpl implements SNSService {

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private Environment environment;

    private static final Logger LOGGER = LoggerFactory.getLogger(SNSServiceImpl.class);

    private static String AWS_SNS_LOCALISATION_TOPIC_ARN = "aws.sns.localisation.topic.arn";

    @Override
    public void publish(String message) {
        LOGGER.info("SNS message received {}", message);
        PublishRequest publishRequest = PublishRequest.builder().message(message)
                .topicArn(environment.getProperty(AWS_SNS_LOCALISATION_TOPIC_ARN)).build();
        snsClient.publish(publishRequest);
    }
}