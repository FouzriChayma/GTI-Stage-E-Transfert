package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.gti.E_Transfert.entity.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
}