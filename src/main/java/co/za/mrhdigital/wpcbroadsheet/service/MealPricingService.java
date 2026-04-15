package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.MealPricingDto;
import co.za.mrhdigital.wpcbroadsheet.model.MealPricingEntity;
import co.za.mrhdigital.wpcbroadsheet.repository.MealPricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPricingService {

    private final MealPricingRepository pricingRepository;

    public MealPricingDto getPricing(String siteId, int year, int month) {
        return pricingRepository.findBySiteIdAndYearAndMonth(siteId, year, month)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No pricing config found for site=%s year=%d month=%d".formatted(siteId, year, month)
            ));
    }

    public List<MealPricingDto> getPricingHistory(String siteId) {
        return pricingRepository.findAllBySiteIdOrderByYearDescMonthDesc(siteId)
            .stream().map(this::toDto).toList();
    }

    @Transactional
    public MealPricingDto upsertPricing(MealPricingDto dto, String actor) {
        long now = System.currentTimeMillis();
        MealPricingEntity entity = toEntity(dto, now, actor);
        // Mark as synced since the backend just received it
        entity.setLastSyncedAt(now);
        return toDto(pricingRepository.save(entity));
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    public MealPricingDto toDto(MealPricingEntity e) {
        return new MealPricingDto(
            e.getSiteId(), e.getYear(), e.getMonth(),
            e.getCourse1(), e.getCourse2(), e.getCourse3(), e.getFullBoard(),
            e.getSun1Course(), e.getSun3Course(), e.getBreakfast(), e.getDinner(),
            e.getSoupDessert(), e.getVisitorMonSat(), e.getVisitorSun1(), e.getVisitorSun3(),
            e.getTaBakkies(), e.getVatRate(), e.getCompulsoryMealsDeduction(),
            e.getLastModifiedAt(), e.getLastModifiedBy()
        );
    }

    private MealPricingEntity toEntity(MealPricingDto dto, long now, String actor) {
        return MealPricingEntity.builder()
            .siteId(dto.getSiteId())
            .year(dto.getYear())
            .month(dto.getMonth())
            .course1(dto.getCourse1())
            .course2(dto.getCourse2())
            .course3(dto.getCourse3())
            .fullBoard(dto.getFullBoard())
            .sun1Course(dto.getSun1Course())
            .sun3Course(dto.getSun3Course())
            .breakfast(dto.getBreakfast())
            .dinner(dto.getDinner())
            .soupDessert(dto.getSoupDessert())
            .visitorMonSat(dto.getVisitorMonSat())
            .visitorSun1(dto.getVisitorSun1())
            .visitorSun3(dto.getVisitorSun3())
            .taBakkies(dto.getTaBakkies())
            .vatRate(dto.getVatRate())
            .compulsoryMealsDeduction(dto.getCompulsoryMealsDeduction())
            .lastModifiedAt(now)
            .lastModifiedBy(actor)
            .build();
    }
}
