package ru.creditbank.creditservice.operation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.creditbank.creditservice.operation.dto.error.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateCreditApplicationException.class)
  public ResponseEntity<ErrorResponseDTO> handleBadCredentials(
      DuplicateCreditApplicationException exception
  ) {
    ErrorResponseDTO response = new ErrorResponseDTO(
        HttpStatus.BAD_REQUEST.value(),
        exception.getMessage()
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }
}
