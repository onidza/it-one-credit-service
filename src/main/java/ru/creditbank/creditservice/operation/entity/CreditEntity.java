package ru.creditbank.creditservice.operation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
            name = "requested_amount",
            nullable = false
    )
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

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

    public CreditEntity(
            UUID userId,
            String userFullName,
            BigDecimal requestedAmount,
            Integer termMonths
    ) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.status = Status.PENDING;
    }
}
