package com.alam.payment.exception;

import com.alam.payment.entity.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPaymentStateException(PaymentStatus current, PaymentStatus requested) {

		super("Invalid payment state transition: " + current + " -> " + requested);
	}
}