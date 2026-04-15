package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.UserDto;
import co.za.mrhdigital.wpcbroadsheet.dto.UserRequest;
import co.za.mrhdigital.wpcbroadsheet.model.UserEntity;
import co.za.mrhdigital.wpcbroadsheet.model.UserRole;
import co.za.mrhdigital.wpcbroadsheet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getActiveUsers() {
        return userRepository.findAllByIsActiveTrueOrderByRoleAscNameAsc()
            .stream().map(this::toDto).toList();
    }

    public List<UserDto> getUnitManagers(String siteId) {
        if (siteId != null) {
            return userRepository
                .findAllByIsActiveTrueAndRoleAndSiteIdOrderByNameAsc(UserRole.UNIT_MANAGER, siteId)
                .stream().map(this::toDto).toList();
        }
        return userRepository
            .findAllByIsActiveTrueAndRoleOrderByNameAsc(UserRole.UNIT_MANAGER)
            .stream().map(this::toDto).toList();
    }

    public UserDto getUser(String id) {
        return userRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    @Transactional
    public UserDto createUser(UserRequest req, String actor) {
        long now  = System.currentTimeMillis();
        UserRole role = UserRole.valueOf(req.getRole());

        if (userRepository.findByEmailAndIsActiveTrue(req.getEmail().toLowerCase()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Email already in use: " + req.getEmail());
        }

        String hash = sha256(req.getPassword());
        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID().toString())
            .name(req.getName().trim())
            .email(req.getEmail().trim().toLowerCase())
            .passwordHash(hash)
            .role(role)
            .phone(req.getPhone() == null ? "" : req.getPhone().trim())
            .siteId(role == UserRole.UNIT_MANAGER ? req.getSiteId() : null)
            .isActive(true)
            .createdAt(now)
            .createdBy(actor)
            .lastModifiedAt(now)
            .lastModifiedBy(actor)
            .build();

        return toDto(userRepository.save(entity));
    }

    @Transactional
    public UserDto updateUser(String id, UserRequest req, String actor) {
        long now  = System.currentTimeMillis();
        UserEntity existing = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        UserRole role = UserRole.valueOf(req.getRole());
        String hash = (req.getPassword() != null && !req.getPassword().isBlank())
            ? sha256(req.getPassword())
            : existing.getPasswordHash();

        existing.setName(req.getName().trim());
        existing.setEmail(req.getEmail().trim().toLowerCase());
        existing.setPasswordHash(hash);
        existing.setRole(role);
        existing.setPhone(req.getPhone() == null ? "" : req.getPhone().trim());
        existing.setSiteId(role == UserRole.UNIT_MANAGER ? req.getSiteId() : null);
        existing.setLastModifiedAt(now);
        existing.setLastModifiedBy(actor);

        return toDto(userRepository.save(existing));
    }

    @Transactional
    public void deactivateUser(String id, String actor) {
        userRepository.deactivate(id, System.currentTimeMillis(), actor);
    }

    @Transactional
    public void relocateUnitManager(String id, String newSiteId, String actor) {
        userRepository.relocate(id, newSiteId, System.currentTimeMillis(), actor);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public String sha256(String input) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    public UserDto toDto(UserEntity e) {
        return new UserDto(
            e.getId(), e.getName(), e.getEmail(),
            e.getRole().name(), e.getPhone(), e.getSiteId(),
            e.isActive(),
            e.getCreatedAt(), e.getCreatedBy(),
            e.getLastModifiedAt(), e.getLastModifiedBy()
        );
    }
}
