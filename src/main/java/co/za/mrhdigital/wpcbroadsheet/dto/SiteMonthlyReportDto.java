package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full monthly billing report for one site.
 * Mirrors the Android SiteMonthlyReport data class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteMonthlyReportDto {
    private SiteDto site;
    private int year;
    private int month;
    private List<ResidentMonthlyBillingDto> residentBillings;
    private int grandTotalMeals;
    private double grandSubtotal;
    private double grandVat;
    private double grandTaBakkies;
    private double grandTotal;
}
