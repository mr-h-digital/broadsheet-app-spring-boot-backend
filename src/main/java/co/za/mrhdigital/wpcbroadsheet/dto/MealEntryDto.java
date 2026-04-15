package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * REST DTO for a meal entry.
 * counts is a Map from MealType name to integer count, e.g.:
 *   {"COURSE_1": 12, "COURSE_2": 4, "TA_BAKKIES": 3}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryDto {
    private String siteId;
    private String unitNumber;
    private int year;
    private int month;
    private Map<String, Integer> counts;  // key = MealType.name()
    private long lastModifiedAt;
    private String lastModifiedBy;
}
