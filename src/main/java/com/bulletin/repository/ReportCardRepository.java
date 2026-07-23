package com.bulletin.repository;

import com.bulletin.entity.ReportCard;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {
    List<ReportCard> findByEnrollmentId(Long enrollmentId);
    List<ReportCard> findByPeriodId(Long periodId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findAll();
}
