package ru.creditbank.creditservice.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.creditbank.creditservice.operation.dto.publisher.CreditApprovedEventDTO;
import ru.creditbank.creditservice.operation.dto.publisher.CreditStatusChangedEventDTO;
import ru.creditbank.creditservice.operation.service.LoanIssuanceSagaOrchestrator;
import ru.creditbank.creditservice.operation.service.NotificationService;

@Component
@RequiredArgsConstructor
public class CreditStatusChangedListener {

  private final LoanIssuanceSagaOrchestrator sagaOrchestrator;
  private final NotificationService notificationService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(CreditStatusChangedEventDTO event) {
    notificationService.sendStatusChangedEmail(
        event.email(),
        event.fullName(),
        event.applicationId(),
        event.applicationStatus(),
        event.managerComment()
    );
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(CreditApprovedEventDTO event) {
    sagaOrchestrator.createLoanAndSchedule(event);
  }
}
