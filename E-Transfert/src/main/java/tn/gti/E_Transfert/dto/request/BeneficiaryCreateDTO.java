package tn.gti.E_Transfert.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BeneficiaryCreateDTO {
    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 100, message = "Beneficiary name must not exceed 100 characters")
    String name;

    @NotBlank(message = "Beneficiary country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country;

    @NotBlank(message = "Destination bank is required")
    @Size(max = 100, message = "Destination bank must not exceed 100 characters")
    String destinationBank;

    @Size(max = 34, message = "Bank account must not exceed 34 characters")
    String bankAccount;
}