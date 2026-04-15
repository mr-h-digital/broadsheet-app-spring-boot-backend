package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Calculated billing result for a single resident in one billing month.
 * Mirrors the Android ResidentMonthlyBilling data class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentMonthlyBillingDto {
    private ResidentDto resident;
    private Map<String, Integer> totalMealCounts;  // key = MealType.name()
    private int totalMeals;
    private double subtotalExclVat;
    private double vat;
    private double taBakkiesTotal;
    private double compulsoryDeduction;
    private double finalTotal;
    private boolean isCredit;
}
