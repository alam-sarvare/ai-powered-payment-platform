package com.alam.payment.kafka;

import com.alam.payment.ai.PaymentRiskAnalyzer;
import com.alam.payment.ai.PaymentRiskCache;
import com.alam.payment.ai.PaymentRiskRequest;
import com.alam.payment.ai.PaymentRiskResponse;
import com.alam.payment.event.PaymentRiskAnalysisEvent;
import com.alam.payment.entity.Payment;
import com.alam.payment.entity.PaymentProcessingError;
import com.alam.payment.repository.PaymentProcessingErrorRepository;
import com.alam.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRiskAnalysisConsumer {

	private final PaymentRepository paymentRepository;

	private final PaymentRiskAnalyzer paymentRiskAnalyzer;

	private final PaymentRiskCache paymentRiskCache;

	private final PaymentProcessingErrorRepository errorRepository;

	@DltHandler
	public void handleDlt(PaymentRiskAnalysisEvent event) {

		log.error("Payment {} moved to DLT", event.paymentId());

		PaymentProcessingError error = new PaymentProcessingError();

		error.setPaymentId(event.paymentId());

		error.setStage("AI_RISK_ANALYSIS");

		error.setErrorMessage("Payment risk analysis failed after retries");

		error.setCreatedAt(LocalDateTime.now());

		errorRepository.save(error);
	}

	@RetryableTopic(attempts = "4", backoff = @Backoff(delay = 2000, multiplier = 2.0), topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
	@KafkaListener(topics = "payment-risk-analysis", groupId = "payment-risk-group")
	@Transactional
	public void analyzePayment(PaymentRiskAnalysisEvent event,
			@Header(name = "X-Correlation-ID", required = false) String correlationId) {

		log.info("Received risk analysis event for payment {}", event.paymentId());

		MDC.put("X-Correlation-ID", correlationId);
		Payment payment = paymentRepository.findById(event.paymentId())
				.orElseThrow(() -> new RuntimeException("Payment not found: " + event.paymentId()));

		/*
		 * Idempotency protection.
		 *
		 * If risk has already been calculated, don't call the AI again.
		 */

		if (payment.getRiskLevel() != null) {

			log.info("Risk already calculated for payment {}", payment.getId());

			return;
		}

		PaymentRiskRequest request = new PaymentRiskRequest(event.customerId(), event.amount(), event.currency(),
				event.paymentType(), event.description());

		PaymentRiskResponse response = paymentRiskAnalyzer.analyze(request);

		paymentRiskCache.save(event.paymentId(), response);

		payment.setRiskLevel(response.riskLevel());

		payment.setRiskReason(response.reason());

		paymentRepository.save(payment);

		log.info("Payment {} risk calculated as {}", payment.getId(), response.riskLevel());
	}
}