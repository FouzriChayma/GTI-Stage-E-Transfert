package tn.gti.E_Transfert.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Only serialize, don't deserialize
    private Long idBeneficiary;

    @Column(name = "beneficiary_name", nullable = false)
    @NotBlank(message = "Beneficiary name is required")
    private String name;

    @Column(name = "beneficiary_country", nullable = false)
    @NotBlank(message = "Beneficiary country is required")
    private String country;

    @Column(name = "beneficiary_bank_name", nullable = false)
    @NotBlank(message = "Destination bank is required")
    private String destinationBank;

    @Column(name = "beneficiary_bank_account")
    private String bankAccount;
}