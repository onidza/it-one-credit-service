package ru.creditbank.creditservice.operation.dto.publisher;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditApprovedEventDTO(
    UUID creditApplicationId,
    UUID userId,
    BigDecimal totalAmount,
    Integer termMonths,
    BigDecimal interestRate
) {

}
