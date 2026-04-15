package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Matches the Android LoginResponse DTO exactly.
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private ApiUser user;
}
