package ru.creditbank.creditservice.operation.outbox;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxStatus;
import ru.creditbank.creditservice.operation.repository.OutboxRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepository outboxRepository;
  private final Clock clock;

  @Transactional
  public void markProcessed(UUID eventId) {
    OutboxEventEntity event =
        outboxRepository.findById(eventId)
            .orElseThrow();

    event.markProcessed();
  }

  @Transactional
  public void markRetry(
      UUID eventId,
      String error
  ) {
    OutboxEventEntity event =
        outboxRepository.findById(eventId)
            .orElseThrow();

    event.markRetry(
        clock.instant().plusSeconds(30),
        error
    );
  }

  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public List<OutboxEventEntity> findReadyForProcessing(
      OutboxStatus pending,
      OutboxStatus retry,
      Instant now
  ) {
    return outboxRepository.findReadyForProcessing(
        pending,
        retry,
        now
    );
  }
}
