package com.alam.payment.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class PaymentRiskController {

	private final PaymentRiskAnalyzer paymentRiskAnalyzer;

	@PostMapping("/risk")
	public PaymentRiskResponse analyzeRisk(@RequestBody PaymentRiskRequest request) {

		return paymentRiskAnalyzer.analyze(request);
	}
}