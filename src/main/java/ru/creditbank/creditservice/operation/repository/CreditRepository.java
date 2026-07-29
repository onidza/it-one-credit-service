package ru.creditbank.creditservice.operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.creditbank.creditservice.operation.entity.CreditEntity;

import java.time.Instant;
import java.util.UUID;

public interface CreditRepository extends JpaRepository<CreditEntity, UUID> {

    boolean existsByUserIdAndCreationDateAfter(
            UUID userId,
            Instant creationDate
    );
}
