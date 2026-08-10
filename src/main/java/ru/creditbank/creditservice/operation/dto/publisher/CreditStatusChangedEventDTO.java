package ru.creditbank.creditservice.operation.dto.publisher;

import java.util.UUID;
import ru.creditbank.creditservice.operation.entity.Status;

public record CreditStatusChangedEventDTO(
    String email,
    String fullName,
    UUID applicationId,
    Status applicationStatus,
    String managerComment
) {

}
