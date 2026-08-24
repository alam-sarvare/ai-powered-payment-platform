package com.alam.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payment_processing_errors")
public class PaymentProcessingError {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID paymentId;

	private String stage;

	@Column(length = 2000)
	private String errorMessage;

	private LocalDateTime createdAt;

	public PaymentProcessingError() {
	}

	// getters and setters
}