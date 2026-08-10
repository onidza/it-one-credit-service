package ru.creditbank.creditservice.operation.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.creditbank.creditservice.operation.entity.CreditEntity;

public interface CreditRepository extends JpaRepository<CreditEntity, UUID> {

  boolean existsByUserId(UUID userId);
}
