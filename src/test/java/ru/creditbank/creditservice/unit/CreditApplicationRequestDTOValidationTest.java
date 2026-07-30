package ru.creditbank.creditservice.unit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreditApplicationRequestDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    @Test
    void shouldPassValidationForValidRequest() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        new BigDecimal("50000"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenFullNameIsNull() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        null,
                        new BigDecimal("50000"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("fullName");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(NotBlank.class);
                });
    }

    @Test
    void shouldFailWhenFullNameIsBlank() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "",
                        new BigDecimal("50000"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .filteredOn(violation ->
                        violation.getConstraintDescriptor()
                                .getAnnotation() instanceof NotBlank)
                .singleElement()
                .satisfies(violation ->
                        assertThat(violation.getPropertyPath())
                                .hasToString("fullName")
                );
    }

    @Test
    void shouldFailWhenFullNameIsTooShort() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван",
                        new BigDecimal("50000"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("fullName");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(Size.class);
                });
    }

    @Test
    void shouldFailWhenFullNameContainsInvalidCharacters() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов123",
                        new BigDecimal("50000"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("fullName");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(Pattern.class);
                });
    }

    @Test
    void shouldFailWhenRequestedAmountIsNull() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        null,
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("requestedAmount");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(NotNull.class);
                });
    }

    @Test
    void shouldFailWhenRequestedAmountIsNegative() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        new BigDecimal("-1"),
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("requestedAmount");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(Positive.class);
                });
    }

    @Test
    void shouldFailWhenRequestedAmountIsZero() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        BigDecimal.ZERO,
                        12
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("requestedAmount");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(Positive.class);
                });
    }

    @Test
    void shouldFailWhenTermMonthsIsNull() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        new BigDecimal("50000"),
                        null
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("termMonths");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(NotNull.class);
                });
    }

    @Test
    void shouldFailWhenTermMonthsIsLessThanOne() {
        CreditApplicationRequestDTO request =
                new CreditApplicationRequestDTO(
                        "Иван Иванов",
                        new BigDecimal("50000"),
                        0
                );

        Set<ConstraintViolation<CreditApplicationRequestDTO>> violations =
                validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath())
                            .hasToString("termMonths");

                    assertThat(violation.getConstraintDescriptor().getAnnotation())
                            .isInstanceOf(Min.class);
                });
    }
}
