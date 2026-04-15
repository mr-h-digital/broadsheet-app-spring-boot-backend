package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Matches the Android ApiUser DTO exactly.
 * role is the UserRole enum name string (e.g. "ADMIN", "UNIT_MANAGER").
 */
@Data
@AllArgsConstructor
public class ApiUser {
    private String id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String siteId;     // null for ADMIN / OPERATIONS_MANAGER
    private String avatarUrl;  // null until cloud storage is wired
}
