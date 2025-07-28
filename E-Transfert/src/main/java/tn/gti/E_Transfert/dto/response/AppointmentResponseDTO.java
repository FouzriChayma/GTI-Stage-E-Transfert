package tn.gti.E_Transfert.dto.response;

import lombok.*;
import tn.gti.E_Transfert.enums.AppointmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {
    private Long idAppointment;
    private UserResponseDTO user;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private AppointmentStatus status;
    private String notes;
    private Boolean isNotified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}