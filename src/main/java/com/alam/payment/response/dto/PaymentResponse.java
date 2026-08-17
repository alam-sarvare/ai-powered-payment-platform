package com.alam.payment.response.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.alam.payment.entity.PaymentStatus;

public record PaymentResponse(
		 	UUID paymentId,
	        String customerId,
	        BigDecimal amount,
	        String currency,
	        PaymentStatus status,
	        LocalDateTime createdAt,
	        LocalDateTime updatedAt
		) {
}
