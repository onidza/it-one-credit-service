package ru.creditbank.creditservice.operation.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.Customizer;
import ru.creditbank.creditservice.operation.config.filter.grpc.TraceIdServerInterceptor;

@Configuration
public class SecurityConfigGrpc {

  @Bean
  @GlobalServerInterceptor
  public AuthenticationProcessInterceptor grpcAuthenticationInterceptor(
      GrpcSecurity grpcSecurity
  ) throws Exception {

    return grpcSecurity
        .authorizeRequests(requests ->
            requests
                .allRequests()
                .authenticated()
        )
        .oauth2ResourceServer(oauth2 ->
            oauth2.jwt(Customizer.withDefaults())
        )
        .build();
  }

  @Bean
  @GlobalServerInterceptor
  public TraceIdServerInterceptor traceIdServerInterceptor() {
    return new TraceIdServerInterceptor();
  }
}
