package ru.creditbank.creditservice.operation.exception;

public class InvalidStatusUpdateException extends RuntimeException {

  public InvalidStatusUpdateException(String message) {
    super(message);
  }
}
