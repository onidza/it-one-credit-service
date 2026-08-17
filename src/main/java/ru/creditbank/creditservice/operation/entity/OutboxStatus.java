package ru.creditbank.creditservice.operation.entity;

public enum OutboxStatus {
  PENDING,
  RETRY,
  PROCESSED
}
