package com.alam.payment.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaRetryConfig {

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {

		return new DeadLetterPublishingRecoverer(kafkaTemplate,
				(record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
	}

	@Bean
	public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {

		/*
		 * 3 retries
		 *
		 * 2 seconds between retries
		 */

		FixedBackOff backOff = new FixedBackOff(2000L, 3L);

		return new DefaultErrorHandler(recoverer, backOff);
	}
}