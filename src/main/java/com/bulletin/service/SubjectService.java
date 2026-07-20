package com.bulletin.service;

import com.bulletin.dto.curriculum.SubjectRequest;
import com.bulletin.dto.curriculum.SubjectResponse;
import com.bulletin.entity.Subject;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.SubjectMapper;
import com.bulletin.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        Subject subject = subjectMapper.toEntity(request);
        Subject saved = subjectRepository.save(subject);
        log.info("Matière créée: {}", saved.getId());
        return subjectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubject(Long id) {
        return subjectMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toResponse)
                .toList();
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = findById(id);
        subjectMapper.updateEntity(request, subject);
        Subject saved = subjectRepository.save(subject);
        log.info("Matière mise à jour: {}", saved.getId());
        return subjectMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = findById(id);
        subject.setDeletedAt(java.time.LocalDateTime.now());
        subjectRepository.save(subject);
        log.info("Matière supprimée (soft): {}", id);
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière non trouvée avec l'ID: " + id));
    }
}
