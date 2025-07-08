package tn.gti.E_Transfert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gti.E_Transfert.dto.request.UserLoginDTO;
import tn.gti.E_Transfert.dto.request.UserRegisterDTO;
import tn.gti.E_Transfert.dto.request.UserUpdateDTO;
import tn.gti.E_Transfert.dto.response.UserResponseDTO;
import tn.gti.E_Transfert.entity.User;
import tn.gti.E_Transfert.enums.UserRole;
import tn.gti.E_Transfert.exception.TransferException;
import tn.gti.E_Transfert.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserResponseDTO registerUser(UserRegisterDTO registerDTO) {
        log.info("Registering user with email: {}", registerDTO.getEmail());
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("Email already exists: {}", registerDTO.getEmail());
            throw new TransferException("Email already exists: " + registerDTO.getEmail());
        }
        User user = modelMapper.map(registerDTO, User.class);
        user.setRole(UserRole.CLIENT); // Default role
        user.setActive(true);
        User saved = userRepository.save(user);
        log.debug("Registered user: {}", saved.getId());
        return modelMapper.map(saved, UserResponseDTO.class);
    }

    public UserResponseDTO authenticateUser(UserLoginDTO loginDTO) {
        log.info("Authenticating user with email: {}", loginDTO.getEmail());
        if (loginDTO.getEmail() == null || loginDTO.getEmail().isEmpty()) {
            log.error("Login attempt with empty email");
            throw new TransferException("Email is required");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            log.error("Login attempt with empty password");
            throw new TransferException("Password is required");
        }

        User user = userRepository.findByEmail(loginDTO.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("No user found with email: {}", loginDTO.getEmail());
                    return new TransferException("Invalid email or password");
                });

        // Simple password comparison (no encoding for now)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            log.warn("Password mismatch for user: {}", loginDTO.getEmail());
            throw new TransferException("Invalid email or password");
        }

        if (!user.isActive()) {
            log.warn("Login attempt for disabled account: {}", loginDTO.getEmail());
            throw new TransferException("User account is disabled");
        }

        log.info("User authenticated successfully: {}", user.getEmail());
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public List<UserResponseDTO> getAllUsers() {
        log.info("Retrieving all users");
        try {
            return userRepository.findAll().stream()
                    .map(user -> modelMapper.map(user, UserResponseDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to retrieve all users: {}", e.getMessage(), e);
            throw new TransferException("Failed to retrieve all users", e);
        }
    }

    public UserResponseDTO getUserById(Long id) {
        log.info("Retrieving user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new TransferException("User not found with ID: " + id));
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        log.info("Updating user with ID: {}", id);
        try {
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));

            // Update only non-null fields from DTO
            modelMapper.map(updateDTO, existing);

            // Ensure email uniqueness if email is being updated
            if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(existing.getEmail())) {
                if (userRepository.existsByEmail(updateDTO.getEmail())) {
                    log.warn("Email already exists: {}", updateDTO.getEmail());
                    throw new TransferException("Email already exists: " + updateDTO.getEmail());
                }
            }

            User updated = userRepository.save(existing);
            log.debug("Updated user: {}", updated.getId());
            return modelMapper.map(updated, UserResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to update user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to update user with ID: " + id, e);
        }
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        try {
            if (!userRepository.existsById(id)) {
                throw new TransferException("User not found with ID: " + id);
            }
            userRepository.deleteById(id);
            log.debug("Deleted user with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to delete user with ID: " + id, e);
        }
    }
}