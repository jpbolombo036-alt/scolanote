package com.bulletin.service;

import com.bulletin.dto.school.PeriodRequest;
import com.bulletin.dto.school.PeriodResponse;
import com.bulletin.entity.Period;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.PeriodMapper;
import com.bulletin.repository.PeriodRepository;
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
public class PeriodService {
    private final PeriodRepository periodRepository;
    private final TrimesterRepository trimesterRepository;
    private final PeriodMapper periodMapper;

    @Transactional
    public PeriodResponse createPeriod(PeriodRequest request) {
        Period period = periodMapper.toEntity(request);
        period.setTrimester(findTrimester(request.getTrimesterId()));
        Period saved = periodRepository.save(period);
        log.info("Période créée: {}", saved.getId());
        return periodMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PeriodResponse getPeriod(Long id) {
        return periodMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<PeriodResponse> getAllPeriods(Pageable pageable) {
        return periodRepository.findAll(pageable)
                .map(period -> {
                    if (period.getTrimester() == null) {
                        return null;
                    }
                    return periodMapper.toResponse(period);
                });
    }

    @Transactional(readOnly = true)
    public List<PeriodResponse> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(period -> {
                    if (period.getTrimester() == null) {
                        return null;
                    }
                    return periodMapper.toResponse(period);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeriodResponse> getPeriodsByTrimester(Long trimesterId) {
        return periodRepository.findByTrimesterId(trimesterId).stream()
                .map(period -> {
                    if (period.getTrimester() == null) {
                        return null;
                    }
                    return periodMapper.toResponse(period);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeriodResponse> getVerrouillees() {
        return periodRepository.findByVerrouilleTrue().stream()
                .map(period -> {
                    if (period.getTrimester() == null) {
                        return null;
                    }
                    return periodMapper.toResponse(period);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeriodResponse> getOuvertes() {
        return periodRepository.findByVerrouilleFalse().stream()
                .map(period -> {
                    if (period.getTrimester() == null) {
                        return null;
                    }
                    return periodMapper.toResponse(period);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public PeriodResponse updatePeriod(Long id, PeriodRequest request) {
        Period period = findById(id);
        periodMapper.updateEntity(request, period);
        period.setTrimester(findTrimester(request.getTrimesterId()));
        Period saved = periodRepository.save(period);
        log.info("Période mise à jour: {}", saved.getId());
        return periodMapper.toResponse(saved);
    }

    @Transactional
    public void deletePeriod(Long id) {
        Period period = findById(id);
        period.setDeletedAt(java.time.LocalDateTime.now());
        periodRepository.save(period);
        log.info("Période supprimée (soft): {}", id);
    }

    public Period findById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + id));
    }

    private Trimester findTrimester(Long id) {
        return trimesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
    }
}
