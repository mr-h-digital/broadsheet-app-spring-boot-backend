package co.za.mrhdigital.wpcbroadsheet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private String role;          // UserRole.name()

    private String phone = "";

    private String siteId;        // required for UNIT_MANAGER

    /** Plain-text password — only required on create; optional on update. */
    private String password;
}
