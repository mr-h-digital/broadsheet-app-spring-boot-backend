package co.za.mrhdigital.wpcbroadsheet.model;

import java.io.Serializable;
import lombok.*;

/**
 * Composite PK for MealPricingEntity: (siteId, year, month).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class MealPricingId implements Serializable {
    private String siteId;
    private int year;
    private int month;
}
