package ru.creditbank.creditservice.operation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.dto.details.CreditApplicationDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.LoanDetailsDTO;
import ru.creditbank.creditservice.operation.dto.details.UserInfoDTO;
import ru.creditbank.creditservice.operation.dto.publisher.CreditApprovedEventDTO;
import ru.creditbank.creditservice.operation.dto.publisher.CreditStatusChangedEventDTO;
import ru.creditbank.creditservice.operation.dto.status.StatusUpdateRequestDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.exception.ApplicationAlreadyExistsException;
import ru.creditbank.creditservice.operation.exception.CreditApplicationNotFoundException;
import ru.creditbank.creditservice.operation.repository.CreditRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCreateService {

  private final CreditRepository creditRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(rollbackFor = Exception.class)
  public CreditApplicationResponseDTO createApplication(
      CreditApplicationRequestDTO request,
      UUID userId,
      String email
  ) {
    if (creditRepository.existsByUserId(userId)) {
      throw new ApplicationAlreadyExistsException(userId);
    }

    CreditEntity application = new CreditEntity(
        userId,
        request.fullName(),
        email,
        request.requestedAmount(),
        request.termMonths()
    );

    creditRepository.saveAndFlush(application);

    log.info(
        "Credit application created, applicationId = {}, userId = {}",
        application.getId(),
        userId
    );

    return new CreditApplicationResponseDTO(
        application.getId(),
        application.getStatus(),
        application.getCreationDate()
    );
  }

  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public CreditApplicationDetailsDTO getApplicationDetailsById(UUID applicationId) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(() ->
            new CreditApplicationNotFoundException(applicationId)
        );

    log.info("Credit application details found, id = {}", applicationId);

    return new CreditApplicationDetailsDTO(
        applicationId,
        new UserInfoDTO(
            application.getUserId(),
            application.getUserFullName(),
            application.getUserEmail()
        ),
        new LoanDetailsDTO(
            application.getRequestedAmount(),
            application.getTermMonths(),
            application.getInterestRate()
        ),
        application.getStatus(),
        application.getCreationDate()
    );
  }

  @Transactional(rollbackFor = Exception.class)
  public void changeStatusByApplicationId(
      UUID applicationId,
      StatusUpdateRequestDTO request,
      String accessToken
  ) {
    CreditEntity application = creditRepository.findById(applicationId)
        .orElseThrow(() ->
            new CreditApplicationNotFoundException(applicationId)
        );

    application.changeStatus(
        Status.valueOf(request.status().name()),
        request.managerComment(),
        request.interestRate()
    );

    log.info("Application = {} has a new status = {}",
        applicationId, request.status().name());

    eventPublisher.publishEvent(
        new CreditStatusChangedEventDTO(
            application.getUserEmail(),
            application.getUserFullName(),
            applicationId,
            application.getStatus(),
            application.getManagerComment()
        )
    );

    if (application.getStatus() == Status.APPROVED) {
      eventPublisher.publishEvent(new CreditApprovedEventDTO(
              applicationId,
              application.getUserId(),
              application.getRequestedAmount(),
              application.getTermMonths(),
              application.getInterestRate(),
              accessToken
          )
      );
    }
  }
}