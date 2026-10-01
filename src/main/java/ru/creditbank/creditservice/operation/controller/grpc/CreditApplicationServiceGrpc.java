package ru.creditbank.creditservice.operation.controller.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.creditbank.contract.credit.v1.CreditApplicationDetailsRequest;
import ru.creditbank.contract.credit.v1.CreditApplicationDetailsResponse;
import ru.creditbank.contract.credit.v1.CreditApplicationRequest;
import ru.creditbank.contract.credit.v1.CreditApplicationResponse;
import ru.creditbank.contract.credit.v1.CreditServiceGrpc;
import ru.creditbank.contract.credit.v1.CreditStatus;
import ru.creditbank.contract.credit.v1.LoanDetails;
import ru.creditbank.contract.credit.v1.StatusUpdateRequest;
import ru.creditbank.contract.credit.v1.UpdateStatus;
import ru.creditbank.contract.credit.v1.UserInfo;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.dto.details.CreditApplicationDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.LoanDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.UserInfoDTO;
import ru.creditbank.creditservice.operation.dto.status.StatusUpdateRequestDTO;
import ru.creditbank.creditservice.operation.dto.status.UpdateStatusDTO;
import ru.creditbank.creditservice.operation.service.CreditService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditApplicationServiceGrpc extends CreditServiceGrpc.CreditServiceImplBase {

  private final Validator validator;
  private final CreditService creditService;

  @Override
  public void createApplication(
      CreditApplicationRequest grpcRequest,
      StreamObserver<CreditApplicationResponse> responseObserver
  ) {
    CreditApplicationRequestDTO request =
        new CreditApplicationRequestDTO(
            grpcRequest.getFullName(),
            parseAmount(grpcRequest.getRequestedAmount()),
            grpcRequest.getTermMonths()
        );

    validate(request);
    Jwt jwt = getCurrentJwt();

    log.info(
        "JWT authentication passed, subject={}",
        jwt.getSubject()
    );

    CreditApplicationResponseDTO response = creditService
        .createApplication(
            request,
            UUID.fromString(Objects.requireNonNull(jwt.getClaimAsString("user_id"))),
            jwt.getSubject(),
            jwt.getTokenValue()
        );

    log.info(
        "Credit application created, id={}",
        response.id()
    );

    CreditApplicationResponse grpcResponse =
        CreditApplicationResponse.newBuilder()
            .setId(response.id().toString())
            .setStatus(mapCreditStatusToGrpc(response.status()))
            .setCreatedAt(mapInstantToGrpc(response.createdAt()))
            .build();

    responseObserver.onNext(grpcResponse);
    responseObserver.onCompleted();
  }

  @Override
  public void getApplicationDetails(
      CreditApplicationDetailsRequest grpcRequest,
      StreamObserver<CreditApplicationDetailsResponse> responseObserver
  ) {
    CreditApplicationDetailsDTO details = creditService.getApplicationDetailsById(
        UUID.fromString(grpcRequest.getId()));

    CreditApplicationDetailsResponse grpcResponse =
        CreditApplicationDetailsResponse.newBuilder()
            .setId(details.id().toString())
            .setUserInfo(userInfoToGrpc(details))
            .setLoanDetails(loanDetailsToGrpc(details))
            .setStatus(mapCreditStatusToGrpc(details.status()))
            .setCreatedAt(mapInstantToGrpc(details.createdAt()))
            .build();

    responseObserver.onNext(grpcResponse);
    responseObserver.onCompleted();
  }

  @Override
  public void changeApplicationStatus(
      StatusUpdateRequest grpcRequest,
      StreamObserver<Empty> responseObserver
  ) {
    StatusUpdateRequestDTO request = statusUpdateRequestToDto(grpcRequest);

    validate(request);

    creditService.changeStatusManuallyByApplicationId(
        UUID.fromString(grpcRequest.getId()),
        request,
        getCurrentJwt().getTokenValue()
    );

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  private CreditStatus mapCreditStatusToGrpc(
      ru.creditbank.creditservice.operation.entity.Status status
  ) {
    return switch (status) {
      case PENDING -> CreditStatus.CREDIT_STATUS_PENDING;
      case APPROVING -> CreditStatus.CREDIT_STATUS_APPROVING;
      case APPROVED -> CreditStatus.CREDIT_STATUS_APPROVED;
      case REJECTED -> CreditStatus.CREDIT_STATUS_REJECTED;
    };
  }

  private Timestamp mapInstantToGrpc(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  private UserInfo userInfoToGrpc(CreditApplicationDetailsDTO details) {
    UserInfoDTO userInfo = details.userInfo();

    return UserInfo.newBuilder()
        .setUserId(userInfo.userId().toString())
        .setFullName(userInfo.fullName())
        .setEmail(userInfo.email())
        .build();
  }

  private LoanDetails loanDetailsToGrpc(CreditApplicationDetailsDTO details) {
    LoanDetailsDTO grpcLoanDetails = details.loanDetails();

    LoanDetails.Builder grpcLoanDetailsBuilder =
        LoanDetails.newBuilder()
            .setRequestedAmount(grpcLoanDetails.requestedAmount().toPlainString())
            .setTermMonths(grpcLoanDetails.termMonths());

    if (grpcLoanDetails.interestRate() != null) {
      grpcLoanDetailsBuilder.setInterestRate(grpcLoanDetails.interestRate().toPlainString());
    }

    return grpcLoanDetailsBuilder.build();
  }

  private UpdateStatusDTO updateStatusToDto(StatusUpdateRequest grpcRequest) {
    UpdateStatus grpcUpdateStatus = grpcRequest.getStatus();

    return switch (grpcUpdateStatus) {
      case UPDATE_STATUS_APPROVING -> UpdateStatusDTO.APPROVING;
      case UPDATE_STATUS_REJECTED -> UpdateStatusDTO.REJECTED;
      case UNRECOGNIZED, UPDATE_STATUS_UNSPECIFIED -> throw new IllegalStateException(
          "Unexpected credit status: " + grpcUpdateStatus
      );
    };
  }

  private StatusUpdateRequestDTO statusUpdateRequestToDto(StatusUpdateRequest grpcRequest) {
    return new StatusUpdateRequestDTO(
        updateStatusToDto(grpcRequest),
        grpcRequest.hasManagerComment() ? grpcRequest.getManagerComment() : null,
        grpcRequest.hasInterestRate() ? parseAmount(grpcRequest.getInterestRate()) : null
    );
  }

  private BigDecimal parseAmount(String value) {

    try {
      return new BigDecimal(value);
    } catch (NumberFormatException e) {
      throw Status.INVALID_ARGUMENT
          .withDescription("Invalid amount format")
          .asRuntimeException();
    }
  }

  private <T> void validate(T request) {
    Set<ConstraintViolation<T>> violations =
        validator.validate(request);

    if (!violations.isEmpty()) {
      String message = violations.stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining("; "));

      throw Status.INVALID_ARGUMENT
          .withDescription(message)
          .asRuntimeException();
    }
  }

  private Jwt getCurrentJwt() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder
            .getContext()
            .getAuthentication();

    return authentication.getToken();
  }
}