package co.za.mrhdigital.wpcbroadsheet.repository;

import co.za.mrhdigital.wpcbroadsheet.model.ResidentAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentAuditRepository extends JpaRepository<ResidentAuditEntity, Long> {

    List<ResidentAuditEntity> findAllBySiteIdAndUnitNumberOrderByAtDesc(String siteId, String unitNumber);

    List<ResidentAuditEntity> findAllBySiteIdOrderByAtDesc(String siteId);
}
