package com.bulletin.service;

import com.bulletin.dto.curriculum.TeachingAssignmentRequest;
import com.bulletin.dto.curriculum.TeachingAssignmentResponse;
import com.bulletin.entity.Classroom;
import com.bulletin.entity.Subject;
import com.bulletin.entity.Teacher;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TeachingAssignmentMapper;
import com.bulletin.repository.ClassroomRepository;
import com.bulletin.repository.SubjectRepository;
import com.bulletin.repository.TeacherRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final TeachingAssignmentMapper teachingAssignmentMapper;
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
    public TeachingAssignmentResponse createTeachingAssignment(TeachingAssignmentRequest request) {
        TeachingAssignment teachingAssignment = teachingAssignmentMapper.toEntity(request);
        teachingAssignment.setTeacher(findTeacher(request.getTeacherId()));
        teachingAssignment.setClassroom(findClassroom(request.getClassroomId()));
        teachingAssignment.setSubject(findSubject(request.getSubjectId()));
        if (teachingAssignment.getTeacher() != null && teachingAssignment.getTeacher().getSchoolId() != null) {
            teachingAssignment.setSchoolId(teachingAssignment.getTeacher().getSchoolId());
        }
        TeachingAssignment saved = teachingAssignmentRepository.save(teachingAssignment);
        log.info("Affectation créée: {}", saved.getId());
        return teachingAssignmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeachingAssignmentResponse getTeachingAssignment(Long id) {
        return teachingAssignmentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAccessibleTeachingAssignments() {
        if (isSuperAdmin()) {
            return teachingAssignmentRepository.findAll().stream()
                    .map(teachingAssignmentMapper::toResponse)
                    .toList();
        }
        return teachingAssignmentRepository.findBySchoolId(requireSchoolId()).stream()
                .map(teachingAssignmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getByTeacher(Long teacherId) {
        return teachingAssignmentRepository.findByTeacherId(teacherId).stream()
                .map(teachingAssignmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getByClassroom(Long classroomId) {
        return teachingAssignmentRepository.findByClassroomId(classroomId).stream()
                .map(teachingAssignmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getBySubject(Long subjectId) {
        return teachingAssignmentRepository.findBySubjectId(subjectId).stream()
                .map(teachingAssignmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public TeachingAssignmentResponse updateTeachingAssignment(Long id, TeachingAssignmentRequest request) {
        TeachingAssignment teachingAssignment = findById(id);
        teachingAssignmentMapper.updateEntity(request, teachingAssignment);
        teachingAssignment.setTeacher(findTeacher(request.getTeacherId()));
        teachingAssignment.setClassroom(findClassroom(request.getClassroomId()));
        teachingAssignment.setSubject(findSubject(request.getSubjectId()));
        TeachingAssignment saved = teachingAssignmentRepository.save(teachingAssignment);
        log.info("Affectation mise à jour: {}", saved.getId());
        return teachingAssignmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTeachingAssignment(Long id) {
        TeachingAssignment teachingAssignment = findById(id);
        teachingAssignment.setDeletedAt(java.time.LocalDateTime.now());
        teachingAssignmentRepository.save(teachingAssignment);
        log.info("Affectation supprimée (soft): {}", id);
    }

    public TeachingAssignment findById(Long id) {
        return teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée avec l'ID: " + id));
    }

    private Teacher findTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professeur non trouvé avec l'ID: " + id));
    }

    private Classroom findClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière non trouvée avec l'ID: " + id));
    }
}
