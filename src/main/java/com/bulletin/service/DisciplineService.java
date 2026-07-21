package com.bulletin.service;

import com.bulletin.dto.tracking.DisciplineRequest;
import com.bulletin.dto.tracking.DisciplineResponse;
import com.bulletin.entity.Discipline;
import com.bulletin.entity.Period;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.DisciplineMapper;
import com.bulletin.repository.DisciplineRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.StudentRepository;
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
    private final PeriodRepository periodRepository;
    private final DisciplineMapper disciplineMapper;
    private final PeriodClosureService periodClosureService;

    @Transactional
    public DisciplineResponse createDiscipline(DisciplineRequest request) {
        periodClosureService.assertPeriodeOuverte(request.getPeriodId());
        Discipline discipline = disciplineMapper.toEntity(request);
        discipline.setStudent(findStudent(request.getStudentId()));
        discipline.setPeriod(findPeriod(request.getPeriodId()));
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
        return disciplineRepository.findByPeriodId(termId).stream()
                .map(disciplineMapper::toResponse)
                .toList();
    }

    @Transactional
    public DisciplineResponse updateDiscipline(Long id, DisciplineRequest request) {
        periodClosureService.assertPeriodeOuverte(request.getPeriodId());
        Discipline discipline = findById(id);
        disciplineMapper.updateEntity(request, discipline);
        discipline.setStudent(findStudent(request.getStudentId()));
        discipline.setPeriod(findPeriod(request.getPeriodId()));
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

    private Period findPeriod(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + id));
    }
}
