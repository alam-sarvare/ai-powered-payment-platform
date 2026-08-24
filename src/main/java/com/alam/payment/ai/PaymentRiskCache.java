package com.alam.payment.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRiskCache {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String KEY_PREFIX = "payment:risk:";

	private String buildKey(UUID paymentId) {
		return KEY_PREFIX + paymentId;
	}

	public void save(UUID paymentId, PaymentRiskResponse response) {

		redisTemplate.opsForValue().set(buildKey(paymentId), response, Duration.ofHours(24));
	}

	public PaymentRiskResponse get(UUID paymentId) {

		Object value = redisTemplate.opsForValue().get(buildKey(paymentId));

		if (value instanceof PaymentRiskResponse response) {
			return response;
		}

		return null;
	}

	public void delete(UUID paymentId) {

		redisTemplate.delete(buildKey(paymentId));
	}
}