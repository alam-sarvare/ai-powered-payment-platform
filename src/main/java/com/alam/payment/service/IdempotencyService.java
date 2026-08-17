package com.alam.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

	private static final String PREFIX = "payment:idempotency:";

	private static final Duration TTL = Duration.ofHours(24);

	private final RedisService redisService;

	public Object getExistingResponse(String idempotencyKey) {

		return redisService.get(PREFIX + idempotencyKey);
	}

	public void saveResponse(String idempotencyKey, Object response) {

		redisService.save(PREFIX + idempotencyKey, response, TTL);
	}

	public boolean exists(String idempotencyKey) {

		return redisService.exists(PREFIX + idempotencyKey);
	}
}