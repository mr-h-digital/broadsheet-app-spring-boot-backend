package co.za.mrhdigital.wpcbroadsheet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SiteEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

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
