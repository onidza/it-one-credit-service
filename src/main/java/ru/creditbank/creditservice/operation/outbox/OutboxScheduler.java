package ru.creditbank.creditservice.operation.outbox;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxStatus;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

  private final OutboxService outboxService;
  private final OutboxProcessor outboxProcessor;

  @Scheduled(fixedDelay = 5000)
  public void processLoanIssuanceOutbox() {
    List<OutboxEventEntity> events = outboxService.findReadyForProcessing(
        OutboxStatus.PENDING,
        OutboxStatus.RETRY,
        Instant.now()
    );

    for (OutboxEventEntity event : events) {
      outboxProcessor.process(event);
    }
  }
}
