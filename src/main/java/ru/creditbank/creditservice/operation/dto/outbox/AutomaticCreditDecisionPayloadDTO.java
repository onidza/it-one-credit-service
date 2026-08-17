package ru.creditbank.creditservice.operation.dto.outbox;

import java.math.BigDecimal;
import java.util.UUID;

public record AutomaticCreditDecisionPayloadDTO(
    UUID userId,
    UUID applicationId,
    BigDecimal requestedAmount,
    String accessToken
) {

}
