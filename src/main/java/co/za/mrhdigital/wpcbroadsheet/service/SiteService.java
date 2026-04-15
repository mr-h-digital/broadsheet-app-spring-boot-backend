package co.za.mrhdigital.wpcbroadsheet.service;

import co.za.mrhdigital.wpcbroadsheet.dto.SiteDto;
import co.za.mrhdigital.wpcbroadsheet.dto.SiteRequest;
import co.za.mrhdigital.wpcbroadsheet.model.SiteEntity;
import co.za.mrhdigital.wpcbroadsheet.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    public List<SiteDto> getActiveSites() {
        return siteRepository.findAllByIsActiveTrueOrderByName()
            .stream().map(this::toDto).toList();
    }

    public List<SiteDto> getAllSites() {
        return siteRepository.findAllByOrderByName()
            .stream().map(this::toDto).toList();
    }

    public SiteDto getSite(String id) {
        return siteRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found: " + id));
    }

    @Transactional
    public SiteDto createSite(SiteRequest req, String actor) {
        long now = System.currentTimeMillis();
        if (siteRepository.existsById(req.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Site already exists: " + req.getId());
        }
        SiteEntity entity = SiteEntity.builder()
            .id(req.getId())
            .name(req.getName())
            .isActive(true)
            .createdAt(now)
            .createdBy(actor)
            .lastModifiedAt(now)
            .lastModifiedBy(actor)
            .build();
        return toDto(siteRepository.save(entity));
    }

    @Transactional
    public SiteDto updateSite(String id, SiteRequest req, String actor) {
        SiteEntity existing = siteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found: " + id));
        long now = System.currentTimeMillis();
        existing.setName(req.getName());
        existing.setLastModifiedAt(now);
        existing.setLastModifiedBy(actor);
        return toDto(siteRepository.save(existing));
    }

    @Transactional
    public void deactivateSite(String id, String actor) {
        SiteEntity existing = siteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found: " + id));
        long now = System.currentTimeMillis();
        existing.setActive(false);
        existing.setLastModifiedAt(now);
        existing.setLastModifiedBy(actor);
        siteRepository.save(existing);
    }

    public SiteDto toDto(SiteEntity e) {
        return new SiteDto(
            e.getId(), e.getName(), e.isActive(),
            e.getCreatedAt(), e.getCreatedBy(),
            e.getLastModifiedAt(), e.getLastModifiedBy()
        );
    }
}
