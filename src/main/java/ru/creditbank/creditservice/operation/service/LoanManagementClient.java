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
        .headers(headers -> headers.setBearerAuth(request.accessToken()))
        .body(request)
        .retrieve()
        .body(CreateLoanResponseDTO.class);
  }

  public void createSchedule(UUID loanId, String accessToken) {
    loanManagementRestClient.post()
        .uri("/loan-management-service/internal/api/v1/loans/{loanId}/schedule", loanId)
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .toBodilessEntity();
  }
}
