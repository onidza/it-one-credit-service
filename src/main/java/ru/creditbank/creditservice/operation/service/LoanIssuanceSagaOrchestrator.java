package ru.creditbank.creditservice.operation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanRequestDTO;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanResponseDTO;
import ru.creditbank.creditservice.operation.dto.publisher.CreditApprovedEventDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanIssuanceSagaOrchestrator {

  private final LoanManagementClient loanManagementClient;

  public void createLoanAndSchedule(CreditApprovedEventDTO event) {

    CreateLoanResponseDTO loan =
        loanManagementClient.createLoan(
            new CreateLoanRequestDTO(
                event.creditApplicationId(),
                event.userId(),
                event.totalAmount(),
                event.termMonths(),
                event.interestRate()
            )
        );

    loanManagementClient.createSchedule(
        loan.loanId()
    );
  }

}
