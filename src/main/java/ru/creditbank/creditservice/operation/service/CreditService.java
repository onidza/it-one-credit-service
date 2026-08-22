package ru.creditbank.creditservice.operation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.dto.details.CreditApplicationDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.LoanDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.UserInfoDTO;
import ru.creditbank.creditservice.operation.dto.outbox.AutomaticCreditDecisionPayloadDTO;
import ru.creditbank.creditservice.operation.dto.outbox.CreditStatusChangedPayloadDTO;
import ru.creditbank.creditservice.operation.dto.outbox.LoanIssuancePayloadDTO;
import ru.creditbank.creditservice.operation.dto.status.StatusUpdateRequestDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventType;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.exception.CreditApplicationNotFoundException;
import ru.creditbank.creditservice.operation.exception.DuplicateCreditApplicationException;
import ru.creditbank.creditservice.operation.exception.InvalidStatusUpdateException;
import ru.creditbank.creditservice.operation.exception.OutboxSerializationException;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.repository.OutboxRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

  private final CreditRepository creditRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional(rollbackFor = Exception.class)
  public CreditApplicationResponseDTO createApplication(
      CreditApplicationRequestDTO request,
      UUID userId,
      String email,
      String accessToken
  ) {
    if (creditRepository.existsByUserIdAndCreationDateAfter(
        userId,
        Instant.now().minusSeconds(10)
    )) {
      throw new DuplicateCreditApplicationException();
    }

    CreditEntity application = new CreditEntity(
        userId,
        request.fullName(),
        email,
        request.requestedAmount(),
        request.termMonths()
    );

    creditRepository.saveAndFlush(application);

    log.info(
        "Credit application created, applicationId = {}, userId = {}",
        application.getId(),
        userId
    );

    AutomaticCreditDecisionPayloadDTO payload =
        new AutomaticCreditDecisionPayloadDTO(
            userId,
            application.getId(),
            application.getRequestedAmount(),
            accessToken
        );

    outboxRepository.save(
        new OutboxEventEntity(
            application.getId(),
            OutboxEventType.AUTOMATIC_CREDIT_DECISION,
            serializePayload(payload),

            Objects.requireNonNull(
                MDC.get("traceId"),
                "traceId must be present in MDC when creating outbox event"
            )
        )
    );

    log.info(
        "Automatic credit decision outbox event created, applicationId = {}",
        application.getId()
    );

    return new CreditApplicationResponseDTO(
        application.getId(),
        application.getStatus(),
        application.getCreationDate()
    );
  }

  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public CreditApplicationDetailsDTO getApplicationDetailsById(UUID applicationId) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(CreditApplicationNotFoundException::new);

    log.info("Credit application details found, id = {}", applicationId);

    return new CreditApplicationDetailsDTO(
        applicationId,
        new UserInfoDTO(
            application.getUserId(),
            application.getUserFullName(),
            application.getUserEmail()
        ),
        new LoanDetailsDTO(
            application.getRequestedAmount(),
            application.getTermMonths(),
            application.getInterestRate()
        ),
        application.getStatus(),
        application.getCreationDate()
    );
  }

  @Transactional(rollbackFor = Exception.class)
  public void changeStatusManuallyByApplicationId(
      UUID applicationId,
      StatusUpdateRequestDTO request,
      String accessToken
  ) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(CreditApplicationNotFoundException::new);

    if (application.getStatus() != Status.PENDING) {
      throw new InvalidStatusUpdateException(application.getStatus().name());
    }

    application.changeStatus(
        Status.valueOf(request.status().name()),
        request.managerComment(),
        request.interestRate()
    );

    log.info(
        "Credit application = {} now has a new status: {}",
        application.getStatus().name(),
        applicationId
    );

    if (application.getStatus() == Status.APPROVING) {
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
    }

    log.info(
        "Loan issuance outbox event created, applicationId = {}",
        applicationId
    );
  }

  @Transactional(rollbackFor = Exception.class)
  public void markApproved(UUID applicationId) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(CreditApplicationNotFoundException::new);

    if (application.getStatus() == Status.APPROVED) {
      return;
    }

    application.setStatus(Status.APPROVED);

    log.info(
        "Credit application marked as APPROVED, applicationId = {}",
        applicationId
    );

    CreditStatusChangedPayloadDTO payload =
        new CreditStatusChangedPayloadDTO(
            application.getUserEmail(),
            application.getUserFullName(),
            applicationId,
            application.getStatus(),
            application.getManagerComment()
        );

    outboxRepository.save(
        new OutboxEventEntity(
            applicationId,
            OutboxEventType.CREDIT_STATUS_CHANGED,
            serializePayload(payload),

            Objects.requireNonNull(
                MDC.get("traceId"),
                "traceId must be present in MDC when creating outbox event"
            )
        )
    );

    log.info(
        "Credit status changed outbox event created, applicationId = {}",
        application.getId()
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