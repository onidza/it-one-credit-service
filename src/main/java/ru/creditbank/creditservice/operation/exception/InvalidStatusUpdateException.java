package ru.creditbank.creditservice.operation.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusUpdateException extends BusinessException {

  public InvalidStatusUpdateException() {
    super(
        "Interest rate must be greater than zero for approved application",
        HttpStatus.BAD_REQUEST
    );
  }
}
