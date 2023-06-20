package com.paytm.pgplus.theia.sns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SNSConfig {

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder().region(Region.AP_SOUTH_1)
                .credentialsProvider(InstanceProfileCredentialsProvider.create()).build();
    }

}
