package com.alam.payment.kafka;

import com.alam.payment.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

	private static final String TOPIC = "payment-created";

	private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

	public void publishPaymentCreated(PaymentCreatedEvent event) {

		kafkaTemplate.send(TOPIC, event.paymentId().toString(), event);
	}
}