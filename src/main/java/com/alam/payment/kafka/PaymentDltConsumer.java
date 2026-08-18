package com.alam.payment.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentDltConsumer {

	@KafkaListener(topics = "payment-created.DLT", groupId = "payment-dlt-group")
	public void consumeDlt(ConsumerRecord<String, Object> record) {

		log.error("Payment event moved to DLT. " + "topic={}, partition={}, offset={}, key={}, value={}",
				record.topic(), record.partition(), record.offset(), record.key(), record.value());

		/*
		 * Later we can:
		 *
		 * 1. Store failed event in DB 2. Alert operations team 3. Provide retry API 4.
		 * Build monitoring dashboard
		 */
	}
}