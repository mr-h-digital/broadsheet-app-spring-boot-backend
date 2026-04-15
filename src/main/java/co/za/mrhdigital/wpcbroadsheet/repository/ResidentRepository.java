package co.za.mrhdigital.wpcbroadsheet.repository;

import co.za.mrhdigital.wpcbroadsheet.model.ResidentEntity;
import co.za.mrhdigital.wpcbroadsheet.model.ResidentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<ResidentEntity, ResidentId> {

    List<ResidentEntity> findAllBySiteIdAndIsActiveTrueOrderByUnitNumber(String siteId);

    List<ResidentEntity> findAllBySiteIdOrderByUnitNumber(String siteId);

    List<ResidentEntity> findAllByIsActiveTrueOrderByUnitNumber();

    Optional<ResidentEntity> findBySiteIdAndUnitNumber(String siteId, String unitNumber);

    int countBySiteIdAndIsActiveTrue(String siteId);

    int countByIsActiveTrue();

    @Modifying
    @Query("""
        UPDATE ResidentEntity r
        SET r.isActive = :active,
            r.lastModifiedBy = :actor,
            r.lastModifiedAt = :at,
            r.deactivatedBy  = :deactivatedBy,
            r.deactivatedAt  = :deactivatedAt
        WHERE r.siteId = :siteId AND r.unitNumber = :unitNumber
    """)
    void setActive(
        @Param("siteId")        String siteId,
        @Param("unitNumber")    String unitNumber,
        @Param("active")        boolean active,
        @Param("actor")         String actor,
        @Param("at")            long at,
        @Param("deactivatedBy") String deactivatedBy,
        @Param("deactivatedAt") Long deactivatedAt
    );

    @Modifying
    @Query("DELETE FROM ResidentEntity r WHERE r.siteId = :siteId AND r.unitNumber = :unitNumber")
    void hardDelete(@Param("siteId") String siteId, @Param("unitNumber") String unitNumber);
}
