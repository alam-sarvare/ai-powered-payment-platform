package com.alam.payment.ai;

public record PaymentRiskResponse(

		RiskLevel riskLevel,

		String reason) {
}