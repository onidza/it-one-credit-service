package ru.creditbank.creditservice.integration;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.config.TestcontainersConfiguration;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.service.CreditCreateService;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class CreditApplicationServiceIntegrationTest {

  @Autowired
  private CreditCreateService creditCreateService;

  @Autowired
  private CreditRepository creditRepository;

  @Test
  void shouldSaveCreditApplication() {
    UUID userId = UUID.randomUUID();

    CreditApplicationRequestDTO request =
        new CreditApplicationRequestDTO(
            "Иванов Иван Иванович",
            new BigDecimal("50000.00"),
            12
        );

    CreditApplicationResponseDTO response =
        creditCreateService.createApplication(
            request,
            userId,
            "example@gmail.com",
            ""
        );

    CreditEntity saved = creditRepository
        .findById(response.id())
        .orElseThrow();

    Assertions.assertEquals(userId, saved.getUserId());
    Assertions.assertEquals("example@gmail.com", saved.getUserEmail());
    Assertions.assertEquals(Status.PENDING, saved.getStatus());
    Assertions.assertEquals(
        new BigDecimal("50000.00"),
        saved.getRequestedAmount()
    );
  }
}
