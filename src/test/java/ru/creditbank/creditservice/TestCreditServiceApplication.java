package ru.creditbank.creditservice;

import org.springframework.boot.SpringApplication;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;

public class TestCreditServiceApplication {

  public static void main(String[] args) {
    SpringApplication.from(CreditServiceApplication::main).with(TestcontainersConfiguration.class)
        .run(args);
  }

}
