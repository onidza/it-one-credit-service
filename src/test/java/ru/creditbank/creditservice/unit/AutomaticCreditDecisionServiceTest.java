package ru.creditbank.creditservice.unit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.creditbank.creditservice.operation.dto.internal.CreditProfileDTO;
import ru.creditbank.creditservice.operation.service.AutomaticCreditDecisionService;
import ru.creditbank.creditservice.operation.service.CreditDecisionTransactionService;
import ru.creditbank.creditservice.operation.service.LoanManagementClient;

@ExtendWith(MockitoExtension.class)
class AutomaticCreditDecisionServiceTest {

  private static final String ACCESS_TOKEN = "test-access-token";

  @Mock
  private LoanManagementClient loanManagementClient;

  @Mock
  private CreditDecisionTransactionService transactionService;

  @InjectMocks
  private AutomaticCreditDecisionService decisionService;

  @Test
  void shouldNotApproveWhenOverduePaymentsExist() {
    UUID userId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    CreditProfileDTO profile = new CreditProfileDTO(
        1,
        10,
        new BigDecimal("100000")
    );

    when(loanManagementClient.getCreditProfile(userId, ACCESS_TOKEN))
        .thenReturn(profile);

    decisionService.processApplication(
        applicationId,
        userId,
        new BigDecimal("100000"),
        ACCESS_TOKEN
    );

    verify(transactionService, never())
        .approveAutomatically(
            applicationId,
            new BigDecimal("15.00"),
            ACCESS_TOKEN
        );
  }

  @Test
  void shouldNotApproveWhenCompletedPaymentsAreLessThanRequired() {
    UUID userId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    CreditProfileDTO profile = new CreditProfileDTO(
        0,
        9,
        new BigDecimal("100000")
    );

    when(loanManagementClient.getCreditProfile(userId, ACCESS_TOKEN))
        .thenReturn(profile);

    decisionService.processApplication(
        applicationId,
        userId,
        new BigDecimal("100000"),
        ACCESS_TOKEN
    );

    verify(transactionService, never())
        .approveAutomatically(
            applicationId,
            new BigDecimal("15.00"),
            ACCESS_TOKEN
        );
  }

  @Test
  void shouldNotApproveWhenTotalCreditExposureExceedsLimit() {
    UUID userId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    CreditProfileDTO profile = new CreditProfileDTO(
        0,
        10,
        new BigDecimal("900000")
    );

    when(loanManagementClient.getCreditProfile(userId, ACCESS_TOKEN))
        .thenReturn(profile);

    decisionService.processApplication(
        applicationId,
        userId,
        new BigDecimal("100001"),
        ACCESS_TOKEN
    );

    verify(transactionService, never())
        .approveAutomatically(
            applicationId,
            new BigDecimal("15.00"),
            ACCESS_TOKEN
        );
  }

  @Test
  void shouldApproveWhenAllBusinessRulesAreSatisfied() {
    UUID userId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    CreditProfileDTO profile = new CreditProfileDTO(
        0,
        10,
        new BigDecimal("800000")
    );

    when(loanManagementClient.getCreditProfile(userId, ACCESS_TOKEN))
        .thenReturn(profile);

    decisionService.processApplication(
        applicationId,
        userId,
        new BigDecimal("200000"),
        ACCESS_TOKEN
    );

    verify(transactionService)
        .approveAutomatically(
            applicationId,
            new BigDecimal("15.00"),
            ACCESS_TOKEN
        );
  }
}