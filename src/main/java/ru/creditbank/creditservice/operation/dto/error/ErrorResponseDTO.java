package ru.creditbank.creditservice.operation.dto.error;

public record ErrorResponseDTO(
        int status,
        String message
) {}