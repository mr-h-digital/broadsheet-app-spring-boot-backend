package co.za.mrhdigital.wpcbroadsheet.model;

import java.io.Serializable;
import lombok.*;

/**
 * Composite primary key for ResidentEntity: (siteId, unitNumber).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ResidentId implements Serializable {
    private String siteId;
    private String unitNumber;
}
