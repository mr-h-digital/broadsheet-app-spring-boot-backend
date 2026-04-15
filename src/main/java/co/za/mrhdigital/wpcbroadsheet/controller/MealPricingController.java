package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.MealPricingDto;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.service.MealPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class MealPricingController {

    private final MealPricingService pricingService;

    /**
     * GET /api/pricing/{siteId}/{year}/{month}
     * Returns the pricing config for a specific site and billing period.
     */
    @GetMapping("/{siteId}/{year}/{month}")
    public ResponseEntity<MealPricingDto> getPricing(
        @PathVariable String siteId,
        @PathVariable int year,
        @PathVariable int month
    ) {
        return ResponseEntity.ok(pricingService.getPricing(siteId, year, month));
    }

    /**
     * GET /api/pricing/{siteId}/history
     * Returns all pricing configs for a site ordered newest first.
     */
    @GetMapping("/{siteId}/history")
    public ResponseEntity<List<MealPricingDto>> getPricingHistory(
        @PathVariable String siteId
    ) {
        return ResponseEntity.ok(pricingService.getPricingHistory(siteId));
    }

    /**
     * POST /api/pricing
     * Upsert a pricing config for a site+period.
     * Admin and Operations Manager only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<MealPricingDto> upsertPricing(
        @RequestBody MealPricingDto dto,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(pricingService.upsertPricing(dto, actor.getEmail()));
    }
}
