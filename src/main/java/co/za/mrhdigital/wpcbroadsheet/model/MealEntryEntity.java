package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Persisted monthly meal aggregate per resident per site.
 * Composite PK: (siteId, unitNumber, year, month).
 *
 * countsJson stores the Map&lt;MealType, Integer&gt; as a JSON object, e.g.:
 *   {"COURSE_1":12,"COURSE_2":4,"TA_BAKKIES":3}
 */
@Entity
@Table(
    name = "meal_entries",
    indexes = {
        @Index(name = "idx_entries_site_period", columnList = "site_id, entry_year, entry_month"),
        @Index(name = "idx_entries_unit",         columnList = "unit_number")
    }
)
@IdClass(MealEntryId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MealEntryEntity {

    @Id
    @Column(nullable = false)
    private String siteId;

    @Id
    @Column(nullable = false)
    private String unitNumber;

    @Id
    @Column(name = "entry_year", nullable = false)
    private int year;

    @Id
    @Column(name = "entry_month", nullable = false)
    private int month;      // 1–12

    /** JSON-serialised Map<MealType, Integer> */
    @Column(nullable = false, length = 4096)
    private String countsJson;

    /** Epoch millis of last write. */
    @Column(nullable = false)
    private long lastModifiedAt;

    @Column(nullable = false)
    private String lastModifiedBy;
}
