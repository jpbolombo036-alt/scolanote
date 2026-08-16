package com.bulletin.service;

import com.bulletin.dto.grade.AssessmentRequest;
import com.bulletin.dto.grade.AssessmentResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.AssessmentType;
import com.bulletin.entity.Period;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.mapper.AssessmentMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Couvre les correctifs multi-tenant du module évaluation :
 *  - liste par classe/période filtrée par l'école de l'utilisateur
 *  - impossibilité de référencer un type d'évaluation d'une autre école
 */
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    AssessmentRepository assessmentRepository;
    @Mock
    TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock
    AssessmentTypeRepository assessmentTypeRepository;
    @Mock
    PeriodRepository periodRepository;
    @Mock
    AssessmentMapper assessmentMapper;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    PeriodClosureService periodClosureService;

    AssessmentService assessmentService;

    private static final Long SCHOOL_ID = 10L;
    private static final Long OTHER_SCHOOL_ID = 99L;
    private static final Long PERIOD_ID = 5L;
    private static final Long CLASSROOM_ID = 7L;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentService(assessmentRepository, teachingAssignmentRepository,
                assessmentTypeRepository, periodRepository, assessmentMapper, securityUtils,
                periodClosureService);
    }

    private Period newPeriod() {
        Period period = new Period();
        period.setId(PERIOD_ID);
        period.setSchoolId(SCHOOL_ID);
        return period;
    }

    @Test
    void getByClassroomAndPeriod_usesSchoolFilteredQuery_whenNotSuperAdmin() {
        when(periodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(newPeriod()));
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.requireSchoolId()).thenReturn(SCHOOL_ID);
        when(assessmentRepository.findByClassroomIdAndPeriodIdAndSchoolIdWithDetails(
                CLASSROOM_ID, PERIOD_ID, SCHOOL_ID)).thenReturn(List.of());

        List<AssessmentResponse> result = assessmentService.getByClassroomAndPeriod(CLASSROOM_ID, PERIOD_ID);

        assertTrue(result.isEmpty());
        verify(assessmentRepository).findByClassroomIdAndPeriodIdAndSchoolIdWithDetails(
                CLASSROOM_ID, PERIOD_ID, SCHOOL_ID);
        verify(assessmentRepository, never()).findByClassroomIdAndPeriodIdWithDetails(any(), any());
    }

    @Test
    void getByClassroomAndPeriod_usesUnfilteredQuery_whenSuperAdmin() {
        when(periodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(newPeriod()));
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        when(assessmentRepository.findByClassroomIdAndPeriodIdWithDetails(CLASSROOM_ID, PERIOD_ID))
                .thenReturn(List.of());

        List<AssessmentResponse> result = assessmentService.getByClassroomAndPeriod(CLASSROOM_ID, PERIOD_ID);

        assertTrue(result.isEmpty());
        verify(assessmentRepository).findByClassroomIdAndPeriodIdWithDetails(CLASSROOM_ID, PERIOD_ID);
        verify(assessmentRepository, never()).findByClassroomIdAndPeriodIdAndSchoolIdWithDetails(
                any(), any(), any());
    }

    @Test
    void createAssessment_throwsSecurity_whenAssessmentTypeBelongsToAnotherSchool() {
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(3L);
        assignment.setSchoolId(SCHOOL_ID);
        when(teachingAssignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(assessmentMapper.toEntity(any(AssessmentRequest.class))).thenReturn(new Assessment());

        // Le type d'évaluation appartient à une AUTRE école
        AssessmentType foreignType = new AssessmentType();
        foreignType.setId(8L);
        foreignType.setSchoolId(OTHER_SCHOOL_ID);
        when(assessmentTypeRepository.findById(8L)).thenReturn(Optional.of(foreignType));
        // Strict stubs : assertSchoolAccess est appelé avec l'école de l'utilisateur
        // (findAssignment) ET avec l'école étrangère (findAssessmentType) → stubber les deux
        doNothing().when(securityUtils).assertSchoolAccess(SCHOOL_ID);
        doThrow(new SecurityException("Accès refusé : ressource hors de votre école"))
                .when(securityUtils).assertSchoolAccess(OTHER_SCHOOL_ID);

        AssessmentRequest request = AssessmentRequest.builder()
                .assignmentId(3L)
                .assessmentTypeId(8L)
                .periodId(PERIOD_ID)
                .noteMax(new BigDecimal("20"))
                .build();

        assertThrows(SecurityException.class, () -> assessmentService.createAssessment(request));
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void setPublication_marksAssessmentAsPublished_andChecksOwnership() {
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(3L);
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setSchoolId(SCHOOL_ID);
        assessment.setAssignment(assignment);
        assessment.setPeriod(newPeriod());
        assessment.setAssessmentType(new AssessmentType());
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(assessment));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentMapper.toResponse(assessment))
                .thenReturn(AssessmentResponse.builder().id(1L).publie(true).build());

        AssessmentResponse result = assessmentService.setPublication(1L, true);

        assertTrue(result.isPublie());
        assertTrue(assessment.isPublie());
        verify(securityUtils).assertTeacherOwnsAssignment(assignment);
        verify(assessmentRepository).save(assessment);
    }
}
