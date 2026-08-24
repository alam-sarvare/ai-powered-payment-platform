package com.alam.payment.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRiskAnalyzer {

	private final ChatClient chatClient;

	public PaymentRiskResponse analyze(PaymentRiskRequest request) {

		String prompt = """
				You are a payment risk analysis system.

				Analyze this payment:

				Customer ID: %s
				Amount: %s
				Currency: %s
				Payment Type: %s
				Description: %s

				Classify the risk as exactly one of:

				LOW
				MEDIUM
				HIGH

				Provide a short reason.

				Return ONLY:

				Risk: LOW
				Reason: example reason

				Do not provide anything else.
				""".formatted(request.customerId(), request.amount(), request.currency(), request.paymentType(),
				request.description());

		String response = chatClient.prompt().user(prompt).call().content();

		return parseResponse(response);
	}

	private PaymentRiskResponse parseResponse(String response) {

		String risk = "LOW";
		String reason = response;

		for (String line : response.split("\\R")) {

			if (line.startsWith("Risk:")) {
				risk = line.substring(5).trim().toUpperCase();
			}

			if (line.startsWith("Reason:")) {
				reason = line.substring(7).trim();
			}
		}

		RiskLevel riskLevel;

		try {
			riskLevel = RiskLevel.valueOf(risk);

		} catch (IllegalArgumentException exception) {

			riskLevel = RiskLevel.MEDIUM;
		}

		return new PaymentRiskResponse(riskLevel, reason);
	}
}