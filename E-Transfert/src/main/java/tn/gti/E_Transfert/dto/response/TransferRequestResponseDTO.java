package tn.gti.E_Transfert.dto.response;

import lombok.*;
import tn.gti.E_Transfert.enums.AccountType;
import tn.gti.E_Transfert.enums.FeeType;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestResponseDTO {
    Long idTransferRequest;
    UserResponseDTO user; // Replace userId with UserResponseDTO
    String commissionAccountNumber;
    AccountType commissionAccountType;
    String settlementAccountNumber;
    AccountType settlementAccountType;
    TransferType transferType;
    LocalDate issueDate;
    FeeType feeType;
    String currency;
    BigDecimal amount;
    String invoiceNumber;
    LocalDate invoiceDate;
    String transferReason;
    Boolean isNegotiation;
    Boolean isTermNegotiation;
    Boolean isFinancing;
    TransferStatus status;
    LocalDateTime createdAt;
    LocalDateTime validatedAt;
    Long validatorId;
    BeneficiaryResponseDTO beneficiary;
    List<DocumentResponseDTO> documents;
}