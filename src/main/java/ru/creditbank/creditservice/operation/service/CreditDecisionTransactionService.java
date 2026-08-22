package ru.creditbank.creditservice.operation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.dto.outbox.LoanIssuancePayloadDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventType;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.exception.CreditApplicationNotFoundException;
import ru.creditbank.creditservice.operation.exception.OutboxSerializationException;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.repository.OutboxRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditDecisionTransactionService {

  private final CreditRepository creditRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional(rollbackFor = Exception.class)
  public void approveAutomatically(
      UUID applicationId,
      BigDecimal interestRate,
      String accessToken
  ) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(CreditApplicationNotFoundException::new);

    if (application.getStatus() != Status.PENDING) {
      return;
    }

    application.activate(Status.APPROVING, interestRate);

    log.info(
        "Credit application = {} now has a new status: approving",
        applicationId
    );

    LoanIssuancePayloadDTO payload = new LoanIssuancePayloadDTO(
        application.getId(),
        application.getUserId(),
        application.getRequestedAmount(),
        application.getTermMonths(),
        application.getInterestRate(),
        accessToken
    );

    outboxRepository.save(
        new OutboxEventEntity(
            application.getId(),
            OutboxEventType.LOAN_ISSUANCE,
            serializePayload(payload),

            Objects.requireNonNull(
                MDC.get("traceId"),
                "traceId must be present in MDC when creating outbox event"
            )
        )
    );

    log.info(
        "Loan issuance outbox event created, applicationId = {}",
        applicationId
    );
  }

  private String serializePayload(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException ex) {
      throw new OutboxSerializationException(ex);
    }
  }
}