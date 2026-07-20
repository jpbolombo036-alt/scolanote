package com.bulletin.service;

import com.bulletin.dto.people.TeacherRequest;
import com.bulletin.dto.people.TeacherResponse;
import com.bulletin.entity.Teacher;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TeacherMapper;
import com.bulletin.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    @Transactional
    public TeacherResponse createTeacher(TeacherRequest request) {
        Teacher teacher = teacherMapper.toEntity(request);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Professeur créé: {}", saved.getId());
        return teacherMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(Long id) {
        return teacherMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponse)
                .toList();
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = findById(id);
        teacherMapper.updateEntity(request, teacher);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Professeur mis à jour: {}", saved.getId());
        return teacherMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = findById(id);
        teacher.setDeletedAt(java.time.LocalDateTime.now());
        teacherRepository.save(teacher);
        log.info("Professeur supprimé (soft): {}", id);
    }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professeur non trouvé avec l'ID: " + id));
    }
}
