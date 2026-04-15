package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.UserDto;
import co.za.mrhdigital.wpcbroadsheet.dto.UserRequest;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users — all active users (Admin only) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    /** GET /api/users/unit-managers?siteId= (Admin/Ops Manager) */
    @GetMapping("/unit-managers")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    public ResponseEntity<List<UserDto>> getUnitManagers(
        @RequestParam(required = false) String siteId
    ) {
        return ResponseEntity.ok(userService.getUnitManagers(siteId));
    }

    /** GET /api/users/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    /** POST /api/users (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(
        @Valid @RequestBody UserRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.createUser(req, actor.getEmail()));
    }

    /** PUT /api/users/{id} (Admin or self for profile edits) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable String id,
        @Valid @RequestBody UserRequest req,
        @AuthenticationPrincipal UserEntity actor
    ) {
        return ResponseEntity.ok(userService.updateUser(id, req, actor.getEmail()));
    }

    /** DELETE /api/users/{id} — soft deactivation (Admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(
        @PathVariable String id,
        @AuthenticationPrincipal UserEntity actor
    ) {
        userService.deactivateUser(id, actor.getEmail());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/users/{id}/relocate
     * Body: { "newSiteId": "bakkies" }
     * Admin only.
     */
    @PostMapping("/{id}/relocate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> relocateUnitManager(
        @PathVariable String id,
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal UserEntity actor
    ) {
        userService.relocateUnitManager(id, body.get("newSiteId"), actor.getEmail());
        return ResponseEntity.noContent().build();
    }
}
