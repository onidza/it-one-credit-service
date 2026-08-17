package ru.creditbank.creditservice.operation.dto.outbox;

import java.util.UUID;
import ru.creditbank.creditservice.operation.entity.Status;

public record CreditStatusChangedPayloadDTO(
    String email,
    String fullName,
    UUID applicationId,
    Status applicationStatus,
    String managerComment
) {

}
