package ru.creditbank.creditservice.operation.exception.rest;

import org.springframework.http.HttpStatus;

public class InvalidStatusUpdateException extends BusinessException {

  public InvalidStatusUpdateException(String currentStatus) {
    super(
        "Status already has been changed on '%s".formatted(currentStatus),
        HttpStatus.BAD_REQUEST
    );
  }
}
