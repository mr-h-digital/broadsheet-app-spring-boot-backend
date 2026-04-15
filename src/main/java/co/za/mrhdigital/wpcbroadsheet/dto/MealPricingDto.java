package co.za.mrhdigital.wpcbroadsheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPricingDto {
    private String siteId;
    private int year;
    private int month;
    private double course1;
    private double course2;
    private double course3;
    private double fullBoard;
    private double sun1Course;
    private double sun3Course;
    private double breakfast;
    private double dinner;
    private double soupDessert;
    private double visitorMonSat;
    private double visitorSun1;
    private double visitorSun3;
    private double taBakkies;
    private double vatRate;
    private double compulsoryMealsDeduction;
    private long lastModifiedAt;
    private String lastModifiedBy;
}
