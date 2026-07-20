package com.bulletin.service;

import com.bulletin.dto.school.TermRequest;
import com.bulletin.dto.school.TermResponse;
import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.Term;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TermMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TermService {

    private final TermRepository termRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TermMapper termMapper;

    @Transactional
    public TermResponse createTerm(TermRequest request) {
        Term term = termMapper.toEntity(request);
        term.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        Term saved = termRepository.save(term);
        log.info("Trimestre créé: {}", saved.getId());
        return termMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TermResponse getTerm(Long id) {
        return termMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<TermResponse> getAllTerms() {
        return termRepository.findAll().stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TermResponse> getTermsByAcademicYear(Long academicYearId) {
        return termRepository.findByAcademicYearId(academicYearId).stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @Transactional
    public TermResponse updateTerm(Long id, TermRequest request) {
        Term term = findById(id);
        termMapper.updateEntity(request, term);
        term.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        Term saved = termRepository.save(term);
        log.info("Trimestre mis à jour: {}", saved.getId());
        return termMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTerm(Long id) {
        Term term = findById(id);
        term.setDeletedAt(java.time.LocalDateTime.now());
        termRepository.save(term);
        log.info("Trimestre supprimé (soft): {}", id);
    }

    public Term findById(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
    }

    private AcademicYear findAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + id));
    }
}
