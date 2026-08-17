package com.alam.payment.response.dto;

import com.alam.payment.entity.PaymentStatus;

import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(

        @NotNull(message = "Status is required")
        PaymentStatus status
) {
}