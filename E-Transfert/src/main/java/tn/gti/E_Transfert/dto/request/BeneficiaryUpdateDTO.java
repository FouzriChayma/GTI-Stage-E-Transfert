package tn.gti.E_Transfert.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BeneficiaryUpdateDTO {
    Long idBeneficiary;

    @Size(max = 100, message = "Beneficiary name must not exceed 100 characters")
    String name;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country;

    @Size(max = 100, message = "Destination bank must not exceed 100 characters")
    String destinationBank;

    @Size(max = 34, message = "Bank account must not exceed 34 characters")
    String bankAccount;
}
