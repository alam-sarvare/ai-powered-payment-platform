package com.alam.payment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alam.payment.ai.PaymentRiskCache;
import com.alam.payment.ai.PaymentRiskResponse;
import com.alam.payment.config.PaymentMetrics;
import com.alam.payment.entity.Payment;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.event.PaymentRiskAnalysisEvent;
import com.alam.payment.exception.PaymentNotFoundException;
import com.alam.payment.kafka.PaymentEventProducer;
import com.alam.payment.kafka.PaymentRiskProducer;
import com.alam.payment.repository.PaymentRepository;
import com.alam.payment.request.dto.CreatePaymentRequest;
import com.alam.payment.response.dto.PaymentResponse;
import com.alam.payment.response.dto.UpdatePaymentStatusRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final IdempotencyService idempotencyService;
	private final OutboxService outboxService;
	private final PaymentRiskProducer paymentRiskProducer;
	private final PaymentRiskCache paymentRiskCache;
	private final PaymentMetrics paymentMetrics;

	@Transactional
	public PaymentResponse createPayment(CreatePaymentRequest request) {

		/*
		 * 1. Check Redis.
		 */

		Object cachedResponse = idempotencyService.getExistingResponse(request.idempotencyKey());

		if (cachedResponse != null) {

			return (PaymentResponse) cachedResponse;
		}

		/*
		 * 2. Check PostgreSQL.
		 */

		Payment existingPayment = paymentRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);

		if (existingPayment != null) {

			PaymentResponse response = toResponse(existingPayment);

			idempotencyService.saveResponse(request.idempotencyKey(), response);

			return response;
		}

		/*
		 * 3. Create Payment.
		 */

		Payment payment = Payment.builder().customerId(request.customerId()).amount(request.amount())
				.currency(request.currency()).idempotencyKey(request.idempotencyKey()).build();

		Payment savedPayment = paymentRepository.save(payment);

		PaymentRiskAnalysisEvent event = new PaymentRiskAnalysisEvent(savedPayment.getId(),
				savedPayment.getCustomerId(), savedPayment.getAmount(), savedPayment.getCurrency(), "PAYMENT",
				"Payment transaction");

		paymentRiskProducer.publish(event);

		PaymentResponse response = toResponse(savedPayment);
		paymentMetrics.paymentCreated();

		/*
		 * 4. Create Kafka event.
		 */

		PaymentCreatedEvent event1 = new PaymentCreatedEvent(savedPayment.getId(), savedPayment.getCustomerId(),
				savedPayment.getAmount(), savedPayment.getCurrency(), savedPayment.getIdempotencyKey());

		/*
		 * 5. Store event in Outbox.
		 */

		outboxService.createPaymentCreatedEvent(event1);

		/*
		 * 6. Cache response.
		 */

		idempotencyService.saveResponse(request.idempotencyKey(), response);

		return response;
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPayment(UUID paymentId) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		return toResponse(payment);
	}

	@Transactional(readOnly = true)
	public List<PaymentResponse> getPaymentsByCustomer(String customerId) {

		return paymentRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
	}

	@Transactional
	public PaymentResponse updateStatus(UUID paymentId, UpdatePaymentStatusRequest request) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		payment.setStatus(request.status());

		Payment updatedPayment = paymentRepository.save(payment);

		return toResponse(updatedPayment);
	}

	private PaymentResponse toResponse(Payment payment) {

		return new PaymentResponse(payment.getId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(),
				payment.getStatus(), payment.getCreatedAt(), payment.getUpdatedAt());
	}

	public PaymentRiskResponse getRisk(UUID paymentId) {

		// 1. Check Redis

		PaymentRiskResponse cached = paymentRiskCache.get(paymentId);

		if (cached != null) {

			return cached;
		}

		// 2. If Redis miss, check DB

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		if (payment.getRiskLevel() == null) {
			// Risk analysis is still pending — surface a 404 so callers can retry
			throw new PaymentNotFoundException(paymentId,
					"Risk analysis not yet available for payment " + paymentId);
		}

		PaymentRiskResponse response = new PaymentRiskResponse(payment.getRiskLevel(), payment.getRiskReason());

		// 3. Populate Redis

		paymentRiskCache.save(paymentId, response);

		return response;
	}
}
