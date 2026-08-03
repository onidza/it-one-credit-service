package ru.creditbank.creditservice.operation.dto.application;

import java.time.Instant;
import java.util.UUID;
import ru.creditbank.creditservice.operation.entity.Status;

public record CreditApplicationResponseDTO(
    UUID id,
    Status status,
    Instant createdAt
) {

}
