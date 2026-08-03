package com.bulletin.repository;

import com.bulletin.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByAssignmentId(Long assignmentId);
    List<Assessment> findByPeriodId(Long periodId);
    List<Assessment> findBySchoolId(Long schoolId);

    List<Assessment> findByAssignmentIdAndSchoolId(Long assignmentId, Long schoolId);

    List<Assessment> findByPeriodIdAndSchoolId(Long periodId, Long schoolId);

    @Query("""
            SELECT a FROM Assessment a
            WHERE a.assignment.id = :assignmentId
              AND a.assignment IS NOT NULL
              AND a.assessmentType IS NOT NULL
              AND a.period IS NOT NULL
            """)
    List<Assessment> findCompleteByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("""
            SELECT a FROM Assessment a
            WHERE a.period.id = :periodId
              AND a.assignment IS NOT NULL
              AND a.assessmentType IS NOT NULL
              AND a.period IS NOT NULL
            """)
    List<Assessment> findCompleteByPeriodId(@Param("periodId") Long periodId);

    @Query("""
            SELECT a FROM Assessment a
            WHERE a.assignment IS NOT NULL
              AND a.assessmentType IS NOT NULL
              AND a.period IS NOT NULL
            """)
    List<Assessment> findAllComplete();

    @Query("""
            SELECT a FROM Assessment a
            WHERE a.schoolId = :schoolId
              AND a.assignment IS NOT NULL
              AND a.assessmentType IS NOT NULL
              AND a.period IS NOT NULL
            """)
    List<Assessment> findCompleteBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * Charge en une seule requête toutes les évaluations d'une classe pour une période,
     * avec le type d'évaluation (pour le coefficient) et l'affectation (pour la matière).
     * Utilisé par le calcul batch des bulletins (évite le N+1).
     */
    @Query("""
            SELECT a FROM Assessment a
            LEFT JOIN FETCH a.assessmentType
            LEFT JOIN FETCH a.assignment asn
            LEFT JOIN FETCH asn.subject
            WHERE asn.classroom.id = :classroomId
              AND a.period.id = :periodId
            """)
    List<Assessment> findByClassroomIdAndPeriodIdWithDetails(
            @Param("classroomId") Long classroomId,
            @Param("periodId") Long periodId);
}
