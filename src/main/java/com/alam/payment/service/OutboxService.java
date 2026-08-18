package com.alam.payment.service;

import com.alam.payment.entity.OutboxEvent;
import com.alam.payment.entity.OutboxStatus;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {

	private final OutboxEventRepository outboxEventRepository;

	private final ObjectMapper objectMapper;

	public void createPaymentCreatedEvent(PaymentCreatedEvent event) {

		try {

			String payload = objectMapper.writeValueAsString(event);

			OutboxEvent outboxEvent = OutboxEvent.builder().aggregateId(event.paymentId()).eventType("PaymentCreated")
					.topic("payment-created").payload(payload).status(OutboxStatus.PENDING).build();

			outboxEventRepository.save(outboxEvent);

		} catch (JsonProcessingException exception) {

			throw new RuntimeException("Failed to serialize payment event", exception);
		}
	}
}