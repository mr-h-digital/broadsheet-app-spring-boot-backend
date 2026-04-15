package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.MealEntryDto;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.service.MealEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-entries")
@RequiredArgsConstructor
public class MealEntryController {

    private final MealEntryService mealEntryService;

    /**
     * GET /api/meal-entries/{siteId}/{year}/{month}
     * Returns all meal entries for a site in a given billing period.
     */
    @GetMapping("/{siteId}/{year}/{month}")
    public ResponseEntity<List<MealEntryDto>> getEntriesForMonth(
        @PathVariable String siteId,
        @PathVariable int year,
        @PathVariable int month
    ) {
        return ResponseEntity.ok(mealEntryService.getEntriesForMonth(siteId, year, month));
    }

    /**
     * GET /api/meal-entries/{siteId}/{unitNumber}/{year}/{month}
     * Returns the meal entry for a single resident in a given billing period.
     */
    @GetMapping("/{siteId}/{unitNumber}/{year}/{month}")
    public ResponseEntity<MealEntryDto> getEntry(
        @PathVariable String siteId,
        @PathVariable String unitNumber,
        @PathVariable int year,
        @PathVariable int month
    ) {
        return ResponseEntity.ok(mealEntryService.getEntry(siteId, unitNumber, year, month));
    }

    /**
     * POST /api/meal-entries — merge counts (read-merge-upsert)
     * Existing counts are merged with the incoming counts (incoming wins on collision).
     */
    @PostMapping
    public ResponseEntity<MealEntryDto> upsertEntry(
        @RequestBody MealEntryDto dto,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(mealEntryService.upsertEntry(dto, actor.getEmail()));
    }

    /**
     * PUT /api/meal-entries — full replace of counts for a resident/month.
     */
    @PutMapping
    public ResponseEntity<MealEntryDto> replaceEntry(
        @RequestBody MealEntryDto dto,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(mealEntryService.replaceEntry(dto, actor.getEmail()));
    }
}
