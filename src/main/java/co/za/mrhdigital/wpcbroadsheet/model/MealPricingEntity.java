package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Persisted pricing configuration per site per billing period.
 * Composite PK: (siteId, year, month).
 * All prices stored EXCLUSIVE of VAT.
 */
@Entity
@Table(
    name = "meal_pricing",
    indexes = {
        @Index(name = "idx_pricing_site", columnList = "site_id")
    }
)
@IdClass(MealPricingId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MealPricingEntity {

    @Id
    @Column(nullable = false)
    private String siteId;

    @Id
    @Column(name = "pricing_year", nullable = false)
    private int year;

    @Id
    @Column(name = "pricing_month", nullable = false)
    private int month;      // 1–12

    // ── Meal prices excl. VAT ──────────────────────────────────────────────────
    @Column(nullable = false) private double course1;
    @Column(nullable = false) private double course2;
    @Column(nullable = false) private double course3;
    @Column(nullable = false) private double fullBoard;
    @Column(nullable = false) private double sun1Course;
    @Column(nullable = false) private double sun3Course;
    @Column(nullable = false) private double breakfast;
    @Column(nullable = false) private double dinner;
    @Column(nullable = false) private double soupDessert;
    @Column(nullable = false) private double visitorMonSat;
    @Column(nullable = false) private double visitorSun1;
    @Column(nullable = false) private double visitorSun3;
    @Column(nullable = false) private double taBakkies;
    @Column(nullable = false) private double vatRate;
    @Column(nullable = false) private double compulsoryMealsDeduction;

    // ── Audit ──────────────────────────────────────────────────────────────────
    @Column(nullable = false) private long lastModifiedAt;
    @Column(nullable = false) private String lastModifiedBy;

    /**
     * Null until this row has been successfully acknowledged by the backend.
     * Used to detect dirty configs that need syncing.
     */
    @Column
    private Long lastSyncedAt;
}
