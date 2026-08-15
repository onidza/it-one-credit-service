package ru.creditbank.creditservice.operation.repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.creditbank.creditservice.operation.entity.CreditEntity;
import ru.creditbank.creditservice.operation.entity.Status;

public interface CreditRepository extends JpaRepository<CreditEntity, UUID> {

  boolean existsByUserIdAndStatusIn(UUID userId, Collection<Status> statuses);
}
