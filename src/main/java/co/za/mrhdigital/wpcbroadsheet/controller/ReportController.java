package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.*;
import co.za.mrhdigital.wpcbroadsheet.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SiteService siteService;
    private final ResidentService residentService;
    private final MealEntryService mealEntryService;
    private final MealPricingService pricingService;
    private final BillingCalculatorService billingCalculator;

    /**
     * GET /api/reports/{siteId}/{year}/{month}
     * Returns the full billing report for a site in a given month,
     * using BillingCalculatorService — mirrors Android BillingCalculator exactly.
     */
    @GetMapping("/{siteId}/{year}/{month}")
    public ResponseEntity<SiteMonthlyReportDto> getSiteMonthlyReport(
        @PathVariable String siteId,
        @PathVariable int year,
        @PathVariable int month
    ) {
        SiteDto site = siteService.getSite(siteId);

        List<ResidentDto> residents = residentService.getResidents(siteId, false);
        List<MealEntryDto> entries  = mealEntryService.getEntriesForMonth(siteId, year, month);

        MealPricingDto pricing;
        try {
            pricing = pricingService.getPricing(siteId, year, month);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "No pricing config for site=%s year=%d month=%d".formatted(siteId, year, month)
            );
        }

        SiteMonthlyReportDto report = billingCalculator.calculateSiteReport(
            site, residents, entries, year, month, pricing
        );
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/all/{year}/{month}
     * Consolidated report across ALL active sites for a billing period.
     */
    @GetMapping("/all/{year}/{month}")
    public ResponseEntity<List<SiteMonthlyReportDto>> getAllSitesReport(
        @PathVariable int year,
        @PathVariable int month
    ) {
        List<SiteMonthlyReportDto> reports = siteService.getActiveSites().stream()
            .map(site -> {
                List<ResidentDto> residents = residentService.getResidents(site.getId(), false);
                List<MealEntryDto> entries  = mealEntryService.getEntriesForMonth(site.getId(), year, month);
                MealPricingDto pricing;
                try {
                    pricing = pricingService.getPricing(site.getId(), year, month);
                } catch (ResponseStatusException e) {
                    // Return empty report if no pricing configured yet
                    return billingCalculator.calculateSiteReport(
                        site, residents, entries, year, month,
                        defaultPricing(site.getId(), year, month)
                    );
                }
                return billingCalculator.calculateSiteReport(site, residents, entries, year, month, pricing);
            })
            .toList();

        return ResponseEntity.ok(reports);
    }

    private MealPricingDto defaultPricing(String siteId, int year, int month) {
        // Lizane Village defaults — same as Android SampleData defaults
        return new MealPricingDto(
            siteId, year, month,
            35.652174, 42.608696, 54.782609, 81.739130,
            53.913043, 68.695652, 23.913043, 27.391304,
            13.043478, 45.217391, 63.478261, 76.521739,
            5.0, 0.15, 246.0,
            System.currentTimeMillis(), "system"
        );
    }
}
