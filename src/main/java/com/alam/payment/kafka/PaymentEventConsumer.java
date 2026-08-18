package com.alam.payment.kafka;

import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

	private final PaymentProcessingService paymentProcessingService;

	@KafkaListener(topics = "payment-created", groupId = "payment-processing-group")
	public void consumePaymentCreated(PaymentCreatedEvent event) {

		log.info("Received payment event: {}", event.paymentId());

		paymentProcessingService.processPayment(event);

		log.info("Payment event processed: {}", event.paymentId());
	}
}