package ru.creditbank.creditservice.operation.dto.outbox;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanIssuancePayloadDTO(
    UUID creditApplicationId,
    UUID userId,
    BigDecimal totalAmount,
    Integer termMonths,
    BigDecimal interestRate,
    String accessToken
) {

}
