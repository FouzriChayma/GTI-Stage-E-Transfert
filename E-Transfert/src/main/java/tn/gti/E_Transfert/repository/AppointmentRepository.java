// src/main/java/tn/gti/E_Transfert/repository/AppointmentRepository.java
package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Add this import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.gti.E_Transfert.entity.Appointment;
import tn.gti.E_Transfert.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId")
    List<Appointment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :start AND :end " +
            "AND a.status NOT IN (:completed, :cancelled)")
    List<Appointment> findAvailableSlots(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("completed") AppointmentStatus completed,
                                         @Param("cancelled") AppointmentStatus cancelled);
}