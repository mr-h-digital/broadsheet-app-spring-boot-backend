package co.za.mrhdigital.wpcbroadsheet.controller;

import co.za.mrhdigital.wpcbroadsheet.dto.ApiUser;
import co.za.mrhdigital.wpcbroadsheet.dto.LoginRequest;
import co.za.mrhdigital.wpcbroadsheet.dto.LoginResponse;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.repository.UserRepository;
import co.za.mrhdigital.wpcbroadsheet.security.JwtService;
import co.za.mrhdigital.wpcbroadsheet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Matches the Android WpcApiService exactly:
 *   POST  /api/auth/login   → LoginResponse
 *   GET   /api/auth/me      → ApiUser
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;

    /**
     * POST /api/auth/login
     *
     * Body:  { "email": "...", "password": "<sha256-hex>" }
     * Returns: { "token": "Bearer ...", "user": { ApiUser } }
     *
     * The Android app sends the password as a SHA-256 hex digest.
     * We compare directly with the stored hash (never store plaintext).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        ApiUser apiUser = toApiUser(user);
        return ResponseEntity.ok(new LoginResponse(token, apiUser));
    }

    /**
     * GET /api/auth/me
     *
     * Header: Authorization: Bearer &lt;token&gt;
     * Returns: ApiUser for the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiUser> me(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(toApiUser(user));
    }

    private ApiUser toApiUser(UserEntity u) {
        return new ApiUser(
            u.getId(), u.getName(), u.getEmail(),
            u.getRole().name(), u.getPhone(), u.getSiteId(),
            null  // avatarUrl — not yet implemented
        );
    }
}
