package ru.creditbank.creditservice.unit;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.creditbank.creditservice.operation.dto.status.StatusUpdateRequestDTO;
import ru.creditbank.creditservice.operation.dto.status.UpdateStatusDTO;

class CreditApplicationStatusChangeRequestDTOValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation
        .buildDefaultValidatorFactory()
        .getValidator();
  }

  @Test
  void shouldPassValidationForValidInterestRate() {
    StatusUpdateRequestDTO request = new StatusUpdateRequestDTO(
        UpdateStatusDTO.APPROVED,
        "Заявка одобрена",
        new BigDecimal("15.30")
    );

    Set<ConstraintViolation<StatusUpdateRequestDTO>> violations =
        validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldFailWhenStatusIsNull() {
    StatusUpdateRequestDTO request =
        new StatusUpdateRequestDTO(
            null,
            "Заявка рассмотрена",
            new BigDecimal("15.5")
        );

    Set<ConstraintViolation<StatusUpdateRequestDTO>> violations =
        validator.validate(request);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString())
        .contains("status");
  }

  @Test
  void shouldFailWhenCommentIsTooLong() {
    StatusUpdateRequestDTO request =
        new StatusUpdateRequestDTO(
            UpdateStatusDTO.APPROVED,
            "a".repeat(501),
            new BigDecimal("15.5")
        );

    Set<ConstraintViolation<StatusUpdateRequestDTO>> violations =
        validator.validate(request);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString())
        .contains("managerComment");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "0.00",
      "100.01",
      "15.123"
  })
  void shouldFailWhenInterestRateIsInvalid(String interestRate) {
    StatusUpdateRequestDTO request = new StatusUpdateRequestDTO(
        UpdateStatusDTO.APPROVED,
        "Заявка одобрена",
        new BigDecimal(interestRate)
    );

    Set<ConstraintViolation<StatusUpdateRequestDTO>> violations =
        validator.validate(request);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("interestRate");
  }
}
