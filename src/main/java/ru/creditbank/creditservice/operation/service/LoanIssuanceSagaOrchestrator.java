package ru.creditbank.creditservice.operation.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanRequestDTO;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanResponseDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanIssuanceSagaOrchestrator {

  private final LoanManagementClient loanManagementClient;
  private final CreditService creditService;

  public void createLoanAndSchedule(
      UUID applicationId,
      UUID userId,
      BigDecimal totalAmount,
      Integer termMonths,
      BigDecimal interestRate,
      String accessToken
  ) {
    CreateLoanResponseDTO loan =
        loanManagementClient.createLoan(
            new CreateLoanRequestDTO(
                applicationId,
                userId,
                totalAmount,
                termMonths,
                interestRate,
                accessToken
            )
        );

    log.info(
        "Loan was created, application id = {}, loan id = {}",
        applicationId,
        loan.loanId()
    );

    loanManagementClient.createSchedule(
        loan.loanId(),
        accessToken
    );

    log.info("Schedule for loan was created");

    creditService.markApproved(applicationId);

    log.info("Credit application = {} was approved", applicationId);
  }
}
