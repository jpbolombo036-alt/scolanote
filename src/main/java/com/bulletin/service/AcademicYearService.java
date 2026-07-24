package com.bulletin.service;

import com.bulletin.dto.school.AcademicYearRequest;
import com.bulletin.dto.school.AcademicYearResponse;
import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.School;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AcademicYearMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.SchoolRepository;
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
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearMapper academicYearMapper;
    private final SecurityUtils securityUtils;

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
    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        AcademicYear academicYear = academicYearMapper.toEntity(request);
        academicYear.setSchool(findSchool(request.getSchoolId()));
        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Année scolaire créée: {}", saved.getId());
        return academicYearMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYear(Long id) {
        return academicYearMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<AcademicYearResponse> getAccessibleAcademicYears(Pageable pageable) {
        if (isSuperAdmin()) {
            return academicYearRepository.findAll(pageable)
                    .map(academicYearMapper::toResponse);
        }
        return academicYearRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(academicYearMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAccessibleAcademicYears() {
        if (isSuperAdmin()) {
            return academicYearRepository.findAll().stream()
                    .map(academicYearMapper::toResponse)
                    .toList();
        }
        return academicYearRepository.findBySchoolId(requireSchoolId()).stream()
                .map(academicYearMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAcademicYearsBySchool(Long schoolId) {
        return academicYearRepository.findBySchoolId(schoolId).stream()
                .map(academicYearMapper::toResponse)
                .toList();
    }

    @Transactional
    public AcademicYearResponse updateAcademicYear(Long id, AcademicYearRequest request) {
        AcademicYear academicYear = findById(id);
        academicYearMapper.updateEntity(request, academicYear);
        academicYear.setSchool(findSchool(request.getSchoolId()));
        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Année scolaire mise à jour: {}", saved.getId());
        return academicYearMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAcademicYear(Long id) {
        AcademicYear academicYear = findById(id);
        academicYear.setDeletedAt(java.time.LocalDateTime.now());
        academicYearRepository.save(academicYear);
        log.info("Année scolaire supprimée (soft): {}", id);
    }

    public AcademicYear findById(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + id));
    }

    private School findSchool(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + id));
    }
}
