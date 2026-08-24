package com.alam.payment.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

	private final Counter paymentCounter;

	public PaymentMetrics(MeterRegistry meterRegistry) {

		paymentCounter = Counter.builder("payments.created").description("Number of payments created")
				.register(meterRegistry);
	}

	public void paymentCreated() {
		paymentCounter.increment();
	}
}