package com.alam.payment.service;

import com.alam.payment.entity.Payment;
import com.alam.payment.entity.PaymentStatus;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

	private final PaymentRepository paymentRepository;

	@Transactional
	public void processPayment(PaymentCreatedEvent event) {

		Payment payment = paymentRepository.findById(event.paymentId()).orElse(null);

		if (payment == null) {

			log.error("Payment not found: {}", event.paymentId());

			return;
		}

		/*
		 * Move payment to PROCESSING.
		 */

		payment.setStatus(PaymentStatus.PROCESSING);

		paymentRepository.save(payment);

		log.info("Payment {} moved to PROCESSING", event.paymentId());

		/*
		 * Simulate payment processing.
		 */

		boolean successful = true;

		if (successful) {

			payment.setStatus(PaymentStatus.SUCCESS);

			log.info("Payment {} completed successfully", event.paymentId());

		} else {

			payment.setStatus(PaymentStatus.FAILED);

			log.error("Payment {} failed", event.paymentId());
		}

		paymentRepository.save(payment);
	}
}