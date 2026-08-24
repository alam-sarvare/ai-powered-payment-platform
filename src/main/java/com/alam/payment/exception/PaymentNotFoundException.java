package com.alam.payment.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException(UUID paymentId) {
		super("Payment not found: " + paymentId);
	}

	public PaymentNotFoundException(UUID paymentId, String message) {
		super(message);
	}
}
