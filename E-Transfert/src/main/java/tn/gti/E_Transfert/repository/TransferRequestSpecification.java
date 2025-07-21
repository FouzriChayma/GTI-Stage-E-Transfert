package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.domain.Specification;
import tn.gti.E_Transfert.entity.TransferRequest;
import tn.gti.E_Transfert.entity.User;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransferRequestSpecification {

    public static Specification<TransferRequest> searchByCriteria(
            Long userId,
            String commissionAccountNumber,
            TransferType transferType,
            TransferStatus status,
            BigDecimal amount,
            String firstName,
            String lastName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
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
            if (firstName != null && !firstName.isEmpty()) {
                Join<TransferRequest, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isEmpty()) {
                Join<TransferRequest, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}