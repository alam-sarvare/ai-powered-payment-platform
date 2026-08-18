package com.alam.payment.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handlePaymentNotFound(PaymentNotFoundException ex) {

		Map<String, Object> response = new HashMap<>();

		response.put("timestamp", LocalDateTime.now());
		response.put("status", 404);
		response.put("error", "PAYMENT_NOT_FOUND");
		response.put("message", ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		Map<String, Object> response = new HashMap<>();

		response.put("timestamp", LocalDateTime.now());
		response.put("status", 400);
		response.put("error", "VALIDATION_FAILED");
		response.put("errors", errors);

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(InvalidPaymentStateException.class)
	public ResponseEntity<String> handleInvalidState(InvalidPaymentStateException exception) {

		return ResponseEntity.badRequest().body(exception.getMessage());
	}
}