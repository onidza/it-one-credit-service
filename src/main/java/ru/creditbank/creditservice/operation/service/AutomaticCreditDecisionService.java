package ru.creditbank.creditservice.operation.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.creditbank.creditservice.operation.dto.internal.CreditProfileDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutomaticCreditDecisionService {

  private final CreditDecisionTransactionService transactionService;
  private final LoanManagementClient loanManagementClient;

  private static final BigDecimal MAX_TOTAL_CREDIT_EXPOSURE = new BigDecimal("1000000");
  private static final int NEED_COMPLETED_PAYMENTS_FOR_APPROVE = 10;
  private static final BigDecimal AUTOMATIC_INTEREST_RATE = new BigDecimal("15.00");

  public void processApplication(
      UUID userId,
      UUID applicationId,
      BigDecimal requestedAmount,
      String accessToken
  ) {
    CreditProfileDTO creditProfile =
        loanManagementClient.getCreditProfile(
            userId,
            accessToken
        );

    if (creditProfile.existingOverduePaymentsCount() > 0) {
      return;
    }

    if (creditProfile.completedPaymentsCount() < NEED_COMPLETED_PAYMENTS_FOR_APPROVE) {
      return;
    }

    BigDecimal totalExposure = creditProfile.totalRemainingAmount().add(requestedAmount);
    if (totalExposure.compareTo(MAX_TOTAL_CREDIT_EXPOSURE) > 0) {
      return;
    }

    transactionService.approveAutomatically(
        applicationId,
        AUTOMATIC_INTEREST_RATE,
        accessToken
    );
  }
}
