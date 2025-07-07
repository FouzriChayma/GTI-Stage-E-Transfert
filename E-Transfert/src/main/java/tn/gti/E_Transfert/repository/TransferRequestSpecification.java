package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.domain.Specification;
import tn.gti.E_Transfert.entity.TransferRequest;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransferRequestSpecification {

    public static Specification<TransferRequest> searchByCriteria(Long userId, String commissionAccountNumber,
                                                                  TransferType transferType, TransferStatus status, BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            if (commissionAccountNumber != null && !commissionAccountNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("commissionAccountNumber"), "%" + commissionAccountNumber + "%"));
            }
            if (transferType != null) {
                predicates.add(criteriaBuilder.equal(root.get("transferType"), transferType));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (amount != null) {
                predicates.add(criteriaBuilder.equal(root.get("amount"), amount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}