package ru.creditbank.creditservice.operation.repository;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.creditbank.creditservice.operation.entity.CreditEntity;

public interface CreditRepository extends JpaRepository<CreditEntity, UUID> {

  boolean existsByUserIdAndCreationDateAfter(UUID userId, Instant creationDateAfter);
}
