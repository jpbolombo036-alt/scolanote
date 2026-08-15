package com.bulletin.service;

import com.bulletin.entity.AssessmentType;
import com.bulletin.mapper.AssessmentTypeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Couvre la garde d'intégrité : un type d'évaluation encore utilisé par des
 * évaluations actives ne peut pas être supprimé (HTTP 409).
 */
@ExtendWith(MockitoExtension.class)
class AssessmentTypeServiceTest {

    @Mock
    AssessmentTypeRepository assessmentTypeRepository;
    @Mock
    AssessmentTypeMapper assessmentTypeMapper;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    AssessmentRepository assessmentRepository;

    AssessmentTypeService assessmentTypeService;

    @BeforeEach
    void setUp() {
        assessmentTypeService = new AssessmentTypeService(assessmentTypeRepository,
                assessmentTypeMapper, securityUtils, assessmentRepository);
    }

    private AssessmentType newType() {
        AssessmentType type = new AssessmentType();
        type.setId(8L);
        type.setSchoolId(10L);
        type.setNom("Interrogation");
        return type;
    }

    @Test
    void deleteAssessmentType_throwsConflict_whenStillUsedByAssessments() {
        AssessmentType type = newType();
        when(assessmentTypeRepository.findById(8L)).thenReturn(Optional.of(type));
        when(assessmentRepository.countByAssessmentTypeId(8L)).thenReturn(3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> assessmentTypeService.deleteAssessmentType(8L));

        assertTrue(ex.getMessage().contains("3 évaluation(s)"));
        assertNull(type.getDeletedAt());
        verify(assessmentTypeRepository, never()).save(any(AssessmentType.class));
    }

    @Test
    void deleteAssessmentType_softDeletes_whenUnused() {
        AssessmentType type = newType();
        when(assessmentTypeRepository.findById(8L)).thenReturn(Optional.of(type));
        when(assessmentRepository.countByAssessmentTypeId(8L)).thenReturn(0L);

        assessmentTypeService.deleteAssessmentType(8L);

        assertNotNull(type.getDeletedAt());
        verify(assessmentTypeRepository).save(type);
    }
}
