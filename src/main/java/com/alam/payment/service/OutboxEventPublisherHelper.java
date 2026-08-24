package com.alam.payment.service;

import com.alam.payment.entity.OutboxEvent;
import com.alam.payment.entity.OutboxStatus;
import com.alam.payment.event.PaymentCreatedEvent;
import com.alam.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Separate bean so that @Transactional is applied via the Spring proxy.
 * Calling publishEvent() from within OutboxPublisher directly (same bean)
 * would bypass the proxy and the transaction would never be started.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPublisherHelper {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishEvent(OutboxEvent outboxEvent) {

        try {

            PaymentCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), PaymentCreatedEvent.class);

            kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getAggregateId().toString(), event)
                    .get(10, TimeUnit.SECONDS);

            outboxEvent.setStatus(OutboxStatus.PUBLISHED);
            outboxEvent.setPublishedAt(java.time.LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

            log.info("Outbox event published: {}", outboxEvent.getId());

        } catch (Exception exception) {

            log.error("Failed to publish outbox event: {}", outboxEvent.getId(), exception);

            outboxEvent.setStatus(OutboxStatus.FAILED);
            outboxEventRepository.save(outboxEvent);
        }
    }
}
