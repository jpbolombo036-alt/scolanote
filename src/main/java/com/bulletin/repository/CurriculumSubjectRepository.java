package com.bulletin.repository;

import com.bulletin.entity.CurriculumSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CurriculumSubjectRepository extends JpaRepository<CurriculumSubject, Long> {
    List<CurriculumSubject> findByCurriculumId(Long curriculumId);
    List<CurriculumSubject> findBySubjectId(Long subjectId);

    long countByCurriculumIdAndSchoolIdAndOrdre(Long curriculumId, Long schoolId, Integer ordre);

    @Query("SELECT MAX(cs.ordre) FROM CurriculumSubject cs WHERE cs.curriculum.id = :curriculumId AND cs.schoolId = :schoolId")
    Integer maxOrdreByCurriculumIdAndSchoolId(@Param("curriculumId") Long curriculumId, @Param("schoolId") Long schoolId);

    /**
     * Charge en une seule requête les coefficients de plusieurs matières.
     * Utilisé par le calcul batch des bulletins (évite le N+1).
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT cs FROM CurriculumSubject cs
            LEFT JOIN FETCH cs.subject
            WHERE cs.subject.id IN :subjectIds
              AND cs.coefficient IS NOT NULL
            """)
    List<CurriculumSubject> findCoefficientsBySubjectIds(
            @org.springframework.data.repository.query.Param("subjectIds") List<Long> subjectIds);
}
