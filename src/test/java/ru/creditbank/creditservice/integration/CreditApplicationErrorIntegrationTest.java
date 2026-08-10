package ru.creditbank.creditservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.repository.CreditRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreditApplicationErrorIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  CreditRepository repository;

  @Test
  void shouldReturnOkForValidApplication() throws Exception {
    UUID userId = UUID.randomUUID();

    String request = """
        {
            "fullName": "Иван Иванов Иванович",
            "requestedAmount": 50000,
            "termMonths": 12
        }
        """;

    mockMvc.perform(post("/credit-service/api/v1/credit")
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("test@example.com")
                    .claim(
                        "user_id",
                        userId.toString()
                    ))
                .authorities(
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    List<CreditEntity> operations = repository.findAll();

    assertThat(operations).hasSize(1);
    assertThat(operations.get(0).getUserId()).isEqualTo(userId);
    assertThat(operations.get(0).getRequestedAmount())
        .isEqualByComparingTo("50000.00");
  }

  @Test
  void shouldReturnBadRequestForInvalidApplication() throws Exception {
    String request = """
        {
            "fullName": "",
            "requestedAmount": -50000,
            "termMonths": 0
        }
        """;

    mockMvc.perform(post("/credit-service/api/v1/credit")
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("test@example.com")
                    .claim(
                        "user_id",
                        UUID.randomUUID().toString()
                    ))
                .authorities(
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnUnauthorizedWithoutJwt() throws Exception {
    String request = """
        {
            "fullName": "Иван Иванов Иванович",
            "requestedAmount": 50000,
            "termMonths": 12
        }
        """;

    mockMvc.perform(post("/credit-service/api/v1/credit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isUnauthorized());

    assertThat(repository.count()).isZero();
  }
}
