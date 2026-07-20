package com.bulletin.service;

import com.bulletin.dto.curriculum.CurriculumSubjectRequest;
import com.bulletin.dto.curriculum.CurriculumSubjectResponse;
import com.bulletin.entity.Curriculum;
import com.bulletin.entity.CurriculumSubject;
import com.bulletin.entity.Subject;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.CurriculumSubjectMapper;
import com.bulletin.repository.CurriculumRepository;
import com.bulletin.repository.CurriculumSubjectRepository;
import com.bulletin.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumSubjectService {

    private final CurriculumSubjectRepository curriculumSubjectRepository;
    private final CurriculumRepository curriculumRepository;
    private final SubjectRepository subjectRepository;
    private final CurriculumSubjectMapper curriculumSubjectMapper;

    @Transactional
    public CurriculumSubjectResponse createCurriculumSubject(CurriculumSubjectRequest request) {
        CurriculumSubject curriculumSubject = curriculumSubjectMapper.toEntity(request);
        curriculumSubject.setCurriculum(findCurriculum(request.getCurriculumId()));
        curriculumSubject.setSubject(findSubject(request.getSubjectId()));
        CurriculumSubject saved = curriculumSubjectRepository.save(curriculumSubject);
        log.info("Matière de programme créée: {}", saved.getId());
        return curriculumSubjectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CurriculumSubjectResponse getCurriculumSubject(Long id) {
        return curriculumSubjectMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getAllCurriculumSubjects() {
        return curriculumSubjectRepository.findAll().stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getByCurriculum(Long curriculumId) {
        return curriculumSubjectRepository.findByCurriculumId(curriculumId).stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getBySubject(Long subjectId) {
        return curriculumSubjectRepository.findBySubjectId(subjectId).stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional
    public CurriculumSubjectResponse updateCurriculumSubject(Long id, CurriculumSubjectRequest request) {
        CurriculumSubject curriculumSubject = findById(id);
        curriculumSubjectMapper.updateEntity(request, curriculumSubject);
        curriculumSubject.setCurriculum(findCurriculum(request.getCurriculumId()));
        curriculumSubject.setSubject(findSubject(request.getSubjectId()));
        CurriculumSubject saved = curriculumSubjectRepository.save(curriculumSubject);
        log.info("Matière de programme mise à jour: {}", saved.getId());
        return curriculumSubjectMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCurriculumSubject(Long id) {
        CurriculumSubject curriculumSubject = findById(id);
        curriculumSubjectRepository.delete(curriculumSubject);
        log.info("Matière de programme supprimée: {}", id);
    }

    public CurriculumSubject findById(Long id) {
        return curriculumSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière de programme non trouvée avec l'ID: " + id));
    }

    private Curriculum findCurriculum(Long id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme non trouvé avec l'ID: " + id));
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière non trouvée avec l'ID: " + id));
    }
}
