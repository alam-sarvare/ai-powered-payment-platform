package com.alam.payment.kafka;

import com.alam.payment.event.PaymentRiskAnalysisEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRiskProducer {

	private static final String TOPIC = "payment-risk-analysis";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publish(PaymentRiskAnalysisEvent event) {

		String correlationId = MDC.get("X-Correlation-ID");

		Message<PaymentRiskAnalysisEvent> message = MessageBuilder.withPayload(event)
				.setHeader(KafkaHeaders.TOPIC, TOPIC).setHeader(KafkaHeaders.KEY, event.paymentId().toString())
				.setHeader("X-Correlation-ID", correlationId).build();

		kafkaTemplate.send(message);
	}
}