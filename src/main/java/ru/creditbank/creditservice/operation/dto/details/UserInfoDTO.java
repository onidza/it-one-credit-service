package ru.creditbank.creditservice.operation.dto.details;

import java.util.UUID;

public record UserInfoDTO(
    UUID userId,
    String fullName,
    String email
) {

}
