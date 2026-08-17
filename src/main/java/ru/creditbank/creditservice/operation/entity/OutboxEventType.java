package ru.creditbank.creditservice.operation.entity;

public enum OutboxEventType {
  AUTOMATIC_CREDIT_DECISION,
  LOAN_ISSUANCE,
  CREDIT_STATUS_CHANGED
}
