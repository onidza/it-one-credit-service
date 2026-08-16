package ru.creditbank.creditservice.operation.dto.internal;

import java.math.BigDecimal;

public record CreditProfileDTO(
    long existingOverduePaymentsCount,
    long completedPaymentsCount,
    BigDecimal totalRemainingAmount
) {

}
