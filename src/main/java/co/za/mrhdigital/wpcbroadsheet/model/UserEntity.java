package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email",  columnList = "email",  unique = true),
        @Index(name = "idx_users_role",   columnList = "role"),
        @Index(name = "idx_users_siteId", columnList = "siteId")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** SHA-256 hex digest of the password — matches the Android app hashing. */
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String phone = "";

    /** null for ADMIN and OPERATIONS_MANAGER; siteId for UNIT_MANAGER. */
    @Column
    private String siteId;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private long lastModifiedAt;

    @Column(nullable = false)
    private String lastModifiedBy;
}
