package ru.creditbank.creditservice.operation.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class CreditApplicationNotFoundException extends BusinessException {

  public CreditApplicationNotFoundException(UUID applicationId) {
    super(
        "Credit application with id '%s' not found".formatted(applicationId),
        HttpStatus.NOT_FOUND
    );
  }
}
