package ru.creditbank.creditservice.operation.exception.rest;

import org.springframework.http.HttpStatus;

public class DuplicateCreditApplicationException extends BusinessException {

  public DuplicateCreditApplicationException() {
    super(
        "Credit application was submitted too recently",
        HttpStatus.CONFLICT
    );
  }
}
