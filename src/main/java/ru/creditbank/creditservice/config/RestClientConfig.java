package ru.creditbank.creditservice.config;

import java.time.Duration;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${rest.loan-service.url}")
  private String url;

  @Value("${rest.loan-service.connect-timeout}")
  private Duration connectionTimeout;

  @Value("${rest.loan-service.read-timeout}")
  private Duration readTimeout;

  @Bean
  public RestClient loanManagementRestClient(RestClient.Builder builder) {
    ClientHttpRequestFactorySettings settings =
        ClientHttpRequestFactorySettings.defaults()
            .withTimeouts(
                connectionTimeout,
                readTimeout
            );

    ClientHttpRequestFactory requestFactory =
        ClientHttpRequestFactoryBuilder
            .detect()
            .build(settings);

    return builder
        .baseUrl(url)
        .requestFactory(requestFactory)
        .build();
  }

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder()
        .requestInterceptor((request, body, execution) -> {

          String traceId = MDC.get("traceId");

          if (traceId != null) {
            request.getHeaders()
                .set("X-Trace-Id", traceId);
          }

          return execution.execute(request, body);
        });
  }
}