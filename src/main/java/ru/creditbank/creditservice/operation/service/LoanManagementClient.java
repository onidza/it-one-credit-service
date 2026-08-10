package ru.creditbank.creditservice.operation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanRequestDTO;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanResponseDTO;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoanManagementClient {

  private final RestClient loanManagementRestClient;

  public CreateLoanResponseDTO createLoan(CreateLoanRequestDTO request) {
    return loanManagementRestClient.post()
        .uri("/loan-management-service/internal/api/v1/loans")
        .body(request)
        .retrieve()
        .body(CreateLoanResponseDTO.class);
  }

  public void createSchedule(UUID loanId) {
    loanManagementRestClient.post()
        .uri("/loan-management-service/internal/api/v1/loans", loanId)
        .retrieve()
        .toBodilessEntity();
  }
}
