package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.ResidentAuditDto;
import co.za.mrhdigital.wpcbroadsheet.dto.ResidentDto;
import co.za.mrhdigital.wpcbroadsheet.dto.ResidentRequest;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    /**
     * GET /api/residents?siteId=lizane&includeInactive=false
     */
    @GetMapping
    public ResponseEntity<List<ResidentDto>> getResidents(
        @RequestParam(required = false) String siteId,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(residentService.getResidents(siteId, includeInactive));
    }

    /** GET /api/residents/{siteId}/{unitNumber} */
    @GetMapping("/{siteId}/{unitNumber}")
    public ResponseEntity<ResidentDto> getResident(
        @PathVariable String siteId,
        @PathVariable String unitNumber
    ) {
        return ResponseEntity.ok(residentService.getResident(siteId, unitNumber));
    }

    /** GET /api/residents/{siteId}/{unitNumber}/audit */
    @GetMapping("/{siteId}/{unitNumber}/audit")
    public ResponseEntity<List<ResidentAuditDto>> getAuditTrail(
        @PathVariable String siteId,
        @PathVariable String unitNumber
    ) {
        return ResponseEntity.ok(residentService.getAuditTrail(siteId, unitNumber));
    }

    /** POST /api/residents */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','UNIT_MANAGER')")
    public ResponseEntity<ResidentDto> createResident(
        @Valid @RequestBody ResidentRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(residentService.createResident(req, actor.getEmail()));
    }

    /** PUT /api/residents/{siteId}/{unitNumber} */
    @PutMapping("/{siteId}/{unitNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','UNIT_MANAGER')")
    public ResponseEntity<ResidentDto> updateResident(
        @PathVariable String siteId,
        @PathVariable String unitNumber,
        @Valid @RequestBody ResidentRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(residentService.updateResident(siteId, unitNumber, req, actor.getEmail()));
    }

    /** POST /api/residents/{siteId}/{unitNumber}/deactivate */
    @PostMapping("/{siteId}/{unitNumber}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<Void> deactivateResident(
        @PathVariable String siteId,
        @PathVariable String unitNumber,
        @RequestBody(required = false) Map<String, String> body,
        @AuthenticationPrincipal UserEntity actor
    ) {
        String note = body != null ? body.get("note") : null;
        residentService.deactivateResident(siteId, unitNumber, note, actor.getEmail());
        return ResponseEntity.noContent().build();
    }

    /** POST /api/residents/{siteId}/{unitNumber}/reactivate */
    @PostMapping("/{siteId}/{unitNumber}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<Void> reactivateResident(
        @PathVariable String siteId,
        @PathVariable String unitNumber,
        @AuthenticationPrincipal UserEntity actor
    ) {
        residentService.reactivateResident(siteId, unitNumber, actor.getEmail());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/residents/{siteId}/{unitNumber}/relocate
     * Body: { "toSiteId": "bakkies" }
     */
    @PostMapping("/{siteId}/{unitNumber}/relocate")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<ResidentDto> relocateResident(
        @PathVariable String siteId,
        @PathVariable String unitNumber,
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal UserEntity actor
    ) {
        String toSiteId = body.get("toSiteId");
        return ResponseEntity.ok(
            residentService.relocateResident(siteId, unitNumber, toSiteId, actor.getEmail())
        );
    }
}
