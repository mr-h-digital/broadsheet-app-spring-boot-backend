package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.SiteDto;
import co.za.mrhdigital.wpcbroadsheet.dto.SiteRequest;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    /** GET /api/sites — all active sites (all authenticated roles) */
    @GetMapping
    public ResponseEntity<List<SiteDto>> getActiveSites() {
        return ResponseEntity.ok(siteService.getActiveSites());
    }

    /** GET /api/sites/all — include inactive (Admin/Ops Manager only) */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<List<SiteDto>> getAllSites() {
        return ResponseEntity.ok(siteService.getAllSites());
    }

    /** GET /api/sites/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<SiteDto> getSite(@PathVariable String id) {
        return ResponseEntity.ok(siteService.getSite(id));
    }

    /** POST /api/sites (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> createSite(
        @Valid @RequestBody SiteRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(siteService.createSite(req, actor.getEmail()));
    }

    /** PUT /api/sites/{id} (Admin only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> updateSite(
        @PathVariable String id,
        @Valid @RequestBody SiteRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(siteService.updateSite(id, req, actor.getEmail()));
    }

    /** DELETE /api/sites/{id} — soft deactivation (Admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateSite(
        @PathVariable String id,
        @AuthenticationPrincipal UserEntity actor
    ) {
        siteService.deactivateSite(id, actor.getEmail());
        return ResponseEntity.noContent().build();
    }
}
