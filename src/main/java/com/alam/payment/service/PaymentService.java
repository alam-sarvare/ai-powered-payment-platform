package com.alam.payment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alam.payment.entity.Payment;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.exception.PaymentNotFoundException;
import com.alam.payment.kafka.PaymentEventProducer;
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

		PaymentResponse response = toResponse(savedPayment);

		/*
		 * 4. Create Kafka event.
		 */

		PaymentCreatedEvent event = new PaymentCreatedEvent(savedPayment.getId(), savedPayment.getCustomerId(),
				savedPayment.getAmount(), savedPayment.getCurrency(), savedPayment.getIdempotencyKey());

		/*
		 * 5. Store event in Outbox.
		 */

		outboxService.createPaymentCreatedEvent(event);

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
}
