package com.bulletin.repository;

import com.bulletin.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByAssessmentId(Long assessmentId);
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findBySchoolId(Long schoolId);
    Page<Grade> findBySchoolId(Long schoolId, Pageable pageable);

    List<Grade> findByAssessmentIdAndSchoolId(Long assessmentId, Long schoolId);

    /**
     * Recherche la/les note(s) ACTIVE(S) d'un élève pour une évaluation donnée.
     * Utilisée pour garantir la règle « 1 élève = 1 note par évaluation ».
     * Retourne une liste (et non Optional) par robustesse face aux éventuels
     * doublons historiques ; les notes soft-deletées sont exclues par le filtre
     * global {@code deleted_at IS NULL}, donc une re-saisie après suppression
     * reste possible.
     */
    List<Grade> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);

    List<Grade> findByStudentIdAndSchoolId(Long studentId, Long schoolId);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.assessment.id = :assessmentId
              AND g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    List<Grade> findCompleteByAssessmentId(@Param("assessmentId") Long assessmentId);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.student.id = :studentId
              AND g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    List<Grade> findCompleteByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    Page<Grade> findAllComplete(Pageable pageable);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.schoolId = :schoolId
              AND g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    Page<Grade> findCompleteBySchoolId(@Param("schoolId") Long schoolId, Pageable pageable);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    List<Grade> findAllComplete();

    @Query("""
            SELECT g FROM Grade g
            WHERE g.schoolId = :schoolId
              AND g.student IS NOT NULL
              AND g.assessment IS NOT NULL
            """)
    List<Grade> findCompleteBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * Charge en une seule requête toutes les notes d'une classe pour une période,
     * avec l'élève et l'évaluation (via son affectation -> matière).
     * Utilisé par le calcul batch des bulletins (évite le N+1).
     */
    @Query("""
            SELECT g FROM Grade g
            LEFT JOIN FETCH g.student
            LEFT JOIN FETCH g.assessment a
            LEFT JOIN FETCH a.assignment asn
            WHERE asn.classroom.id = :classroomId
              AND a.period.id = :periodId
            """)
    List<Grade> findByClassroomIdAndPeriodId(
            @Param("classroomId") Long classroomId,
            @Param("periodId") Long periodId);

    /**
     * Paires distinctes (assessment_id, student_id) des notes d'une période.
     * Utilisé par la validation de clôture : comptage exact, insensible aux
     * éventuels doublons, sans charger les entités complètes en mémoire.
     */
    @Query("""
            SELECT DISTINCT g.assessment.id, g.student.id
            FROM Grade g
            WHERE g.assessment.period.id = :periodId
            """)
    List<Object[]> findDistinctAssessmentStudentPairsByPeriodId(@Param("periodId") Long periodId);
}
