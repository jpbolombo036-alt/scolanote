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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    ClassroomRepository classroomRepository;
    @Mock
    EnrollmentMapper enrollmentMapper;
    @Mock
    SecurityUtils securityUtils;

    AutoOrdreService autoOrdreService;
    EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        autoOrdreService = new AutoOrdreService();
        enrollmentService = new EnrollmentService(
                enrollmentRepository, studentRepository, classroomRepository,
                enrollmentMapper, securityUtils, autoOrdreService);
    }

    private static Student student(Long id, Long schoolId) {
        Student s = Student.builder().nom("Student").matricule("MAT001").schoolId(schoolId).build();
        s.setId(id);
        return s;
    }

    private static Classroom classroom(Long id) {
        Classroom c = Classroom.builder().nom("Classroom").build();
        c.setId(id);
        return c;
    }

    private static Enrollment enrollment(Long id, Student student, Classroom classroom, Long schoolId, Integer numeroOrdre) {
        Enrollment e = Enrollment.builder()
                .student(student).classroom(classroom).schoolId(schoolId)
                .numeroOrdre(numeroOrdre).etat("ACTIF").dateInscription(LocalDate.now()).build();
        e.setId(id);
        return e;
    }

    @Test
    void createEnrollment_computesNumeroOrdreServerSide() {
        Long schoolId = 1L;
        Long studentId = 10L;
        Long classroomId = 20L;

        Student student = student(studentId, schoolId);
        Classroom classroom = classroom(classroomId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.maxNumeroOrdreByClassroomIdAndSchoolId(classroomId, schoolId)).thenReturn(3);

        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(studentId).classroomId(classroomId)
                .dateInscription(LocalDate.now()).numeroOrdre(99).etat("ACTIF").build();

        Enrollment fromMapper = Enrollment.builder().etat("ACTIF").build();
        Enrollment saved = enrollment(30L, student, classroom, schoolId, 4);
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(30L).studentId(studentId).studentNom("Student").studentMatricule("MAT001")
                .classroomId(classroomId).classroomNom("Classroom").numeroOrdre(4).etat("ACTIF").build();

        when(enrollmentMapper.toEntity(request)).thenReturn(fromMapper);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.createEnrollment(request);

        assertEquals(4, result.getNumeroOrdre());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        Enrollment captured = captor.getValue();
        assertEquals(schoolId, captured.getSchoolId());
        assertEquals(student, captured.getStudent());
        assertEquals(classroom, captured.getClassroom());
        assertEquals(4, captured.getNumeroOrdre());
    }

    @Test
    void createEnrollment_setsNumeroOrdreToOneWhenScopeIsEmpty() {
        Long schoolId = 1L;
        Long studentId = 10L;
        Long classroomId = 20L;

        Student student = student(studentId, schoolId);
        Classroom classroom = classroom(classroomId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.maxNumeroOrdreByClassroomIdAndSchoolId(classroomId, schoolId)).thenReturn((Integer) null);

        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(studentId).classroomId(classroomId).etat("ACTIF").build();

        Enrollment fromMapper = Enrollment.builder().etat("ACTIF").build();
        Enrollment saved = enrollment(30L, student, classroom, schoolId, 1);
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(30L).studentId(studentId).classroomId(classroomId).numeroOrdre(1).etat("ACTIF").build();

        when(enrollmentMapper.toEntity(request)).thenReturn(fromMapper);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.createEnrollment(request);

        assertEquals(1, result.getNumeroOrdre());
    }

    @Test
    void createEnrollment_throwsWhenStudentNotFound() {
        Long studentId = 99L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        EnrollmentRequest request = EnrollmentRequest.builder().studentId(studentId).classroomId(20L).build();

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.createEnrollment(request));
    }

    @Test
    void createEnrollment_throwsWhenClassroomNotFound() {
        Long studentId = 10L;
        Long classroomId = 99L;
        Student student = student(studentId, 1L);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.empty());

        EnrollmentRequest request = EnrollmentRequest.builder().studentId(studentId).classroomId(classroomId).build();

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.createEnrollment(request));
    }

    @Test
    void updateEnrollment_preservesExistingNumeroOrdre() {
        Long schoolId = 1L;
        Long enrollmentId = 30L;
        Long studentId = 10L;
        Long classroomId = 20L;

        Student student = student(studentId, schoolId);
        Classroom classroom = classroom(classroomId);

        Enrollment existing = enrollment(enrollmentId, student, classroom, schoolId, 5);
        Enrollment saved = enrollment(enrollmentId, student, classroom, schoolId, 5);
        saved.setEtat("INACTIF");
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(enrollmentId).studentId(studentId).classroomId(classroomId)
                .numeroOrdre(5).etat("INACTIF").build();

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(existing));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        doAnswer(inv -> {
            EnrollmentRequest req = inv.getArgument(0);
            Enrollment target = inv.getArgument(1);
            target.setEtat(req.getEtat());
            return null;
        }).when(enrollmentMapper).updateEntity(any(EnrollmentRequest.class), eq(existing));
        when(enrollmentRepository.save(existing)).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(studentId).classroomId(classroomId).etat("INACTIF").build();
        EnrollmentResponse result = enrollmentService.updateEnrollment(enrollmentId, request);

        assertEquals(5, result.getNumeroOrdre());
        assertEquals(5, existing.getNumeroOrdre());
    }

    @Test
    void getAccessibleEnrollments_superAdminReturnsAll() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        Student student = student(10L, 1L);
        Classroom classroom = classroom(20L);
        Enrollment e1 = enrollment(1L, student, classroom, 1L, 1);
        Enrollment e2 = enrollment(2L, student, classroom, 2L, 2);
        EnrollmentResponse r1 = EnrollmentResponse.builder().id(1L).numeroOrdre(1).build();
        EnrollmentResponse r2 = EnrollmentResponse.builder().id(2L).numeroOrdre(2).build();

        when(enrollmentRepository.findAll()).thenReturn(List.of(e1, e2));
        when(enrollmentMapper.toResponse(e1)).thenReturn(r1);
        when(enrollmentMapper.toResponse(e2)).thenReturn(r2);

        List<EnrollmentResponse> result = enrollmentService.getAccessibleEnrollments();

        assertEquals(2, result.size());
    }

    @Test
    void getAccessibleEnrollments_nonSuperAdminFiltersBySchoolId() {
        Long schoolId = 1L;
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);

        Student student = student(10L, schoolId);
        Classroom classroom = classroom(20L);
        Enrollment e1 = enrollment(1L, student, classroom, schoolId, 1);
        EnrollmentResponse r1 = EnrollmentResponse.builder().id(1L).numeroOrdre(1).build();

        when(enrollmentRepository.findBySchoolId(schoolId)).thenReturn(List.of(e1));
        when(enrollmentMapper.toResponse(e1)).thenReturn(r1);

        List<EnrollmentResponse> result = enrollmentService.getAccessibleEnrollments();

        assertEquals(1, result.size());
        verify(enrollmentRepository).findBySchoolId(schoolId);
    }

    @Test
    void getAccessibleEnrollments_page_superAdmin() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        Student student = student(10L, 1L);
        Classroom classroom = classroom(20L);
        Enrollment e1 = enrollment(1L, student, classroom, 1L, 1);
        EnrollmentResponse r1 = EnrollmentResponse.builder().id(1L).numeroOrdre(1).build();

        Page<Enrollment> page = new PageImpl<>(List.of(e1));
        when(enrollmentRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(enrollmentMapper.toResponse(e1)).thenReturn(r1);

        Page<EnrollmentResponse> result = enrollmentService.getAccessibleEnrollments(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getEnrollmentsByStudent_returnsFiltered() {
        Long studentId = 10L;
        Student student = student(studentId, 1L);
        Classroom classroom = classroom(20L);
        Enrollment e1 = enrollment(1L, student, classroom, 1L, 1);
        Enrollment e2 = enrollment(2L, student, classroom, 1L, 2);
        EnrollmentResponse r1 = EnrollmentResponse.builder().id(1L).numeroOrdre(1).build();
        EnrollmentResponse r2 = EnrollmentResponse.builder().id(2L).numeroOrdre(2).build();

        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(e1, e2));
        when(enrollmentMapper.toResponse(e1)).thenReturn(r1);
        when(enrollmentMapper.toResponse(e2)).thenReturn(r2);

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByStudent(studentId);

        assertEquals(2, result.size());
    }

    @Test
    void getEnrollmentsByClassroom_returnsFiltered() {
        Long classroomId = 20L;
        Student student = student(10L, 1L);
        Classroom classroom = classroom(classroomId);
        Enrollment e1 = enrollment(1L, student, classroom, 1L, 1);
        EnrollmentResponse r1 = EnrollmentResponse.builder().id(1L).numeroOrdre(1).build();

        when(enrollmentRepository.findByClassroomId(classroomId)).thenReturn(List.of(e1));
        when(enrollmentMapper.toResponse(e1)).thenReturn(r1);

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByClassroom(classroomId);

        assertEquals(1, result.size());
    }

    @Test
    void getEnrollment_returnsResponseById() {
        Long enrollmentId = 30L;
        Long schoolId = 1L;
        Student student = student(10L, schoolId);
        Classroom classroom = classroom(20L);
        Enrollment enrollment = enrollment(enrollmentId, student, classroom, schoolId, 1);
        EnrollmentResponse response = EnrollmentResponse.builder().id(enrollmentId).numeroOrdre(1).build();

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.getEnrollment(enrollmentId);

        assertEquals(enrollmentId, result.getId());
    }

    @Test
    void getEnrollment_throwsWhenNotFound() {
        Long enrollmentId = 99L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.getEnrollment(enrollmentId));
    }

    @Test
    void deleteEnrollment_softDeletes() {
        Long enrollmentId = 30L;
        Long schoolId = 1L;
        Student student = student(10L, schoolId);
        Classroom classroom = classroom(20L);
        Enrollment existing = enrollment(enrollmentId, student, classroom, schoolId, 1);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(existing));

        enrollmentService.deleteEnrollment(enrollmentId);

        assertNotNull(existing.getDeletedAt());
        verify(enrollmentRepository).save(existing);
    }

    @Test
    void createEnrollment_retriesOnConflict() {
        Long schoolId = 1L;
        Long studentId = 10L;
        Long classroomId = 20L;

        Student student = student(studentId, schoolId);
        Classroom classroom = classroom(classroomId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.maxNumeroOrdreByClassroomIdAndSchoolId(classroomId, schoolId)).thenReturn((Integer) null, (Integer) null);

        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(studentId).classroomId(classroomId).etat("ACTIF").build();

        Enrollment fromMapper = Enrollment.builder().etat("ACTIF").build();
        Enrollment saved = enrollment(30L, student, classroom, schoolId, 1);
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(30L).studentId(studentId).classroomId(classroomId).numeroOrdre(1).etat("ACTIF").build();

        when(enrollmentMapper.toEntity(request)).thenReturn(fromMapper);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        AtomicInteger saveAttempts = new AtomicInteger();
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(inv -> {
                    if (saveAttempts.incrementAndGet() == 1) {
                        throw new DataIntegrityViolationException("conflict");
                    }
                    return saved;
                });

        EnrollmentResponse result = enrollmentService.createEnrollment(request);
        assertEquals(1, result.getNumeroOrdre());
        verify(enrollmentRepository, atLeast(2)).save(any(Enrollment.class));
    }
}
