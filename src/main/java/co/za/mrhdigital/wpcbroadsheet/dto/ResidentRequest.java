package co.za.mrhdigital.wpcbroadsheet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResidentRequest {

    @NotBlank
    private String siteId;

    @NotBlank
    private String unitNumber;

    @NotBlank
    private String clientName;

    @Min(1)
    private int totalOccupants;

    @NotNull
    private String residentType;  // Must match ResidentType enum name
}
