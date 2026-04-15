package co.za.mrhdigital.wpcbroadsheet.model;

import java.io.Serializable;
import lombok.*;

/**
 * Composite PK for MealEntryEntity: (siteId, unitNumber, year, month).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class MealEntryId implements Serializable {
    private String siteId;
    private String unitNumber;
    private int year;
    private int month;
}
