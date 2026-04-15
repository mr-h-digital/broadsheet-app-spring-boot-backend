package co.za.mrhdigital.wpcbroadsheet.repository;

import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailAndIsActiveTrue(String email);

    List<UserEntity> findAllByIsActiveTrueOrderByRoleAscNameAsc();

    List<UserEntity> findAllByIsActiveTrueAndRoleOrderByNameAsc(UserRole role);

    List<UserEntity> findAllByIsActiveTrueAndRoleAndSiteIdOrderByNameAsc(UserRole role, String siteId);

    long countBy();

    @Modifying
    @Query("UPDATE UserEntity u SET u.isActive = false, u.lastModifiedAt = :now, u.lastModifiedBy = :actor WHERE u.id = :id")
    void deactivate(@Param("id") String id, @Param("now") long now, @Param("actor") String actor);

    @Modifying
    @Query("UPDATE UserEntity u SET u.siteId = :newSiteId, u.lastModifiedAt = :now, u.lastModifiedBy = :actor WHERE u.id = :id")
    void relocate(@Param("id") String id, @Param("newSiteId") String newSiteId, @Param("now") long now, @Param("actor") String actor);
}
