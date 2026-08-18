package com.alam.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreatedEvent(

        UUID paymentId,

        String customerId,

        BigDecimal amount,

        String currency,

        String idempotencyKey

) {
}