package com.bulletin.repository;

import com.bulletin.entity.TeachingAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {
    List<TeachingAssignment> findByTeacherId(Long teacherId);
    List<TeachingAssignment> findByClassroomId(Long classroomId);
    List<TeachingAssignment> findBySubjectId(Long subjectId);
    List<TeachingAssignment> findByTeacherIdAndClassroomId(Long teacherId, Long classroomId);
    List<TeachingAssignment> findBySchoolId(Long schoolId);

    /**
     * Variante paginée, utilisée par le listage côté frontend (GET /api/attributions-enseignement?page=&size=).
     */
    Page<TeachingAssignment> findBySchoolId(Long schoolId, Pageable pageable);

    /**
     * Charge en une seule requête les affectations d'une classe avec leurs matières.
     * Utilisé par le calcul batch des bulletins (évite le N+1).
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT ta FROM TeachingAssignment ta
            LEFT JOIN FETCH ta.subject
            WHERE ta.classroom.id = :classroomId
            """)
    List<TeachingAssignment> findByClassroomIdWithSubject(
            @org.springframework.data.repository.query.Param("classroomId") Long classroomId);
}
