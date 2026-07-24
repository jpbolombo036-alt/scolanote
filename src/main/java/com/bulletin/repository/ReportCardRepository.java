package com.bulletin.repository;

import com.bulletin.entity.ReportCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {
    List<ReportCard> findByEnrollmentId(Long enrollmentId);
    List<ReportCard> findByPeriodId(Long periodId);
    List<ReportCard> findBySchoolId(Long schoolId);
    Page<ReportCard> findBySchoolId(Long schoolId, Pageable pageable);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findAll();

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    java.util.Optional<ReportCard> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    Page<ReportCard> findAll(Pageable pageable);
}
