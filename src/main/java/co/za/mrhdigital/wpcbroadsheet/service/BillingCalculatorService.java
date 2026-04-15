package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.*;
import co.za.mrhdigital.wpcbroadsheet.model.MealType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure calculation engine — mirrors the Android BillingCalculator exactly.
 *
 * Logic:
 *  1. Sum monthly meal counts per meal type for each resident
 *  2. Calculate subtotal excl. VAT using per-meal prices
 *  3. Add VAT (15%)
 *  4. Add T/A Bakkies (flat R5/bakkie incl. VAT)
 *  5. Subtract 6 compulsory meals deduction (R246 fixed)
 *  6. Final = subtotal incl. VAT + bakkies − deduction
 *     (can be negative = credit for low-meal residents)
 */
@Service
public class BillingCalculatorService {

    public ResidentMonthlyBillingDto calculateResidentBilling(
        ResidentDto resident,
        List<MealEntryDto> entries,
        MealPricingDto pricing
    ) {
        // 1. Aggregate all entries for this resident into monthly totals
        Map<String, Integer> totalCounts = new HashMap<>();
        for (MealEntryDto entry : entries) {
            if (entry.getUnitNumber().equals(resident.getUnitNumber())
                && entry.getSiteId().equals(resident.getSiteId())) {
                entry.getCounts().forEach((type, count) ->
                    totalCounts.merge(type, count, Integer::sum)
                );
            }
        }

        // 2. Subtotal excl. VAT
        double subtotalExclVat = 0.0;
        for (Map.Entry<String, Integer> e : totalCounts.entrySet()) {
            subtotalExclVat += priceForType(e.getKey(), pricing) * e.getValue();
        }

        // 3. VAT on meals only (not on bakkies)
        double vat = subtotalExclVat * pricing.getVatRate();

        // 4. T/A Bakkies (already incl. VAT)
        int bakkiesCount = totalCounts.getOrDefault(MealType.TA_BAKKIES.name(), 0);
        double taBakkiesTotal = pricing.getTaBakkies() * bakkiesCount;

        // 5. Total meals (excluding bakkies from count)
        int totalMeals = totalCounts.entrySet().stream()
            .filter(e -> !e.getKey().equals(MealType.TA_BAKKIES.name()))
            .mapToInt(Map.Entry::getValue)
            .sum();

        // 6. Compulsory deduction
        double deduction = pricing.getCompulsoryMealsDeduction();

        // 7. Final total (can be negative = credit)
        double finalTotal = subtotalExclVat + vat + taBakkiesTotal - deduction;

        return new ResidentMonthlyBillingDto(
            resident,
            totalCounts,
            totalMeals,
            subtotalExclVat,
            vat,
            taBakkiesTotal,
            deduction,
            finalTotal,
            finalTotal < 0
        );
    }

    public SiteMonthlyReportDto calculateSiteReport(
        SiteDto site,
        List<ResidentDto> residents,
        List<MealEntryDto> entries,
        int year,
        int month,
        MealPricingDto pricing
    ) {
        List<ResidentMonthlyBillingDto> billings = residents.stream()
            .map(r -> calculateResidentBilling(r, entries, pricing))
            .toList();

        return new SiteMonthlyReportDto(
            site,
            year,
            month,
            billings,
            billings.stream().mapToInt(ResidentMonthlyBillingDto::getTotalMeals).sum(),
            billings.stream().mapToDouble(ResidentMonthlyBillingDto::getSubtotalExclVat).sum(),
            billings.stream().mapToDouble(ResidentMonthlyBillingDto::getVat).sum(),
            billings.stream().mapToDouble(ResidentMonthlyBillingDto::getTaBakkiesTotal).sum(),
            billings.stream().mapToDouble(ResidentMonthlyBillingDto::getFinalTotal).sum()
        );
    }

    private double priceForType(String mealTypeName, MealPricingDto p) {
        return switch (mealTypeName) {
            case "COURSE_1"        -> p.getCourse1();
            case "COURSE_2"        -> p.getCourse2();
            case "COURSE_3"        -> p.getCourse3();
            case "FULL_BOARD"      -> p.getFullBoard();
            case "SUN_1_COURSE"    -> p.getSun1Course();
            case "SUN_3_COURSE"    -> p.getSun3Course();
            case "BREAKFAST"       -> p.getBreakfast();
            case "DINNER"          -> p.getDinner();
            case "SOUP_DESSERT"    -> p.getSoupDessert();
            case "VISITOR_MON_SAT" -> p.getVisitorMonSat();
            case "VISITOR_SUN_1"   -> p.getVisitorSun1();
            case "VISITOR_SUN_3"   -> p.getVisitorSun3();
            case "TA_BAKKIES"      -> 0.0;  // handled separately
            default                -> 0.0;
        };
    }
}
