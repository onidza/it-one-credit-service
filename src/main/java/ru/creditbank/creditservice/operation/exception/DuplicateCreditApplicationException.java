package ru.creditbank.creditservice.operation.exception;

public class DuplicateCreditApplicationException extends RuntimeException {

  public DuplicateCreditApplicationException(String message) {
    super(message);
  }
}
