package tn.gti.E_Transfert.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gti.E_Transfert.dto.request.AppointmentRequestDTO;
import tn.gti.E_Transfert.dto.response.AppointmentResponseDTO;
import tn.gti.E_Transfert.dto.response.UserResponseDTO;
import tn.gti.E_Transfert.entity.Appointment;
import tn.gti.E_Transfert.entity.User;
import tn.gti.E_Transfert.enums.AppointmentStatus;
import tn.gti.E_Transfert.exception.TransferException;
import tn.gti.E_Transfert.repository.AppointmentRepository;
import tn.gti.E_Transfert.repository.AppointmentSpecification;
import tn.gti.E_Transfert.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${appointment.notification-threshold-hours:24}")
    private Long notificationThresholdHours;

    public AppointmentResponseDTO scheduleAppointment(AppointmentRequestDTO requestDTO) {
        log.info("Scheduling appointment for user ID: {}", requestDTO.getUserId());
        validateAppointmentRequest(requestDTO);

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new TransferException("User not found with ID: " + requestDTO.getUserId()));

        Appointment appointment = modelMapper.map(requestDTO, Appointment.class);
        appointment.setUser(user);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(appointment);
        scheduleNotification(saved);
        AppointmentResponseDTO dto = modelMapper.map(saved, AppointmentResponseDTO.class);
        dto.setUser(modelMapper.map(user, UserResponseDTO.class));
        return dto;
    }

    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO requestDTO) {
        log.info("Updating appointment with ID: {}", id);
        validateAppointmentRequest(requestDTO);

        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new TransferException("Appointment not found with ID: " + id));

        if (existing.getStatus() == AppointmentStatus.COMPLETED || existing.getStatus() == AppointmentStatus.CANCELLED) {
            throw new TransferException("Cannot update completed or cancelled appointment");
        }

        // Check if appointmentDateTime has changed
        boolean isRescheduled = !existing.getAppointmentDateTime().equals(requestDTO.getAppointmentDateTime());

        modelMapper.map(requestDTO, existing);
        existing.setUser(userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new TransferException("User not found with ID: " + requestDTO.getUserId())));
        existing.setUpdatedAt(LocalDateTime.now());

        // Set status to RESCHEDULED if the date/time changed and status is not already RESCHEDULED
        if (isRescheduled && existing.getStatus() != AppointmentStatus.RESCHEDULED) {
            existing.setStatus(AppointmentStatus.RESCHEDULED);
        }

        Appointment saved = appointmentRepository.save(existing);
        AppointmentResponseDTO dto = modelMapper.map(saved, AppointmentResponseDTO.class);
        dto.setUser(modelMapper.map(saved.getUser(), UserResponseDTO.class));
        return dto;
    }

    public void cancelAppointment(Long id) {
        log.info("Cancelling appointment with ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new TransferException("Appointment not found with ID: " + id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }


    public List<AppointmentResponseDTO> getAvailableSlots(LocalDateTime start, LocalDateTime end) {
        log.info("Retrieving available slots from {} to {}", start, end);
        return appointmentRepository.findAvailableSlots(start, end, AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED)
                .stream()
                .map(a -> {
                    AppointmentResponseDTO dto = modelMapper.map(a, AppointmentResponseDTO.class);
                    dto.setUser(modelMapper.map(a.getUser(), UserResponseDTO.class));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getUserAppointments(Long userId) {
        log.info("Retrieving appointments for user ID: {}", userId);
        return appointmentRepository.findByUserId(userId)
                .stream()
                .map(a -> {
                    AppointmentResponseDTO dto = modelMapper.map(a, AppointmentResponseDTO.class);
                    dto.setUser(modelMapper.map(a.getUser(), UserResponseDTO.class));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> searchAppointments(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            AppointmentStatus status,
            String notes,
            Boolean isNotified) {
        log.info("Searching appointments with criteria: userId={}, startDate={}, endDate={}, status={}, notes={}, isNotified={}",
                userId, startDate, endDate, status, notes, isNotified);
        try {
            return appointmentRepository.findAll(
                            AppointmentSpecification.searchByCriteria(userId, startDate, endDate, status, notes, isNotified))
                    .stream()
                    .map(appointment -> {
                        AppointmentResponseDTO dto = modelMapper.map(appointment, AppointmentResponseDTO.class);
                        dto.setUser(modelMapper.map(appointment.getUser(), UserResponseDTO.class));
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search appointments: {}", e.getMessage(), e);
            throw new TransferException("Failed to search appointments", e);
        }
    }

    private void validateAppointmentRequest(AppointmentRequestDTO requestDTO) {
        if (requestDTO.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new TransferException("Appointment date must be in the future");
        }
        if (requestDTO.getDurationMinutes() < 15 || requestDTO.getDurationMinutes() % 15 != 0) {
            throw new TransferException("Duration must be in 15-minute increments and at least 15 minutes");
        }
        List<Appointment> overlapping = appointmentRepository.findAvailableSlots(
                requestDTO.getAppointmentDateTime().minusMinutes(15),
                requestDTO.getAppointmentDateTime().plusMinutes(requestDTO.getDurationMinutes()),
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED
        );
        if (!overlapping.isEmpty()) {
            throw new TransferException("Requested time slot overlaps with existing appointments");
        }
    }

    private void scheduleNotification(Appointment appointment) {
        LocalDateTime threshold = appointment.getAppointmentDateTime().minusHours(notificationThresholdHours);
        log.info("Scheduling notification for appointment ID: {} at threshold {}",
                appointment.getIdAppointment(), threshold);
        if (LocalDateTime.now().isAfter(threshold)) {
            sendNotification(appointment);
        }
    }

    private void sendNotification(Appointment appointment) {
        log.info("Sending notification for appointment ID: {} at {}",
                appointment.getIdAppointment(), appointment.getAppointmentDateTime());
        appointment.setIsNotified(true);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    public Map<AppointmentStatus, Long> getAppointmentTrends() {
        return appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
    }

    public AppointmentResponseDTO getAppointment(Long id) {
        log.info("Retrieving appointment with ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new TransferException("Appointment not found with ID: " + id));
        AppointmentResponseDTO dto = modelMapper.map(appointment, AppointmentResponseDTO.class);
        dto.setUser(modelMapper.map(appointment.getUser(), UserResponseDTO.class));
        return dto;
    }


    // ✅ Hard delete
    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new TransferException("Appointment not found with ID: " + id));
        appointmentRepository.delete(appointment);
    }
    public void deleteMultipleAppointments(List<Long> ids) {
        log.info("Deleting multiple appointments with IDs: {}", ids);
        List<Appointment> appointments = appointmentRepository.findAllById(ids);
        if (appointments.size() != ids.size()) {
            List<Long> foundIds = appointments.stream()
                    .map(Appointment::getIdAppointment)
                    .collect(Collectors.toList());
            List<Long> missingIds = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new TransferException("Appointments not found with IDs: " + missingIds);
        }
        appointmentRepository.deleteAll(appointments);
    }
}
