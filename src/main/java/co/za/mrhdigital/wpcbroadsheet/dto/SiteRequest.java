package co.za.mrhdigital.wpcbroadsheet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiteRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String name;
}
