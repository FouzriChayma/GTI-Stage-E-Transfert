package tn.gti.E_Transfert.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BeneficiaryResponseDTO {
    Long idBeneficiary;
    String name;
    String country;
    String destinationBank;
    String bankAccount;
}