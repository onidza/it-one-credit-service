package ru.creditbank.creditservice.operation.exception;

public class OutboxSerializationException extends RuntimeException {

  public OutboxSerializationException(Throwable cause) {
    super("Failed to serialize outbox event payload", cause);
  }
}
