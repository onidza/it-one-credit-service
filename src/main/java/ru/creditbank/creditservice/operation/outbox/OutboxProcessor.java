package ru.creditbank.creditservice.operation.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.creditbank.creditservice.operation.dto.outbox.AutomaticCreditDecisionPayloadDTO;
import ru.creditbank.creditservice.operation.dto.outbox.CreditStatusChangedPayloadDTO;
import ru.creditbank.creditservice.operation.dto.outbox.LoanIssuancePayloadDTO;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.service.AutomaticCreditDecisionService;
import ru.creditbank.creditservice.operation.service.LoanIssuanceSagaOrchestrator;
import ru.creditbank.creditservice.operation.service.NotificationService;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessor {

  private final ObjectMapper objectMapper;
  private final OutboxService outboxService;

  private final LoanIssuanceSagaOrchestrator sagaOrchestrator;
  private final AutomaticCreditDecisionService decisionService;
  private final NotificationService notificationService;

  public void process(OutboxEventEntity event) {
    try {
      switch (event.getEventType()) {
        case AUTOMATIC_CREDIT_DECISION -> processAutomaticCreditDecision(event);
        case LOAN_ISSUANCE -> processLoanIssuance(event);
        case CREDIT_STATUS_CHANGED -> processStatusChangeNotification(event);
      }

      outboxService.markProcessed(event.getId());

      log.info(
          "Outbox event was marked processed, type = {}, event id = {}",
          event.getEventType().name(),
          event.getAggregateId()
      );

    } catch (Exception ex) {
      log.error(
          "Outbox event processing failed, eventId = {}, eventType = {}, aggregateId = {}",
          event.getId(),
          event.getEventType(),
          event.getAggregateId(),
          ex
      );

      outboxService.markRetry(
          event.getId(),
          ex.getMessage()
      );
    }
  }

  private void processAutomaticCreditDecision(
      OutboxEventEntity event
  ) throws JsonProcessingException {

    AutomaticCreditDecisionPayloadDTO payload =
        objectMapper.readValue(
            event.getPayload(),
            AutomaticCreditDecisionPayloadDTO.class
        );

    decisionService.processApplication(
        event.getAggregateId(),
        payload.userId(),
        payload.requestedAmount(),
        payload.accessToken()
    );
  }

  private void processLoanIssuance(
      OutboxEventEntity event
  ) throws JsonProcessingException {

    LoanIssuancePayloadDTO payload =
        objectMapper.readValue(
            event.getPayload(),
            LoanIssuancePayloadDTO.class
        );

    sagaOrchestrator.createLoanAndSchedule(
        event.getAggregateId(),
        payload.userId(),
        payload.totalAmount(),
        payload.termMonths(),
        payload.interestRate(),
        payload.accessToken()
    );
  }

  public void processStatusChangeNotification(
      OutboxEventEntity event
  ) throws JsonProcessingException {
    CreditStatusChangedPayloadDTO payload =
        objectMapper.readValue(
            event.getPayload(),
            CreditStatusChangedPayloadDTO.class
        );

    notificationService.sendStatusChangedEmail(
        payload.email(),
        payload.fullName(),
        payload.applicationId(),
        payload.applicationStatus(),
        payload.managerComment()
    );
  }
}

