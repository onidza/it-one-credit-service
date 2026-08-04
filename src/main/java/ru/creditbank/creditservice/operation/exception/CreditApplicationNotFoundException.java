package ru.creditbank.creditservice.operation.exception;

import java.util.UUID;

public class CreditApplicationNotFoundException extends RuntimeException {

  public CreditApplicationNotFoundException(UUID applicationId) {
    super("Credit application with id '%s' not found".formatted(applicationId));
  }
}
