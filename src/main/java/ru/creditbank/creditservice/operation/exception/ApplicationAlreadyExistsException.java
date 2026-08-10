package ru.creditbank.creditservice.operation.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ApplicationAlreadyExistsException extends BusinessException {

  public ApplicationAlreadyExistsException(UUID userId) {
    super(
        "Application '%s' already exists".formatted(userId),
        HttpStatus.CONFLICT
    );
  }
}
