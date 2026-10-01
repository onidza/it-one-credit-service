package ru.creditbank.creditservice.operation.exception.rest;

import org.springframework.http.HttpStatus;

public class InvalidStatusUpdateApprovedException extends BusinessException {

  public InvalidStatusUpdateApprovedException() {
    super(
        "Interest rate must be greater than zero for approved application",
        HttpStatus.BAD_REQUEST
    );
  }
}
