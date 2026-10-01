package ru.creditbank.creditservice.operation.exception.rest;

import org.springframework.http.HttpStatus;

public class CreditApplicationNotFoundException extends BusinessException {

  public CreditApplicationNotFoundException() {
    super(
        "Credit application not found",
        HttpStatus.NOT_FOUND
    );
  }
}
