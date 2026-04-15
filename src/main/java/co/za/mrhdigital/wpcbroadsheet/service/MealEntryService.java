package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.MealEntryDto;
import co.za.mrhdigital.wpcbroadsheet.model.MealEntryEntity;
import co.za.mrhdigital.wpcbroadsheet.model.MealEntryId;
import co.za.mrhdigital.wpcbroadsheet.repository.MealEntryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MealEntryService {

    private final MealEntryRepository entryRepository;
    private final ObjectMapper objectMapper;

    public List<MealEntryDto> getEntriesForMonth(String siteId, int year, int month) {
        return entryRepository
            .findAllBySiteIdAndYearAndMonthOrderByUnitNumber(siteId, year, month)
            .stream().map(this::toDto).toList();
    }

    public MealEntryDto getEntry(String siteId, String unitNumber, int year, int month) {
        return entryRepository
            .findBySiteIdAndUnitNumberAndYearAndMonth(siteId, unitNumber, year, month)
            .map(this::toDto)
            .orElse(emptyEntry(siteId, unitNumber, year, month));
    }

    @Transactional
    public MealEntryDto upsertEntry(MealEntryDto dto, String actor) {
        long now = System.currentTimeMillis();

        // Read-merge-upsert: preserve existing counts and merge on top
        Map<String, Integer> existing = entryRepository
            .findBySiteIdAndUnitNumberAndYearAndMonth(
                dto.getSiteId(), dto.getUnitNumber(), dto.getYear(), dto.getMonth()
            )
            .map(e -> parseCounts(e.getCountsJson()))
            .orElse(new HashMap<>());

        if (dto.getCounts() != null) {
            existing.putAll(dto.getCounts());
        }

        MealEntryEntity entity = MealEntryEntity.builder()
            .siteId(dto.getSiteId())
            .unitNumber(dto.getUnitNumber())
            .year(dto.getYear())
            .month(dto.getMonth())
            .countsJson(toJson(existing))
            .lastModifiedAt(now)
            .lastModifiedBy(actor)
            .build();

        return toDto(entryRepository.save(entity));
    }

    @Transactional
    public MealEntryDto replaceEntry(MealEntryDto dto, String actor) {
        long now = System.currentTimeMillis();

        MealEntryEntity entity = MealEntryEntity.builder()
            .siteId(dto.getSiteId())
            .unitNumber(dto.getUnitNumber())
            .year(dto.getYear())
            .month(dto.getMonth())
            .countsJson(toJson(dto.getCounts() != null ? dto.getCounts() : Map.of()))
            .lastModifiedAt(now)
            .lastModifiedBy(actor)
            .build();

        return toDto(entryRepository.save(entity));
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private MealEntryDto toDto(MealEntryEntity e) {
        return new MealEntryDto(
            e.getSiteId(),
            e.getUnitNumber(),
            e.getYear(),
            e.getMonth(),
            parseCounts(e.getCountsJson()),
            e.getLastModifiedAt(),
            e.getLastModifiedBy()
        );
    }

    private MealEntryDto emptyEntry(String siteId, String unitNumber, int year, int month) {
        return new MealEntryDto(siteId, unitNumber, year, month, Map.of(), 0L, "");
    }

    private Map<String, Integer> parseCounts(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String toJson(Map<String, Integer> counts) {
        try {
            return objectMapper.writeValueAsString(counts);
        } catch (Exception e) {
            return "{}";
        }
    }
}
