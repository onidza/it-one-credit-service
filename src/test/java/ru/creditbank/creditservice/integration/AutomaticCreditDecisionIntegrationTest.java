package ru.creditbank.creditservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.service.CreditDecisionTransactionService;
import ru.creditbank.creditservice.operation.service.LoanIssuanceSagaOrchestrator;
import ru.creditbank.creditservice.operation.service.NotificationService;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AutomaticCreditDecisionIntegrationTest {

  @Autowired
  private CreditRepository creditRepository;

  @Autowired
  private CreditDecisionTransactionService transactionService;

  @MockitoBean
  private NotificationService notificationService;

  @MockitoBean
  private LoanIssuanceSagaOrchestrator sagaOrchestrator;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  @Test
  void shouldUpdateApplicationStatusInDatabaseAfterAutomaticApproval() {
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

    CreditEntity updated = creditRepository.findById(applicationId)
        .orElseThrow();
    assertThat(updated.getStatus())
        .isEqualTo(Status.APPROVED);
  }

  @Test
  void shouldNotifyUserAfterAutomaticApproval() {
    UUID userId = UUID.randomUUID();

    String email = "ivan@test.com";
    String fullName = "Ivan Ivanov";

    CreditEntity application = new CreditEntity(
        userId,
        fullName,
        email,
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

    verify(notificationService, timeout(3000))
        .sendStatusChangedEmail(
            eq(email),
            eq(fullName),
            eq(applicationId),
            eq(Status.APPROVED),
            isNull()
        );
  }
}
