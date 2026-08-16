package ru.creditbank.creditservice.operation.exception;

import org.springframework.http.HttpStatus;

public class CreditApplicationNotFoundException extends BusinessException {

  public CreditApplicationNotFoundException() {
    super(
        "Credit application not found",
        HttpStatus.NOT_FOUND
    );
  }
}
