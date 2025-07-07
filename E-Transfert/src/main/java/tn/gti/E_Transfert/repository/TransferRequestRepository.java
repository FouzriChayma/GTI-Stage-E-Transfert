package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import tn.gti.E_Transfert.entity.TransferRequest;

import java.util.List;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long>, JpaSpecificationExecutor<TransferRequest> {
    @Query("SELECT DISTINCT tr FROM TransferRequest tr " +
            "LEFT JOIN FETCH tr.beneficiary " +
            "LEFT JOIN FETCH tr.documents")
    List<TransferRequest> findAllWithDetails();
}