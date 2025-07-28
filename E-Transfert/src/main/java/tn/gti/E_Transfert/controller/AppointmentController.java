// src/main/java/tn/gti/E_Transfert/controller/AppointmentController.java
package tn.gti.E_Transfert.controller;

import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.gti.E_Transfert.dto.request.AppointmentRequestDTO;
import tn.gti.E_Transfert.dto.response.AppointmentResponseDTO;
import tn.gti.E_Transfert.enums.AppointmentStatus;
import tn.gti.E_Transfert.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Validated({AppointmentRequestDTO.Create.class, Default.class})
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@RequestBody AppointmentRequestDTO requestDTO) {
        return new ResponseEntity<>(appointmentService.scheduleAppointment(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Validated({AppointmentRequestDTO.Update.class, Default.class})
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequestDTO requestDTO) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AppointmentResponseDTO>> getAvailableSlots(
            @RequestParam LocalDateTime start, @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(start, end));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getUserAppointments(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }

    // New search endpoint
    @GetMapping("/search")
    public ResponseEntity<List<AppointmentResponseDTO>> searchAppointments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Boolean isNotified) {
        List<AppointmentResponseDTO> appointments = appointmentService.searchAppointments(
                userId, startDate, endDate, status, notes, isNotified);
        return appointments.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(appointments);
    }
}