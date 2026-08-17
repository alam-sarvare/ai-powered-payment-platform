package com.alam.payment.request.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreatePaymentRequest (
		@NotBlank(message = "Customer ID is required")
        String customerId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be a 3-letter ISO code"
        )
        String currency,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey
		){

}
