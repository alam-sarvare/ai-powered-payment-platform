package com.alam.payment.kafka;

import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

	private final PaymentProcessingService paymentProcessingService;

	@KafkaListener(topics = "payment-created", groupId = "payment-processing-group")
	public void consumePaymentCreated(PaymentCreatedEvent event, Acknowledgment acknowledgment) {

		log.info("Received payment event: {}", event.paymentId());

		paymentProcessingService.processPayment(event);

		// Manually acknowledge AFTER successful processing (ack-mode=manual is set globally)
		acknowledgment.acknowledge();

		log.info("Payment event processed: {}", event.paymentId());
	}
}