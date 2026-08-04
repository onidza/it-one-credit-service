package ru.creditbank.creditservice.operation.dto.details;

import java.time.Instant;
import java.util.UUID;
import ru.creditbank.creditservice.operation.entity.Status;

public record CreditApplicationDetailsDTO(
    UUID id,
    UserInfoDTO userInfo,
    LoanDetailsDTO loanDetails,
    Status status,
    Instant createdAt
) {

}
