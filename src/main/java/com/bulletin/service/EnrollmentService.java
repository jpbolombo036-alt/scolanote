package com.bulletin.service;

import com.bulletin.dto.people.EnrollmentRequest;
import com.bulletin.dto.people.EnrollmentResponse;
import com.bulletin.entity.Classroom;
import com.bulletin.entity.Enrollment;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.EnrollmentMapper;
import com.bulletin.repository.ClassroomRepository;
import com.bulletin.repository.EnrollmentRepository;
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
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final SecurityUtils securityUtils;
    private final AutoOrdreService autoOrdreService;

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
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        Student student = findStudent(request.getStudentId());
        Classroom classroom = findClassroom(request.getClassroomId());
        Long schoolId = student.getSchoolId();
        // Le numéro d'ordre est calculé côté serveur (max + 1) : la valeur fournie
        // par le client est ignorée. schoolId provient de l'élève (disponible avant save).
        EnrollmentResponse response = AutoOrdreRetry.retry(autoOrdreService,
                () -> enrollmentRepository.maxNumeroOrdreByClassroomIdAndSchoolId(request.getClassroomId(), schoolId),
                ordre -> {
                    Enrollment enrollment = enrollmentMapper.toEntity(request);
                    enrollment.setStudent(student);
                    enrollment.setClassroom(classroom);
                    enrollment.setNumeroOrdre(ordre);
                    enrollment.setSchoolId(schoolId);
                    Enrollment saved = enrollmentRepository.save(enrollment);
                    log.info("Inscription créée: {}", saved.getId());
                    return enrollmentMapper.toResponse(saved);
                });
        return response;
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(Long id) {
        return enrollmentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getAccessibleEnrollments(Pageable pageable) {
        if (isSuperAdmin()) {
            return enrollmentRepository.findAll(pageable)
                    .map(enrollment -> {
                        if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                            return null;
                        }
                        return enrollmentMapper.toResponse(enrollment);
                    });
        }
        return enrollmentRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(enrollment -> {
                    if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                        return null;
                    }
                    return enrollmentMapper.toResponse(enrollment);
                });
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAccessibleEnrollments() {
        if (isSuperAdmin()) {
            return enrollmentRepository.findAll().stream()
                    .map(enrollment -> {
                        if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                            return null;
                        }
                        return enrollmentMapper.toResponse(enrollment);
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return enrollmentRepository.findBySchoolId(requireSchoolId()).stream()
                .map(enrollment -> {
                    if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                        return null;
                    }
                    return enrollmentMapper.toResponse(enrollment);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollment -> {
                    if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                        return null;
                    }
                    return enrollmentMapper.toResponse(enrollment);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByClassroom(Long classroomId) {
        return enrollmentRepository.findByClassroomId(classroomId).stream()
                .map(enrollment -> {
                    if (enrollment.getStudent() == null || enrollment.getClassroom() == null) {
                        return null;
                    }
                    return enrollmentMapper.toResponse(enrollment);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, EnrollmentRequest request) {
        Enrollment enrollment = findById(id);
        Integer existingNumeroOrdre = enrollment.getNumeroOrdre();
        enrollmentMapper.updateEntity(request, enrollment);
        enrollment.setNumeroOrdre(existingNumeroOrdre); // le numéro d'ordre est immuable après création
        Student student = findStudent(request.getStudentId());
        Classroom classroom = findClassroom(request.getClassroomId());
        enrollment.setStudent(student);
        enrollment.setClassroom(classroom);
        enrollment.setSchoolId(student.getSchoolId()); // rafraîchir si l'élève change
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Inscription mise à jour: {}", saved.getId());
        return enrollmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = findById(id);
        enrollment.setDeletedAt(java.time.LocalDateTime.now());
        enrollmentRepository.save(enrollment);
        log.info("Inscription supprimée (soft): {}", id);
    }

    public Enrollment findById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID: " + id));
        securityUtils.assertSchoolAccess(enrollment.getSchoolId());
        return enrollment;
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }

    private Classroom findClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));
    }
}
