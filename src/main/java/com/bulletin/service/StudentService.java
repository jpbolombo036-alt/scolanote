package com.bulletin.service;

import com.bulletin.dto.people.StudentRequest;
import com.bulletin.dto.people.StudentResponse;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.StudentMapper;
import com.bulletin.repository.StudentRepository;
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

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        Student saved = studentRepository.save(student);
        log.info("Élève créé: {}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return studentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable)
                .map(studentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
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
