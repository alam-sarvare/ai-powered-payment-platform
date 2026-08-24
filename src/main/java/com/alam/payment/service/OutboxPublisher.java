package com.alam.payment.service;

import com.alam.payment.entity.OutboxEvent;
import com.alam.payment.entity.OutboxStatus;
import com.alam.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

	private final OutboxEventRepository outboxEventRepository;

	// Injecting the helper bean so @Transactional goes through the Spring proxy (fixes self-invocation bug)
	private final OutboxEventPublisherHelper outboxEventPublisherHelper;

	/** How many consecutive idle polls before slowing down. */
	private static final int IDLE_THRESHOLD = 3;

	/** Maximum back-off multiplier (max skips = MAX_BACKOFF_FACTOR - 1 polls skipped). */
	private static final int MAX_BACKOFF_FACTOR = 12;

	private final AtomicInteger idleCount = new AtomicInteger(0);
	private final AtomicInteger skipCount = new AtomicInteger(0);

	@Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
	public void publishPendingEvents() {

		// Adaptive back-off: skip execution instead of Thread.sleep() to avoid blocking the scheduler thread
		int idle = idleCount.get();
		if (idle >= IDLE_THRESHOLD) {
			int factor = Math.min(idle - IDLE_THRESHOLD + 1, MAX_BACKOFF_FACTOR);
			int currentSkip = skipCount.getAndIncrement();
			if (currentSkip < factor - 1) {
				log.debug("Skipping outbox poll (back-off skip {}/{})", currentSkip + 1, factor - 1);
				return;
			}
			skipCount.set(0);
		}

		List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

		if (events.isEmpty()) {
			idleCount.incrementAndGet();
			log.debug("No pending outbox events (idle count: {})", idleCount.get());
			return;
		}

		// Reset idle counter when there is actual work to do
		idleCount.set(0);
		skipCount.set(0);

		for (OutboxEvent event : events) {
			// Delegate to helper bean so @Transactional proxy is engaged (not self-invocation)
			outboxEventPublisherHelper.publishEvent(event);
		}
	}
}