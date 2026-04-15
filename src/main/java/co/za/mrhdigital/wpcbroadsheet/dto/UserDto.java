package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String siteId;
    private boolean isActive;
    private long createdAt;
    private String createdBy;
    private long lastModifiedAt;
    private String lastModifiedBy;
}
