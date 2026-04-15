package co.za.mrhdigital.wpcbroadsheet.repository;

import co.za.mrhdigital.wpcbroadsheet.model.MealPricingEntity;
import co.za.mrhdigital.wpcbroadsheet.model.MealPricingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealPricingRepository extends JpaRepository<MealPricingEntity, MealPricingId> {

    Optional<MealPricingEntity> findBySiteIdAndYearAndMonth(String siteId, int year, int month);

    List<MealPricingEntity> findAllBySiteIdOrderByYearDescMonthDesc(String siteId);

    List<MealPricingEntity> findAllByLastSyncedAtIsNull();
}
