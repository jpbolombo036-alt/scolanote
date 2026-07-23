package com.bulletin.service;

import com.bulletin.dto.school.TrimesterRequest;
import com.bulletin.dto.school.TrimesterResponse;
import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TrimesterMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.TrimesterRepository;
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

    @Transactional
    public TrimesterResponse createTrimester(TrimesterRequest request) {
        Trimester trimester = trimesterMapper.toEntity(request);
        trimester.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        Trimester saved = trimesterRepository.save(trimester);
        log.info("Trimestre créé: {}", saved.getId());
        return trimesterMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TrimesterResponse getTrimester(Long id) {
        return trimesterMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<TrimesterResponse> getAllTrimesters(Pageable pageable) {
        return trimesterRepository.findAll(pageable)
                .map(trimester -> {
                    if (trimester.getAcademicYear() == null) {
                        return null;
                    }
                    return trimesterMapper.toResponse(trimester);
                });
    }

    @Transactional(readOnly = true)
    public List<TrimesterResponse> getAllTrimesters() {
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
        trimesterMapper.updateEntity(request, trimester);
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
        return trimesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
    }

    private AcademicYear findAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + id));
    }
}
