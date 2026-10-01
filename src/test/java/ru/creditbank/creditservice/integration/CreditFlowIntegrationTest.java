package ru.creditbank.creditservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxEventType;
import ru.creditbank.creditservice.operation.entity.OutboxStatus;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.outbox.OutboxProcessor;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.repository.OutboxRepository;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "resilience4j.circuitbreaker.instances.loanManagement.minimum-number-of-calls=100",
    "resilience4j.retry.instances.loanManagement.wait-duration=10ms"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CreditFlowIntegrationTest {

  private static final String TOKEN = "test-token";
  private static final String EMAIL = "user@test.ru";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CreditRepository creditRepository;

  @Autowired
  private OutboxRepository outboxRepository;

  @Autowired
  private OutboxProcessor outboxProcessor;

  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .http2PlainDisabled(true)
          )
          .build();

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "rest.loan-service.url",
        wireMock::baseUrl
    );
  }

  @Test
  void shouldRecoverManualApprovalAfterScheduleFailure() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID loanId = UUID.randomUUID();

    CreditEntity application = new CreditEntity(
        userId,
        "Ivan Ivanov",
        EMAIL,
        new BigDecimal("100000.00"),
        12
    );

    creditRepository.save(application);

    UUID applicationId = application.getId();

    wireMock.stubFor(
        WireMock.post(urlEqualTo(
                "/loan-management-service/internal/api/v1/loans"
            ))
            .willReturn(
                okJson("""
                    {
                      "loanId": "%s",
                      "status": "CREATING"
                    }
                    """.formatted(loanId))
            )
    );

    stubScheduleFailsThreeTimesThenSucceeds(loanId);

    mockMvc.perform(
            patch(
                "/credit-service/api/v1/credit/{id}/status",
                applicationId
            )
                .with(jwt()
                    .jwt(jwt -> jwt
                        .tokenValue(TOKEN)
                        .claim(
                            "user_id",
                            userId.toString()
                        )
                    )
                    .authorities(
                        new SimpleGrantedAuthority(
                            "ROLE_CREDIT_MANAGER"
                        )
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "APPROVING",
                      "managerComment": "Approving",
                      "interestRate": 15.00
                    }
                    """)
        )
        .andExpect(status().isOk());

    assertThat(
        creditRepository.findById(applicationId)
            .orElseThrow()
            .getStatus()
    ).isEqualTo(Status.APPROVING);

    OutboxEventEntity loanIssuanceEvent =
        outboxRepository
            .findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(
                applicationId,
                OutboxEventType.LOAN_ISSUANCE)
            .orElseThrow();

    outboxProcessor.process(loanIssuanceEvent);

    assertThat(
        outboxRepository.findById(loanIssuanceEvent.getId())
            .orElseThrow()
            .getStatus()
    ).isEqualTo(OutboxStatus.RETRY);

    assertThat(
        creditRepository.findById(applicationId)
            .orElseThrow()
            .getStatus()
    ).isEqualTo(Status.APPROVING);

    OutboxEventEntity retryEvent =
        outboxRepository.findById(loanIssuanceEvent.getId())
            .orElseThrow();

    outboxProcessor.process(retryEvent);

    assertThat(
        creditRepository.findById(applicationId)
            .orElseThrow()
            .getStatus()
    ).isEqualTo(Status.APPROVED);

    assertThat(
        outboxRepository.findById(loanIssuanceEvent.getId())
            .orElseThrow()
            .getStatus()
    ).isEqualTo(OutboxStatus.PROCESSED);

    wireMock.verify(
        2,
        postRequestedFor(
            urlEqualTo(
                "/loan-management-service/internal/api/v1/loans"
            )
        )
    );

    wireMock.verify(
        4,
        postRequestedFor(
            urlEqualTo(
                "/loan-management-service/internal/api/v1/loans/%s/schedule"
                    .formatted(loanId)
            )
        )
    );
  }

  @Test
  void shouldAutomaticallyApproveAndIssueLoan() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID loanId = UUID.randomUUID();

    wireMock.stubFor(
        WireMock.get(urlEqualTo(
                "/loan-management-service/internal/api/v1/credit-profile/user/"
                    + userId
            ))
            .willReturn(
                okJson("""
                    {
                      "existingOverduePaymentsCount": 0,
                      "completedPaymentsCount": 15,
                      "totalRemainingAmount": 100000.00
                    }
                    """)
            )
    );

    wireMock.stubFor(
        WireMock.post(urlEqualTo(
                "/loan-management-service/internal/api/v1/loans"
            ))
            .willReturn(
                okJson("""
                    {
                      "loanId": "%s",
                      "status": "CREATING"
                    }
                    """.formatted(loanId))
            )
    );

    wireMock.stubFor(
        WireMock.post(urlEqualTo(
                "/loan-management-service/internal/api/v1/loans/%s/schedule"
                    .formatted(loanId)
            ))
            .willReturn(aResponse().withStatus(200))
    );

    MvcResult result =
        mockMvc.perform(
                post("/credit-service/api/v1/credit")
                    .with(
                        jwt()
                            .jwt(jwt -> jwt
                                .tokenValue(TOKEN)
                                .subject(EMAIL)
                                .claim("user_id", userId.toString())
                            )
                            .authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                            )
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "fullName": "Ivan Ivanov",
                          "requestedAmount": 100000.00,
                          "termMonths": 12
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andReturn();

    UUID applicationId =
        UUID.fromString(
            objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText()
        );

    OutboxEventEntity decisionEvent =
        outboxRepository.findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(
                applicationId,
                OutboxEventType.AUTOMATIC_CREDIT_DECISION
            )
            .orElseThrow();

    outboxProcessor.process(decisionEvent);

    assertThat(
        creditRepository.findById(applicationId)
            .orElseThrow()
            .getStatus()
    ).isEqualTo(Status.APPROVING);

    OutboxEventEntity loanIssuanceEvent =
        outboxRepository.findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(
                applicationId,
                OutboxEventType.LOAN_ISSUANCE
            )
            .orElseThrow();

    outboxProcessor.process(loanIssuanceEvent);

    assertThat(
        creditRepository.findById(applicationId)
            .orElseThrow()
            .getStatus()
    ).isEqualTo(Status.APPROVED);

    assertThat(
        outboxRepository.findById(decisionEvent.getId())
            .orElseThrow()
            .getStatus()
    ).isEqualTo(OutboxStatus.PROCESSED);

    assertThat(
        outboxRepository.findById(loanIssuanceEvent.getId())
            .orElseThrow()
            .getStatus()
    ).isEqualTo(OutboxStatus.PROCESSED);

    wireMock.verify(
        1,
        getRequestedFor(
            urlEqualTo(
                "/loan-management-service/internal/api/v1/credit-profile/user/"
                    + userId
            )
        )
    );

    wireMock.verify(
        1,
        postRequestedFor(
            urlEqualTo(
                "/loan-management-service/internal/api/v1/loans"
            )
        )
    );

    wireMock.verify(
        1,
        postRequestedFor(
            urlEqualTo(
                "/loan-management-service/internal/api/v1/loans/%s/schedule"
                    .formatted(loanId)
            )
        )
    );
  }

  private void stubScheduleFailsThreeTimesThenSucceeds(UUID loanId) {
    String url =
        "/loan-management-service/internal/api/v1/loans/%s/schedule"
            .formatted(loanId);

    wireMock.stubFor(
        WireMock.post(urlEqualTo(url))
            .inScenario("schedule-creation")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serverError())
            .willSetStateTo("second-attempt")
    );

    wireMock.stubFor(
        WireMock.post(urlEqualTo(url))
            .inScenario("schedule-creation")
            .whenScenarioStateIs("second-attempt")
            .willReturn(serverError())
            .willSetStateTo("third-attempt")
    );

    wireMock.stubFor(
        WireMock.post(urlEqualTo(url))
            .inScenario("schedule-creation")
            .whenScenarioStateIs("third-attempt")
            .willReturn(serverError())
            .willSetStateTo("recovered")
    );

    wireMock.stubFor(
        WireMock.post(urlEqualTo(url))
            .inScenario("schedule-creation")
            .whenScenarioStateIs("recovered")
            .willReturn(aResponse().withStatus(200))
    );
  }
}
