package ru.creditbank.creditservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.repository.CreditRepository;
import ru.creditbank.creditservice.operation.service.CreditCreateService;

import java.math.BigDecimal;
import java.util.UUID;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CreditServiceApplicationTests {

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
                userId
        );

        CreditEntity saved = creditRepository
                .findById(response.id())
                .orElseThrow();

        Assertions.assertEquals(userId, saved.getUserId());
        Assertions.assertEquals(Status.PENDING, saved.getStatus());
        Assertions.assertEquals(
                new BigDecimal("50000.00"),
                saved.getRequestedAmount()
        );
    }
}
