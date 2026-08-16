package ru.creditbank.creditservice.operation.dto.publisher;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditApplicationCreatedEventDTO(
    UUID userId,
    UUID applicationId,
    BigDecimal requestedAmount,
    String accessToken
) {

}
