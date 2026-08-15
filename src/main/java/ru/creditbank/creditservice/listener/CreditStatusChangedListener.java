package ru.creditbank.creditservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CreditStatusChangedListener {

  private final LoanIssuanceSagaOrchestrator sagaOrchestrator;
  private final NotificationService notificationService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(CreditStatusChangedEventDTO event) {
    try {
      notificationService.sendStatusChangedEmail(
          event.email(),
          event.fullName(),
          event.applicationId(),
          event.applicationStatus(),
          event.managerComment()
      );
    } catch (Exception ex) {
      log.error(
          "Failed to send status changed email, applicationId = {}",
          event.applicationId(),
          ex
      );
    }

    //task 7: retry
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(CreditApprovedEventDTO event) {
    try {
      sagaOrchestrator.createLoanAndSchedule(event);
    } catch (Exception ex) {
      log.error(
          "Loan issuance saga failed, applicationId = {}",
          event.creditApplicationId(),
          ex
      );

      // Task 7: retry, compensation
    }
  }
}
