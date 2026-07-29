package ru.creditbank.creditservice;

import org.springframework.boot.SpringApplication;

public class TestCreditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(CreditServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
