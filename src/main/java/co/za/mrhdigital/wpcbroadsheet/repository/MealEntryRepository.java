package co.za.mrhdigital.wpcbroadsheet.repository;

import co.za.mrhdigital.wpcbroadsheet.model.MealEntryEntity;
import co.za.mrhdigital.wpcbroadsheet.model.MealEntryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntryEntity, MealEntryId> {

    List<MealEntryEntity> findAllBySiteIdAndYearAndMonthOrderByUnitNumber(
        String siteId, int year, int month
    );

    Optional<MealEntryEntity> findBySiteIdAndUnitNumberAndYearAndMonth(
        String siteId, String unitNumber, int year, int month
    );

    List<MealEntryEntity> findAllBySiteIdAndUnitNumberOrderByYearDescMonthDesc(
        String siteId, String unitNumber
    );

    List<MealEntryEntity> findAllBySiteIdAndYearAndMonthAndLastModifiedAtGreaterThan(
        String siteId, int year, int month, long syncedBefore
    );
}
