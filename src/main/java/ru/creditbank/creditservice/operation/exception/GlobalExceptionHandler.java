package ru.creditbank.creditservice.operation.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.creditbank.creditservice.operation.dto.error.ErrorResponseDTO;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .distinct()
        .collect(Collectors.joining("; "));

    ErrorResponseDTO response = new ErrorResponseDTO(
        LocalDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );

    return ResponseEntity
        .status(status)
        .body(response);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponseDTO> handleBusinessException(
      BusinessException ex,
      HttpServletRequest request
  ) {
    log.warn(
        "Business exception: {} - {}",
        ex.getClass().getSimpleName(),
        ex.getMessage()
    );

    ErrorResponseDTO response = new ErrorResponseDTO(
        LocalDateTime.now(),
        ex.getStatus().value(),
        ex.getStatus().getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity
        .status(ex.getStatus())
        .body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error", ex);

    ErrorResponseDTO response = new ErrorResponseDTO(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        "An unexpected error occurred",
        request.getRequestURI()
    );
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(response);
  }
}
