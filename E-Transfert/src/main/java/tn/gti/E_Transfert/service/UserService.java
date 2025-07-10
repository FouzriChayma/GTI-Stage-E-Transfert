package tn.gti.E_Transfert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.gti.E_Transfert.dto.request.UserLoginDTO;
import tn.gti.E_Transfert.dto.request.UserRequestDTO;
import tn.gti.E_Transfert.dto.response.UserResponseDTO;
import tn.gti.E_Transfert.entity.User;
import tn.gti.E_Transfert.enums.UserRole;
import tn.gti.E_Transfert.exception.TransferException;
import tn.gti.E_Transfert.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {
        log.info("Registering user with email: {}", requestDTO.getEmail());
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            log.warn("Email already exists: {}", requestDTO.getEmail());
            throw new TransferException("Email already exists: " + requestDTO.getEmail());
        }
        if (requestDTO.getRole() == UserRole.ADMINISTRATOR) {
            log.info("Creating user with ADMINISTRATOR role");
        }
        User user = modelMapper.map(requestDTO, User.class);
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

    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        log.info("Updating user with ID: {}", id);
        try {
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));

            if (requestDTO.getEmail() != null && !requestDTO.getEmail().equals(existing.getEmail())) {
                if (userRepository.existsByEmail(requestDTO.getEmail())) {
                    log.warn("Email already exists: {}", requestDTO.getEmail());
                    throw new TransferException("Email already exists: " + requestDTO.getEmail());
                }
            }

            modelMapper.map(requestDTO, existing);

            if (requestDTO.getRole() != null) {
                existing.setRole(requestDTO.getRole());
                log.debug("Updated role to: {}", requestDTO.getRole());
            }

            if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
                existing.setPassword(requestDTO.getPassword());
                log.debug("Updated password for user ID: {}", id);
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
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));
            if (user.getProfilePhotoPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(user.getProfilePhotoPath()));
                    log.info("Deleted profile photo from filesystem: {}", user.getProfilePhotoPath());
                } catch (IOException e) {
                    log.warn("Failed to delete profile photo from filesystem: {}", user.getProfilePhotoPath(), e);
                }
            }
            userRepository.deleteById(id);
            log.debug("Deleted user with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to delete user with ID: " + id, e);
        }
    }

    @Transactional
    public UserResponseDTO uploadProfilePhoto(Long id, MultipartFile file) {
        log.info("Uploading profile photo for user with ID: {}", id);
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));

            List<String> allowedTypes = List.of("image/png", "image/jpeg");
            if (file == null || file.isEmpty()) {
                log.error("File is null or empty");
                throw new TransferException("Uploaded file is empty or not provided");
            }
            if (!allowedTypes.contains(file.getContentType())) {
                log.error("Invalid file type: {}. Allowed types are: {}", file.getContentType(), allowedTypes);
                throw new TransferException("Invalid file type. Only PNG and JPEG are allowed");
            }

            Path uploadPath = Paths.get(uploadDir);
            String fileName = "user_" + id + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Delete old photo if exists
            if (user.getProfilePhotoPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(user.getProfilePhotoPath()));
                    log.info("Deleted old profile photo: {}", user.getProfilePhotoPath());
                } catch (IOException e) {
                    log.warn("Failed to delete old profile photo: {}", user.getProfilePhotoPath(), e);
                }
            }

            user.setProfilePhotoPath(filePath.toString());
            User updated = userRepository.save(user);
            return modelMapper.map(updated, UserResponseDTO.class);
        } catch (IOException e) {
            log.error("Failed to upload profile photo for user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to upload profile photo", e);
        }
    }

    public byte[] getProfilePhotoContent(Long id) {
        log.info("Retrieving profile photo for user with ID: {}", id);
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));
            if (user.getProfilePhotoPath() == null) {
                throw new TransferException("No profile photo found for user with ID: " + id);
            }
            Path path = Paths.get(user.getProfilePhotoPath());
            if (!Files.exists(path)) {
                log.error("Profile photo file does not exist: {}", user.getProfilePhotoPath());
                throw new TransferException("Profile photo file not found: " + user.getProfilePhotoPath());
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read profile photo for user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to read profile photo content", e);
        }
    }

    @Transactional
    public void deleteProfilePhoto(Long id) {
        log.info("Deleting profile photo for user with ID: {}", id);
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new TransferException("User not found with ID: " + id));
            if (user.getProfilePhotoPath() == null) {
                throw new TransferException("No profile photo found for user with ID: " + id);
            }
            try {
                Files.deleteIfExists(Paths.get(user.getProfilePhotoPath()));
                log.info("Deleted profile photo from filesystem: {}", user.getProfilePhotoPath());
            } catch (IOException e) {
                log.warn("Failed to delete profile photo from filesystem: {}", user.getProfilePhotoPath(), e);
            }
            user.setProfilePhotoPath(null);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Failed to delete profile photo for user with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to delete profile photo for user with ID: " + id, e);
        }
    }
}