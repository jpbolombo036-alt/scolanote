package com.bulletin.service;

import com.bulletin.dto.people.StudentRequest;
import com.bulletin.dto.people.StudentResponse;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.StudentMapper;
import com.bulletin.repository.StudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
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
    public StudentResponse createStudent(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        student.setSchoolId(requireSchoolId());
        Student saved = studentRepository.save(student);
        log.info("Élève créé: {}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return studentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAccessibleStudents(Pageable pageable) {
        if (isSuperAdmin()) {
            return studentRepository.findAll(pageable)
                    .map(studentMapper::toResponse);
        }
        return studentRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(studentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAccessibleStudents() {
        if (isSuperAdmin()) {
            return studentRepository.findAll().stream()
                    .map(studentMapper::toResponse)
                    .toList();
        }
        return studentRepository.findBySchoolId(requireSchoolId()).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findById(id);
        studentMapper.updateEntity(request, student);
        Student saved = studentRepository.save(student);
        log.info("Élève mis à jour: {}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findById(id);
        student.setDeletedAt(java.time.LocalDateTime.now());
        studentRepository.save(student);
        log.info("Élève supprimé (soft): {}", id);
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }
}
