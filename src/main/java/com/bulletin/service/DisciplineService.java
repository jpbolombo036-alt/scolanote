package com.bulletin.service;

import com.bulletin.dto.tracking.DisciplineRequest;
import com.bulletin.dto.tracking.DisciplineResponse;
import com.bulletin.entity.Discipline;
import com.bulletin.entity.Student;
import com.bulletin.entity.Term;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.DisciplineMapper;
import com.bulletin.repository.DisciplineRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final StudentRepository studentRepository;
    private final TermRepository termRepository;
    private final DisciplineMapper disciplineMapper;

    @Transactional
    public DisciplineResponse createDiscipline(DisciplineRequest request) {
        Discipline discipline = disciplineMapper.toEntity(request);
        discipline.setStudent(findStudent(request.getStudentId()));
        discipline.setTerm(findTerm(request.getTermId()));
        Discipline saved = disciplineRepository.save(discipline);
        log.info("Discipline créée: {}", saved.getId());
        return disciplineMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DisciplineResponse getDiscipline(Long id) {
        return disciplineMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DisciplineResponse> getAllDisciplines() {
        return disciplineRepository.findAll().stream()
                .map(disciplineMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisciplineResponse> getByStudent(Long studentId) {
        return disciplineRepository.findByStudentId(studentId).stream()
                .map(disciplineMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisciplineResponse> getByTerm(Long termId) {
        return disciplineRepository.findByTermId(termId).stream()
                .map(disciplineMapper::toResponse)
                .toList();
    }

    @Transactional
    public DisciplineResponse updateDiscipline(Long id, DisciplineRequest request) {
        Discipline discipline = findById(id);
        disciplineMapper.updateEntity(request, discipline);
        discipline.setStudent(findStudent(request.getStudentId()));
        discipline.setTerm(findTerm(request.getTermId()));
        Discipline saved = disciplineRepository.save(discipline);
        log.info("Discipline mise à jour: {}", saved.getId());
        return disciplineMapper.toResponse(saved);
    }

    @Transactional
    public void deleteDiscipline(Long id) {
        Discipline discipline = findById(id);
        discipline.setDeletedAt(java.time.LocalDateTime.now());
        disciplineRepository.save(discipline);
        log.info("Discipline supprimée (soft): {}", id);
    }

    public Discipline findById(Long id) {
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discipline non trouvée avec l'ID: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }

    private Term findTerm(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
    }
}
