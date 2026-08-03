package ru.creditbank.creditservice.operation.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationRequestDTO;
import ru.creditbank.creditservice.operation.dto.application.CreditApplicationResponseDTO;
import ru.creditbank.creditservice.operation.dto.details.CreditApplicationDetailsDTO;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.exception.DuplicateCreditApplicationException;
import ru.creditbank.creditservice.operation.repository.CreditRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCreateService {

  private static final Duration DUPLICATE_INTERVAL =
      Duration.ofSeconds(10);
  private final CreditRepository creditRepository;

  @Transactional(rollbackFor = Exception.class)
  public CreditApplicationResponseDTO createApplication(
      CreditApplicationRequestDTO request,
      UUID userId
  ) {
    boolean recentlyCreated =
        creditRepository.existsByUserIdAndCreationDateAfter(
            userId,
            Instant.now().minus(DUPLICATE_INTERVAL)
        );

    if (recentlyCreated) {
      throw new DuplicateCreditApplicationException(
          "Credit application has already been submitted"
      );
    }

    CreditEntity credit = new CreditEntity(
        userId,
        request.fullName(),
        request.requestedAmount(),
        request.termMonths()
    );

    creditRepository.saveAndFlush(credit);

    log.info(
        "Credit application created, applicationId = {}, userId = {}",
        credit.getId(),
        userId
    );

    return new CreditApplicationResponseDTO(
        credit.getId(),
        credit.getStatus(),
        credit.getCreationDate()
    );
  }

  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public CreditApplicationDetailsDTO getApplicationDetailsById(UUID applicationId) {
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public void changeStatusByApplicationId(UUID applicationId) {

  }
}
