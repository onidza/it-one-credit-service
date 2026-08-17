package ru.creditbank.creditservice.operation.controller;

import jakarta.validation.Valid;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.dto.details.CreditApplicationDetailsDTO;
import ru.creditbank.creditservice.operation.dto.status.StatusUpdateRequestDTO;
import ru.creditbank.creditservice.operation.service.CreditService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/credit-service/api/v1")
public class CreditApplicationController {

  private final CreditService creditService;

  @PostMapping("/credit")
  public ResponseEntity<CreditApplicationResponseDTO> createApplication(
      @Valid @RequestBody CreditApplicationRequestDTO request,
      @AuthenticationPrincipal Jwt accessToken
  ) {
    log.info("Received credit application request");

    UUID userId = UUID.fromString(
        Objects.requireNonNull(accessToken.getClaimAsString("user_id"))
    );

    String email = accessToken.getSubject();

    CreditApplicationResponseDTO response = creditService
        .createApplication(
            request,
            userId,
            email,
            accessToken.getTokenValue()
        );

    return ResponseEntity.ok(response);
  }

  @GetMapping("/credit/{id}")
  public ResponseEntity<CreditApplicationDetailsDTO> getApplicationDetails(
      @PathVariable UUID id
  ) {
    log.info("Received request for application details with application id = {}", id);
    CreditApplicationDetailsDTO details = creditService.getApplicationDetailsById(id);

    return ResponseEntity.ok(details);
  }

  @PatchMapping("/credit/{id}/status")
  public ResponseEntity<Void> changeApplicationStatus(
      @PathVariable UUID id,
      @RequestBody @Valid StatusUpdateRequestDTO request,
      @AuthenticationPrincipal Jwt jwt
  ) {
    log.info("Received request for application id = {}, setting a new status = {}",
        id, request.status());

    creditService.changeStatusManuallyByApplicationId(
        id,
        request,
        jwt.getTokenValue()
    );

    return ResponseEntity.ok().build();
  }
}