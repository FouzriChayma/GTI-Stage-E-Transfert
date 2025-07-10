package tn.gti.E_Transfert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.gti.E_Transfert.dto.request.UserLoginDTO;
import tn.gti.E_Transfert.dto.request.UserRequestDTO;
import tn.gti.E_Transfert.dto.response.UserResponseDTO;
import tn.gti.E_Transfert.enums.UserRole;
import tn.gti.E_Transfert.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive) {
        List<UserResponseDTO> users = userService.searchUsers(email, firstName, lastName, phoneNumber, role, isActive);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO registerDTO) {
        UserResponseDTO response = userService.registerUser(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/users/{id}/profile-photo", consumes = {"multipart/form-data"})
    public ResponseEntity<UserResponseDTO> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        UserResponseDTO response = userService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/profile-photo")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long id) {
        byte[] photoContent = userService.getProfilePhotoContent(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Adjust based on file type if needed
        headers.setContentDispositionFormData("attachment", "profile_photo.jpg");
        return new ResponseEntity<>(photoContent, headers, HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}/profile-photo")
    public ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long id) {
        userService.deleteProfilePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserResponseDTO response = userService.authenticateUser(loginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDTO updateDTO) {
        return ResponseEntity.ok(userService.updateUser(id, updateDTO));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}