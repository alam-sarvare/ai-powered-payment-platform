package com.alam.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentCreatedTopic() {

        return new NewTopic("payment-created",3,(short) 1);
    }
}