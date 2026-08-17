package ru.creditbank.creditservice.operation.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.creditbank.creditservice.operation.entity.OutboxEventEntity;
import ru.creditbank.creditservice.operation.entity.OutboxStatus;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

  @Query("""
      select e
      from OutboxEventEntity e
      where e.status = :pending
      or (e.status = :retry and e.nextAttemptAt <= :now)
      order by e.createdAt
      """)
  List<OutboxEventEntity> findReadyForProcessing(
      OutboxStatus pending,
      OutboxStatus retry,
      Instant now
  );
}
