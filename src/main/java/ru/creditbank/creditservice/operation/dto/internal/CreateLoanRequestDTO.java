package ru.creditbank.creditservice.operation.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanRequestDTO(
    UUID creditApplicationId,
    UUID userId,
    BigDecimal totalAmount,
    Integer termMonths,
    BigDecimal interestRate,
    String accessToken
) {

}
