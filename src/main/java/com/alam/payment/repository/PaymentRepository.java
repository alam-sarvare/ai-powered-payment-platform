package com.alam.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alam.payment.entity.Payment;
import com.alam.payment.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
	Optional<Payment> findByIdempotencyKey(String idempotencyKey);

	List<Payment> findByCustomerId(String customerId);

	List<Payment> findByStatus(PaymentStatus status);

}
