package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDto {
    private String siteId;
    private String unitNumber;
    private String clientName;
    private int totalOccupants;
    private String residentType;   // ResidentType.name()
    private boolean isActive;
    private String createdBy;
    private long createdAt;
    private String lastModifiedBy;
    private long lastModifiedAt;
    private String deactivatedBy;
    private Long deactivatedAt;
}
