package tn.gti.E_Transfert.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.gti.E_Transfert.enums.UserRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "Email is required", groups = {Create.class, Update.class})
    @Email(message = "Invalid email format", groups = {Create.class, Update.class})
    private String email;

    @NotBlank(message = "Password is required", groups = {Create.class})
    @Size(min = 6, message = "Password must be at least 6 characters", groups = {Create.class})
    @Size(min = 6, message = "Password must be at least 6 characters", groups = {Update.class})
    private String password;

    @NotBlank(message = "First name is required", groups = {Create.class})
    @Size(max = 50, message = "First name must not exceed 50 characters", groups = {Create.class, Update.class})
    private String firstName;

    @NotBlank(message = "Last name is required", groups = {Create.class})
    @Size(max = 50, message = "Last name must not exceed 50 characters", groups = {Create.class, Update.class})
    private String lastName;

    @NotBlank(message = "Phone number is required", groups = {Create.class})
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number", groups = {Create.class, Update.class})
    private String phoneNumber;

    @NotNull(message = "Role is required", groups = {Create.class})
    private UserRole role; // Required for create, optional for update

    // Validation groups
    public interface Create {}
    public interface Update {}
}