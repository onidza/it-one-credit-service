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

  public void createLoanAndSchedule(
      UUID creditApplicationId,
      UUID userId,
      BigDecimal totalAmount,
      Integer termMonths,
      BigDecimal interestRate,
      String accessToken
  ) {
    CreateLoanResponseDTO loan =
        loanManagementClient.createLoan(
            new CreateLoanRequestDTO(
                creditApplicationId,
                userId,
                totalAmount,
                termMonths,
                interestRate,
                accessToken
            )
        );

    loanManagementClient.createSchedule(
        loan.loanId(),
        accessToken
    );
  }

}
