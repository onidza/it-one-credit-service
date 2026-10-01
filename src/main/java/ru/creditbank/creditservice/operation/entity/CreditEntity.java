package ru.creditbank.creditservice.operation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.creditbank.creditservice.operation.exception.rest.InvalidStatusUpdateApprovedException;

@Entity
@Table(name = "credit_operation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "user_full_name",
      nullable = false,
      length = 100
  )
  private String userFullName;

  @Column(
      name = "user_email",
      nullable = false
  )
  private String userEmail;

  @Column(
      name = "requested_amount",
      nullable = false
  )
  private BigDecimal requestedAmount;

  @Column(name = "term_months", nullable = false)
  private Integer termMonths;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @CreationTimestamp
  @Column(
      name = "creation_date",
      nullable = false,
      updatable = false
  )
  private Instant creationDate;

  @UpdateTimestamp
  @Column(name = "last_updated", nullable = false)
  private Instant lastUpdated;

  @Setter
  @Column(
      name = "interest_rate",
      precision = 5,
      scale = 2
  )
  private BigDecimal interestRate;

  @Setter
  @Column(
      name = "manager_comment",
      length = 500
  )
  private String managerComment;

  public CreditEntity(
      UUID userId,
      String userFullName,
      String userEmail,
      BigDecimal requestedAmount,
      Integer termMonths
  ) {
    this.userId = userId;
    this.userFullName = userFullName;
    this.userEmail = userEmail;
    this.requestedAmount = requestedAmount;
    this.termMonths = termMonths;
    this.status = Status.PENDING;
  }

  public void changeStatus(
      Status newStatus,
      String managerComment,
      BigDecimal interestRate
  ) {
    if (newStatus == Status.APPROVING) {
      if (interestRate == null || interestRate.signum() <= 0) {
        throw new InvalidStatusUpdateApprovedException();
      }
      this.interestRate = interestRate;
    }

    if (newStatus == Status.REJECTED) {
      this.interestRate = null;
    }

    this.status = newStatus;
    this.managerComment = managerComment == null
        ? null
        : managerComment.strip();
  }

  public void activate(
      Status approving,
      BigDecimal interestRate
  ) {
    this.status = approving;
    this.interestRate = interestRate;
  }
}
