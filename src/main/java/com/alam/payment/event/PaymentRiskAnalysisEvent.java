package com.alam.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRiskAnalysisEvent(

		UUID paymentId,

		String customerId,

		BigDecimal amount,

		String currency,

		String paymentType,

		String description) {
}