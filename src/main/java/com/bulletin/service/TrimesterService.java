package com.bulletin.service;

import com.bulletin.dto.school.TrimesterRequest;
import com.bulletin.dto.school.TrimesterResponse;
import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TrimesterMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.TrimesterRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrimesterService {
    private final TrimesterRepository trimesterRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TrimesterMapper trimesterMapper;
    private final SecurityUtils securityUtils;
    private final AutoOrdreService autoOrdreService;

    private boolean isSuperAdmin() {
        return securityUtils.isSuperAdmin();
    }

    private Long requireSchoolId() {
        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            throw new SecurityException("École non définie pour l'utilisateur connecté");
        }
        return schoolId;
    }

    @Transactional
    public TrimesterResponse createTrimester(TrimesterRequest request) {
        Long schoolId = requireSchoolId();
        AcademicYear academicYear = findAcademicYear(request.getAcademicYearId());
        // L'ordre est calculé côté serveur (max + 1) : la valeur fournie par le client est ignorée.
        TrimesterResponse response = AutoOrdreRetry.retry(autoOrdreService,
                () -> trimesterRepository.maxOrdreByAcademicYearIdAndSchoolId(request.getAcademicYearId(), schoolId),
                ordre -> {
                    Trimester trimester = trimesterMapper.toEntity(request);
                    trimester.setAcademicYear(academicYear);
                    trimester.setSchoolId(schoolId);
                    trimester.setOrdre(ordre);
                    Trimester saved = trimesterRepository.save(trimester);
                    log.info("Trimestre créé: {}", saved.getId());
                    return trimesterMapper.toResponse(saved);
                });
        return response;
    }

    @Transactional(readOnly = true)
    public TrimesterResponse getTrimester(Long id) {
        return trimesterMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<TrimesterResponse> getAccessibleTrimesters(Pageable pageable) {
        if (isSuperAdmin()) {
            return trimesterRepository.findAll(pageable)
                    .map(trimester -> {
                        if (trimester.getAcademicYear() == null) {
                            return null;
                        }
                        return trimesterMapper.toResponse(trimester);
                    });
        }
        return trimesterRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(trimester -> {
                    if (trimester.getAcademicYear() == null) {
                        return null;
                    }
                    return trimesterMapper.toResponse(trimester);
                });
    }

    @Transactional(readOnly = true)
    public List<TrimesterResponse> getAccessibleTrimesters() {
        if (isSuperAdmin()) {
            return trimesterRepository.findAll().stream()
                    .map(trimester -> {
                        if (trimester.getAcademicYear() == null) {
                            return null;
                        }
                        return trimesterMapper.toResponse(trimester);
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return trimesterRepository.findBySchoolId(requireSchoolId()).stream()
                .map(trimester -> {
                    if (trimester.getAcademicYear() == null) {
                        return null;
                    }
                    return trimesterMapper.toResponse(trimester);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrimesterResponse> getTrimestersByAcademicYear(Long academicYearId) {
        return trimesterRepository.findByAcademicYearId(academicYearId).stream()
                .map(trimester -> {
                    if (trimester.getAcademicYear() == null) {
                        return null;
                    }
                    return trimesterMapper.toResponse(trimester);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public TrimesterResponse updateTrimester(Long id, TrimesterRequest request) {
        Trimester trimester = findById(id);
        Integer existingOrdre = trimester.getOrdre();
        trimesterMapper.updateEntity(request, trimester);
        trimester.setOrdre(existingOrdre); // l'ordre est immuable après création
        trimester.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        Trimester saved = trimesterRepository.save(trimester);
        log.info("Trimestre mis à jour: {}", saved.getId());
        return trimesterMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTrimester(Long id) {
        Trimester trimester = findById(id);
        trimester.setDeletedAt(java.time.LocalDateTime.now());
        trimesterRepository.save(trimester);
        log.info("Trimestre supprimé (soft): {}", id);
    }

    public Trimester findById(Long id) {
        Trimester trimester = trimesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(trimester.getSchoolId());
        return trimester;
    }

    private AcademicYear findAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + id));
    }
}
