package ru.creditbank.creditservice.operation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(
      name = "aggregate_id",
      nullable = false
  )
  private UUID aggregateId;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "event_type",
      nullable = false,
      length = 50
  )
  private OutboxEventType eventType;

  @Column(
      name = "payload",
      nullable = false,
      columnDefinition = "TEXT"
  )
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "status",
      nullable = false,
      length = 20
  )
  private OutboxStatus status;

  @Column(
      name = "attempt_count",
      nullable = false
  )
  private Integer attemptCount;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Column(
      name = "last_error",
      columnDefinition = "TEXT"
  )
  private String lastError;

  @CreationTimestamp
  @Column(
      name = "created_at",
      nullable = false,
      updatable = false
  )
  private Instant createdAt;

  @UpdateTimestamp
  @Column(
      name = "updated_at",
      nullable = false
  )
  private Instant updatedAt;

  @Version
  private Long version;

  public OutboxEventEntity(
      UUID aggregateId,
      OutboxEventType eventType,
      String payload
  ) {
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.status = OutboxStatus.PENDING;
    this.attemptCount = 0;
  }

  public void markProcessed() {
    this.status = OutboxStatus.PROCESSED;
    this.nextAttemptAt = null;
    this.lastError = null;
  }

  public void markRetry(
      Instant nextAttemptAt,
      String error
  ) {
    this.status = OutboxStatus.RETRY;
    this.attemptCount++;
    this.nextAttemptAt = nextAttemptAt;
    this.lastError = error;
  }
}
