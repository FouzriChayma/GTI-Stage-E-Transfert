package tn.gti.E_Transfert.entity;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tn.gti.E_Transfert.enums.AccountType;
import tn.gti.E_Transfert.enums.FeeType;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfer_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransferRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(name = "commission_account_number", nullable = false)
    @NotBlank(message = "Commission account number is required")
    private String commissionAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_account_type", nullable = false)
    @NotNull(message = "Commission account type is required")
    private AccountType commissionAccountType;

    @Column(name = "settlement_account_number", nullable = false)
    @NotBlank(message = "Settlement account number is required")
    private String settlementAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_account_type", nullable = false)
    @NotNull(message = "Settlement account type is required")
    private AccountType settlementAccountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    @NotNull(message = "Transfer type is required")
    private TransferType transferType;

    @Column(name = "issue_date", nullable = false)
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @JsonSetter("issueDate")
    public void setIssueDate(String dateStr) {
        this.issueDate = LocalDate.parse(dateStr);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @Column(name = "currency", nullable = false)
    @NotBlank(message = "Currency is required")
    private String currency;

    @Column(name = "amount", nullable = false, precision = 15, scale = 3)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.001", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "transfer_reason")
    private String transferReason;

    @Column(name = "is_negotiation")
    private Boolean isNegotiation = false;

    @Column(name = "is_term_negotiation")
    private Boolean isTermNegotiation = false;

    @Column(name = "is_financing")
    private Boolean isFinancing = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validator_id")
    private Long validatorId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    @NotNull(message = "Beneficiary is required")
    private Beneficiary beneficiary;

    @OneToMany(mappedBy = "transferRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
        if (transferType == TransferType.COMMERCIAL) {
            // Validation can be moved to service layer if needed
        }
    }
}