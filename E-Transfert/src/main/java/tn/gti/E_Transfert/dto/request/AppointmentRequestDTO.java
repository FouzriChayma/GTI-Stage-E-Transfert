package tn.gti.E_Transfert.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.gti.E_Transfert.enums.AppointmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDTO {

    @NotNull(message = "User ID is required", groups = {Create.class, Update.class})
    @Positive(message = "User ID must be positive", groups = {Create.class, Update.class})
    private Long userId;

    @NotNull(message = "Appointment date and time are required", groups = {Create.class, Update.class})
    @Future(message = "Appointment date must be in the future", groups = {Create.class, Update.class})
    private LocalDateTime appointmentDateTime;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    private Integer durationMinutes;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    // Validation groups
    public interface Create {}
    public interface Update {}
}