package com.bulletin.repository;

import com.bulletin.entity.Trimester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TrimesterRepository extends JpaRepository<Trimester, Long> {
    List<Trimester> findByAcademicYearId(Long academicYearId);
    List<Trimester> findByAcademicYearIdIn(List<Long> academicYearIds);
    Page<Trimester> findByAcademicYearIdIn(List<Long> academicYearIds, Pageable pageable);
    List<Trimester> findBySchoolId(Long schoolId);
    Page<Trimester> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);

    long countByAcademicYearIdAndSchoolIdAndOrdre(Long academicYearId, Long schoolId, Integer ordre);

    @Query("SELECT MAX(t.ordre) FROM Trimester t WHERE t.academicYear.id = :academicYearId AND t.schoolId = :schoolId")
    Integer maxOrdreByAcademicYearIdAndSchoolId(@Param("academicYearId") Long academicYearId, @Param("schoolId") Long schoolId);
}
