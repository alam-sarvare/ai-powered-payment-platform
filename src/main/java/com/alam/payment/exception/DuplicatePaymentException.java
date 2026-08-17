package com.alam.payment.exception;

public class DuplicatePaymentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicatePaymentException() {

		super("Payment with this idempotency key already exists");
	}
}