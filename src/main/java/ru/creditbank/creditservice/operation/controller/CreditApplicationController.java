package ru.creditbank.creditservice.operation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.creditbank.creditservice.operation.dto.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.service.CreditCreateService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/credit-service/api/v1")
public class CreditApplicationController {

    private final CreditCreateService creditCreateService;

    @PostMapping("/credit")
    public ResponseEntity<CreditApplicationResponseDTO> createApplication(
            @Valid @RequestBody CreditApplicationRequestDTO request,
            @AuthenticationPrincipal Jwt accessToken
    ) {
        log.info("Received credit application request");

        CreditApplicationResponseDTO response = creditCreateService
                .createApplication(
                        request,
                        accessToken.getClaim("user_id"));

        return ResponseEntity.ok(response);
    }
}
