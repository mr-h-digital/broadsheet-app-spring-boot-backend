package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Persisted resident record. Rows are NEVER hard-deleted — use isActive=false
 * for soft deactivation so billing history remains intact and auditable.
 * Composite PK: (siteId, unitNumber).
 */
@Entity
@Table(name = "residents")
@IdClass(ResidentId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResidentEntity {

    @Id
    @Column(nullable = false)
    private String siteId;

    @Id
    @Column(nullable = false)
    private String unitNumber;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private int totalOccupants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResidentType residentType;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private long createdAt;           // epoch millis

    @Column(nullable = false)
    private String lastModifiedBy;

    @Column(nullable = false)
    private long lastModifiedAt;      // epoch millis

    @Column
    private String deactivatedBy;

    @Column
    private Long deactivatedAt;       // epoch millis, null if still active
}
