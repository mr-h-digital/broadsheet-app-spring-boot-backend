package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentAuditDto {
    private Long id;
    private String siteId;
    private String unitNumber;
    private String action;     // AuditAction.name()
    private String actor;
    private long at;
    private String fromSiteId;
    private String toSiteId;
    private String note;
}
