package tn.gti.E_Transfert.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.gti.E_Transfert.enums.AccountType;
import tn.gti.E_Transfert.enums.FeeType;
import tn.gti.E_Transfert.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestRequestDTO {
    @NotNull(message = "User ID is required", groups = {Create.class, Update.class})
    @Positive(message = "User ID must be positive", groups = {Create.class, Update.class})
    Long userId;

    @NotBlank(message = "Commission account number is required", groups = {Create.class, Update.class})
    @Size(min = 10, max = 34, message = "Commission account number must be between 10 and 34 characters", groups = {Create.class, Update.class})
    String commissionAccountNumber;

    @NotNull(message = "Commission account type is required", groups = {Create.class, Update.class})
    AccountType commissionAccountType;

    @NotBlank(message = "Settlement account number is required", groups = {Create.class, Update.class})
    @Size(min = 10, max = 34, message = "Settlement account number must be between 10 and 34 characters", groups = {Create.class, Update.class})
    String settlementAccountNumber;

    @NotNull(message = "Settlement account type is required", groups = {Create.class, Update.class})
    AccountType settlementAccountType;

    @NotNull(message = "Transfer type is required", groups = {Create.class, Update.class})
    TransferType transferType;

    @NotNull(message = "Issue date is required", groups = {Create.class, Update.class})
    @PastOrPresent(message = "Issue date cannot be in the future", groups = {Create.class, Update.class})
    LocalDate issueDate;

    @NotNull(message = "Fee type is required", groups = {Create.class, Update.class})
    FeeType feeType;

    @NotBlank(message = "Currency is required", groups = {Create.class, Update.class})
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid ISO 4217 code", groups = {Create.class, Update.class})
    String currency;

    @NotNull(message = "Amount is required", groups = {Create.class, Update.class})
    @DecimalMin(value = " leads to 0.001", message = "Amount must be greater than 0", groups = {Create.class, Update.class})
    BigDecimal amount;

    @Size(max = 50, message = "Invoice number must not exceed 50 characters", groups = {Create.class, Update.class})
    String invoiceNumber;

    @PastOrPresent(message = "Invoice date cannot be in the future", groups = {Create.class, Update.class})
    LocalDate invoiceDate;

    @Size(max = 255, message = "Transfer reason must not exceed 255 characters", groups = {Create.class, Update.class})
    String transferReason;

    Boolean isNegotiation;
    Boolean isTermNegotiation;
    Boolean isFinancing;

    @NotNull(message = "Beneficiary is required", groups = {Create.class})
    BeneficiaryCreateDTO beneficiary;

    @NotNull(message = "Beneficiary ID is required", groups = {Update.class})
    Long beneficiaryId;


    // Validation groups
    public interface Create {}
    public interface Update {}
}