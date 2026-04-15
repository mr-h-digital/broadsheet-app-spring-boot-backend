package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.ResidentAuditDto;
import co.za.mrhdigital.wpcbroadsheet.dto.ResidentDto;
import co.za.mrhdigital.wpcbroadsheet.dto.ResidentRequest;
import co.za.mrhdigital.wpcbroadsheet.model.*;
import co.za.mrhdigital.wpcbroadsheet.repository.ResidentAuditRepository;
import co.za.mrhdigital.wpcbroadsheet.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final ResidentAuditRepository auditRepository;

    // ── Read ───────────────────────────────────────────────────────────────────

    public List<ResidentDto> getResidents(String siteId, boolean includeInactive) {
        List<ResidentEntity> entities;
        if (siteId != null) {
            entities = includeInactive
                ? residentRepository.findAllBySiteIdOrderByUnitNumber(siteId)
                : residentRepository.findAllBySiteIdAndIsActiveTrueOrderByUnitNumber(siteId);
        } else {
            entities = includeInactive
                ? residentRepository.findAll()
                : residentRepository.findAllByIsActiveTrueOrderByUnitNumber();
        }
        return entities.stream().map(this::toDto).toList();
    }

    public ResidentDto getResident(String siteId, String unitNumber) {
        return residentRepository.findBySiteIdAndUnitNumber(siteId, unitNumber)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Resident %s/%s not found".formatted(siteId, unitNumber)
            ));
    }

    public List<ResidentAuditDto> getAuditTrail(String siteId, String unitNumber) {
        return auditRepository
            .findAllBySiteIdAndUnitNumberOrderByAtDesc(siteId, unitNumber)
            .stream().map(this::toAuditDto).toList();
    }

    // ── Write ──────────────────────────────────────────────────────────────────

    @Transactional
    public ResidentDto createResident(ResidentRequest req, String actor) {
        long now = System.currentTimeMillis();

        if (residentRepository.findBySiteIdAndUnitNumber(req.getSiteId(), req.getUnitNumber()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Resident %s/%s already exists".formatted(req.getSiteId(), req.getUnitNumber())
            );
        }

        ResidentEntity entity = ResidentEntity.builder()
            .siteId(req.getSiteId())
            .unitNumber(req.getUnitNumber())
            .clientName(req.getClientName())
            .totalOccupants(req.getTotalOccupants())
            .residentType(ResidentType.valueOf(req.getResidentType()))
            .isActive(true)
            .createdBy(actor)
            .createdAt(now)
            .lastModifiedBy(actor)
            .lastModifiedAt(now)
            .build();

        residentRepository.save(entity);
        appendAudit(req.getSiteId(), req.getUnitNumber(), AuditAction.CREATED, actor, now, null, null, null);

        return toDto(entity);
    }

    @Transactional
    public ResidentDto updateResident(String siteId, String unitNumber, ResidentRequest req, String actor) {
        long now = System.currentTimeMillis();
        ResidentEntity existing = residentRepository.findBySiteIdAndUnitNumber(siteId, unitNumber)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Resident %s/%s not found".formatted(siteId, unitNumber)
            ));

        ResidentEntity updated = ResidentEntity.builder()
            .siteId(existing.getSiteId())
            .unitNumber(existing.getUnitNumber())
            .clientName(req.getClientName())
            .totalOccupants(req.getTotalOccupants())
            .residentType(ResidentType.valueOf(req.getResidentType()))
            .isActive(existing.isActive())
            .createdBy(existing.getCreatedBy())
            .createdAt(existing.getCreatedAt())
            .lastModifiedBy(actor)
            .lastModifiedAt(now)
            .deactivatedBy(existing.getDeactivatedBy())
            .deactivatedAt(existing.getDeactivatedAt())
            .build();

        residentRepository.save(updated);
        appendAudit(siteId, unitNumber, AuditAction.UPDATED, actor, now,
            null, null, "Updated resident details");

        return toDto(updated);
    }

    @Transactional
    public void deactivateResident(String siteId, String unitNumber, String note, String actor) {
        long now = System.currentTimeMillis();
        residentRepository.setActive(siteId, unitNumber, false, actor, now, actor, now);
        appendAudit(siteId, unitNumber, AuditAction.DEACTIVATED, actor, now, null, null, note);
    }

    @Transactional
    public void reactivateResident(String siteId, String unitNumber, String actor) {
        long now = System.currentTimeMillis();
        residentRepository.setActive(siteId, unitNumber, true, actor, now, null, null);
        appendAudit(siteId, unitNumber, AuditAction.REACTIVATED, actor, now, null, null, null);
    }

    @Transactional
    public ResidentDto relocateResident(
        String fromSiteId, String unitNumber, String toSiteId, String actor
    ) {
        long now = System.currentTimeMillis();

        ResidentEntity existing = residentRepository.findBySiteIdAndUnitNumber(fromSiteId, unitNumber)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Resident %s/%s not found".formatted(fromSiteId, unitNumber)
            ));

        // Append audit under the OLD site before the row moves
        appendAudit(fromSiteId, unitNumber, AuditAction.RELOCATED, actor, now,
            fromSiteId, toSiteId, null);

        // Hard-delete old row (composite PK changes)
        residentRepository.hardDelete(fromSiteId, unitNumber);

        // Insert new row with updated siteId
        ResidentEntity relocated = ResidentEntity.builder()
            .siteId(toSiteId)
            .unitNumber(unitNumber)
            .clientName(existing.getClientName())
            .totalOccupants(existing.getTotalOccupants())
            .residentType(existing.getResidentType())
            .isActive(true)
            .createdBy(existing.getCreatedBy())
            .createdAt(existing.getCreatedAt())
            .lastModifiedBy(actor)
            .lastModifiedAt(now)
            .build();

        residentRepository.save(relocated);
        return toDto(relocated);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void appendAudit(
        String siteId, String unitNumber, AuditAction action,
        String actor, long at,
        String fromSiteId, String toSiteId, String note
    ) {
        auditRepository.save(ResidentAuditEntity.builder()
            .siteId(siteId)
            .unitNumber(unitNumber)
            .action(action)
            .actor(actor)
            .at(at)
            .fromSiteId(fromSiteId)
            .toSiteId(toSiteId)
            .note(note)
            .build());
    }

    public ResidentDto toDto(ResidentEntity e) {
        return new ResidentDto(
            e.getSiteId(), e.getUnitNumber(), e.getClientName(),
            e.getTotalOccupants(), e.getResidentType().name(),
            e.isActive(),
            e.getCreatedBy(), e.getCreatedAt(),
            e.getLastModifiedBy(), e.getLastModifiedAt(),
            e.getDeactivatedBy(), e.getDeactivatedAt()
        );
    }

    private ResidentAuditDto toAuditDto(ResidentAuditEntity e) {
        return new ResidentAuditDto(
            e.getId(), e.getSiteId(), e.getUnitNumber(),
            e.getAction().name(), e.getActor(), e.getAt(),
            e.getFromSiteId(), e.getToSiteId(), e.getNote()
        );
    }
}
