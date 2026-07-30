package ru.creditbank.creditservice.operation.dto.application;

import ru.creditbank.creditservice.operation.entity.Status;

import java.time.Instant;
import java.util.UUID;

public record CreditApplicationResponseDTO(
    UUID id,
    Status status,
    Instant createdAt
) {}
