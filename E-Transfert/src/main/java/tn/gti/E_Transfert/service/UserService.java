package tn.gti.E_Transfert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import tn.gti.E_Transfert.util.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponseDTO registerUser(UserRegisterDTO registerDTO) {
        log.info("Registering user with email: {}", registerDTO.getEmail());
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new TransferException("Email already exists: " + registerDTO.getEmail());
        }
        User user = modelMapper.map(registerDTO, User.class);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole(UserRole.CLIENT); // Default role
        user.setActive(true);
        User saved = userRepository.save(user);
        log.debug("Registered user: {}", saved.getId());
        return modelMapper.map(saved, UserResponseDTO.class);
    }

    public String authenticateUser(UserLoginDTO loginDTO) {
        log.info("Authenticating user with email: {}", loginDTO.getEmail());
        if (loginDTO.getEmail() == null || loginDTO.getEmail().isEmpty()) {
            log.error("Login attempt with empty email");
            throw new TransferException("Email is required");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            log.error("Login attempt with empty password");
            throw new TransferException("Password is required");
        }

        // Find user by email (case-insensitive for flexibility)
        User user = userRepository.findByEmail(loginDTO.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("No user found with email: {}", loginDTO.getEmail());
                    return new TransferException("Invalid email or password");
                });

        // Verify password
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("Password mismatch for user: {}", loginDTO.getEmail());
            throw new TransferException("Invalid email or password");
        }

        // Check if account is active
        if (!user.isActive()) {
            log.warn("Login attempt for disabled account: {}", loginDTO.getEmail());
            throw new TransferException("User account is disabled");
        }

        // Generate JWT token
        try {
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
            log.info("Generated JWT token for user: {}", user.getEmail());
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", user.getEmail(), e);
            throw new TransferException("Failed to generate authentication token", e);
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        log.info("Retrieving all users");
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponseDTO.class))
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        log.info("Retrieving user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new TransferException("User not found with ID: " + id));
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new TransferException("User not found with ID: " + id));
        if (updateDTO.getEmail() != null && !user.getEmail().equals(updateDTO.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new TransferException("Email already exists: " + updateDTO.getEmail());
        }
        modelMapper.map(updateDTO, user);
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }
        User updated = userRepository.save(user);
        return modelMapper.map(updated, UserResponseDTO.class);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new TransferException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}