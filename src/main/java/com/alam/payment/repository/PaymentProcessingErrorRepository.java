package com.alam.payment.repository;

import com.alam.payment.entity.PaymentProcessingError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentProcessingErrorRepository extends JpaRepository<PaymentProcessingError, UUID> {
}