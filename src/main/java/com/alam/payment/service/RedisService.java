package com.alam.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void save(String key, Object value, Duration expiration) {

		redisTemplate.opsForValue().set(key, value, expiration);
	}

	public Object get(String key) {

		return redisTemplate.opsForValue().get(key);
	}

	public boolean exists(String key) {

		Boolean result = redisTemplate.hasKey(key);

		return Boolean.TRUE.equals(result);
	}

	public void delete(String key) {

		redisTemplate.delete(key);
	}
}