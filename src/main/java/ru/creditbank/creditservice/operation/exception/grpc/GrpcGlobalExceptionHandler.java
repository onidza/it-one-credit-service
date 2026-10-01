package ru.creditbank.creditservice.operation.exception.grpc;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

/**
 * Using for mapping business exceptions -> gRPC exceptions
 */
@Slf4j
@Configuration
public class GrpcGlobalExceptionHandler {

  @Bean
  public GrpcExceptionHandler grpcExceptionHandler() {
    return throwable -> {

      log.error(
          "Unhandled gRPC exception",
          throwable
      );

      return Status.INTERNAL
          .withDescription("Internal server error")
          .asException();
    };
  }
}
