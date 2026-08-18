package com.alam.payment.entity;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events", indexes = { @Index(name = "idx_outbox_status", columnList = "status"),
		@Index(name = "idx_outbox_created_at", columnList = "created_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "topic", nullable = false)
	private String topic;

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OutboxStatus status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@PrePersist
	public void prePersist() {

		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}

		if (status == null) {
			status = OutboxStatus.PENDING;
		}
	}
}