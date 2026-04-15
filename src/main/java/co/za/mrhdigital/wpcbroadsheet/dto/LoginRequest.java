package co.za.mrhdigital.wpcbroadsheet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Matches the Android LoginRequest exactly.
 * password field carries the SHA-256 hex hash of the raw password.
 */
@Data
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    /** SHA-256 hex digest — hashed on the device before sending. */
    @NotBlank
    private String password;
}
