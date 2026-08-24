package com.alam.payment.ai;

import java.math.BigDecimal;

public record PaymentRiskRequest(

		String customerId,

		BigDecimal amount,

		String currency,

		String paymentType,

		String description) {
}