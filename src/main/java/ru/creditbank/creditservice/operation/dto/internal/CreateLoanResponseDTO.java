package ru.creditbank.creditservice.operation.dto.internal;

import java.util.UUID;

public record CreateLoanResponseDTO(
    UUID loanId,
    LoanStatus status
) {

}
