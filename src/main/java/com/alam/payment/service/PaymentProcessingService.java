package com.alam.payment.service;

import com.alam.payment.entity.Payment;
import com.alam.payment.entity.PaymentStatus;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.exception.InvalidPaymentStateException;
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

	private final PaymentStateMachine paymentStateMachine;

	@Transactional
	public void processPayment(PaymentCreatedEvent event) {

		Payment payment = paymentRepository.findById(event.paymentId())
				.orElseThrow(() -> new RuntimeException("Payment not found: " + event.paymentId()));

		/*
		 * Idempotency protection.
		 *
		 * If payment has already reached a final state, don't process it again.
		 */

		if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {

			log.info("Payment {} already processed with status {}", payment.getId(), payment.getStatus());

			return;
		}

		/*
		 * PENDING → PROCESSING
		 */

		changeStatus(payment, PaymentStatus.PROCESSING);

		paymentRepository.save(payment);

		log.info("Payment {} moved to PROCESSING", payment.getId());

		/*
		 * Simulate payment processing.
		 *
		 * Later this will call an external payment gateway.
		 */

		boolean successful = true;

		if (successful) {

			changeStatus(payment, PaymentStatus.SUCCESS);

		} else {

			changeStatus(payment, PaymentStatus.FAILED);
		}

		paymentRepository.save(payment);

		log.info("Payment {} completed with status {}", payment.getId(), payment.getStatus());
	}

	private void changeStatus(Payment payment, PaymentStatus nextStatus) {

		PaymentStatus currentStatus = payment.getStatus();

		if (!paymentStateMachine.isValidTransition(currentStatus, nextStatus)) {

			throw new InvalidPaymentStateException(currentStatus, nextStatus);
		}

		payment.setStatus(nextStatus);
	}
}