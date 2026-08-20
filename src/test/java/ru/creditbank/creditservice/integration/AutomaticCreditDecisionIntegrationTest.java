package ru.creditbank.creditservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventType;
import ru.creditbank.creditservice.operation.entity.OutboxStatus;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.repository.OutboxRepository;
import ru.creditbank.creditservice.operation.service.CreditDecisionTransactionService;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AutomaticCreditDecisionIntegrationTest {

  @Autowired
  private CreditRepository creditRepository;

  @Autowired
  private OutboxRepository outboxRepository;

  @Autowired
  private CreditDecisionTransactionService transactionService;

  @PersistenceContext
  private EntityManager entityManager;

  @Test
  void shouldSetApprovingStatusAndCreateLoanIssuanceOutbox() {
    UUID userId = UUID.randomUUID();

    CreditEntity application = new CreditEntity(
        userId,
        "Ivan Ivanov",
        "ivan@test.com",
        new BigDecimal("100000"),
        12
    );

    application = creditRepository.save(application);

    UUID applicationId = application.getId();

    transactionService.approveAutomatically(
        applicationId,
        new BigDecimal("15.00"),
        "test-access-token"
    );

    entityManager.flush();
    entityManager.clear();

    CreditEntity updatedApplication =
        creditRepository.findById(applicationId)
            .orElseThrow();

    assertThat(updatedApplication.getStatus())
        .isEqualTo(Status.APPROVING);

    OutboxEventEntity outboxEvent =
        outboxRepository
            .findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(
                applicationId,
                OutboxEventType.LOAN_ISSUANCE
            )
            .orElseThrow();

    assertThat(outboxEvent.getAggregateId())
        .isEqualTo(applicationId);

    assertThat(outboxEvent.getEventType())
        .isEqualTo(OutboxEventType.LOAN_ISSUANCE);

    assertThat(outboxEvent.getStatus())
        .isEqualTo(OutboxStatus.PENDING);
  }
}