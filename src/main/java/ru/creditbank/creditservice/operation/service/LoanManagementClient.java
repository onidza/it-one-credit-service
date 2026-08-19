package ru.creditbank.creditservice.operation.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanRequestDTO;
import ru.creditbank.creditservice.operation.dto.internal.CreateLoanResponseDTO;
import ru.creditbank.creditservice.operation.dto.internal.CreditProfileDTO;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoanManagementClient {

  private final RestClient loanManagementRestClient;

  @CircuitBreaker(name = "loanManagement")
  @Retry(name = "loanManagement")
  public CreateLoanResponseDTO createLoan(CreateLoanRequestDTO request) {
    return Objects.requireNonNull(
        loanManagementRestClient.post()
            .uri("/loan-management-service/internal/api/v1/loans")
            .headers(headers -> headers.setBearerAuth(request.accessToken()))
            .body(request)
            .retrieve()
            .body(CreateLoanResponseDTO.class),
        "Loan management service returned empty response body"
    );
  }

  @CircuitBreaker(name = "loanManagement")
  @Retry(name = "loanManagement")
  public void createSchedule(UUID loanId, String accessToken) {
    loanManagementRestClient.post()
        .uri("/loan-management-service/internal/api/v1/loans/{loanId}/schedule", loanId)
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .toBodilessEntity();
  }

  @CircuitBreaker(name = "loanManagement")
  @Retry(name = "loanManagement")
  public CreditProfileDTO getCreditProfile(
      UUID userId,
      String accessToken
  ) {
    return Objects.requireNonNull(
        loanManagementRestClient
            .get()
            .uri(
                "/loan-management-service/internal/api/v1/credit-profile/user/{userId}",
                userId
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(CreditProfileDTO.class),
        "Loan management service returned empty response body"
    );
  }
}
