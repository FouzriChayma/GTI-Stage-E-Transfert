// src/main/java/tn/gti/E_Transfert/repository/AppointmentSpecification.java
package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.domain.Specification;
import tn.gti.E_Transfert.entity.Appointment;
import tn.gti.E_Transfert.entity.User;
import tn.gti.E_Transfert.enums.AppointmentStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentSpecification {

    public static Specification<Appointment> searchByCriteria(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            AppointmentStatus status,
            String notes,
            Boolean isNotified) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("appointmentDateTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("appointmentDateTime"), endDate));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (notes != null && !notes.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), "%" + notes.toLowerCase() + "%"));
            }
            if (isNotified != null) {
                predicates.add(criteriaBuilder.equal(root.get("isNotified"), isNotified));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}