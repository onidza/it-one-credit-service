package ru.creditbank.creditservice.operation.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.dto.publisher.CreditApprovedEventDTO;
import ru.creditbank.creditservice.operation.dto.publisher.CreditStatusChangedEventDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.exception.CreditApplicationNotFoundException;
import ru.creditbank.creditservice.operation.repository.CreditRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditDecisionTransactionService {

  private final CreditRepository creditRepository;
  private final ApplicationEventPublisher eventPublisher;

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

    application.setStatus(Status.APPROVED);
    application.setInterestRate(interestRate);

    log.info(
        "Credit application = {} automatically approved",
        applicationId
    );

    eventPublisher.publishEvent(
        new CreditStatusChangedEventDTO(
            application.getUserEmail(),
            application.getUserFullName(),
            applicationId,
            application.getStatus(),
            application.getManagerComment()
        )
    );

    eventPublisher.publishEvent(new CreditApprovedEventDTO(
            applicationId,
            application.getUserId(),
            application.getRequestedAmount(),
            application.getTermMonths(),
            application.getInterestRate(),
            accessToken
        )
    );
  }
}
