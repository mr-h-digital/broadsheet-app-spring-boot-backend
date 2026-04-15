package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Immutable audit log for resident lifecycle events.
 * A new row is appended for every mutation — rows are NEVER updated or deleted.
 */
@Entity
@Table(
    name = "resident_audit",
    indexes = {
        @Index(name = "idx_audit_site_unit", columnList = "siteId, unitNumber"),
        @Index(name = "idx_audit_action",    columnList = "action"),
        @Index(name = "idx_audit_actor",     columnList = "actor"),
        @Index(name = "idx_audit_at",        columnList = "at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResidentAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Site the resident belonged to at the time of the event. */
    @Column(nullable = false)
    private String siteId;

    @Column(nullable = false)
    private String unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    /** Username or email of the user who triggered the action. */
    @Column(nullable = false)
    private String actor;

    /** Epoch milliseconds. */
    @Column(nullable = false)
    private long at;

    /** Non-null only for RELOCATED events — site moved from. */
    @Column
    private String fromSiteId;

    /** Non-null only for RELOCATED events — site moved to. */
    @Column
    private String toSiteId;

    /** Optional free-text context (reason for deactivation, etc.). */
    @Column
    private String note;
}
