package com.bulletin.repository;

import com.bulletin.entity.ReportCard;
import com.bulletin.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findByEnrollmentId(Long enrollmentId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findByPeriodId(Long periodId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findBySchoolId(Long schoolId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    Page<ReportCard> findBySchoolId(Long schoolId, Pageable pageable);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findByEnrollmentIn(List<Enrollment> enrollments);


    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    List<ReportCard> findAll();

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    java.util.Optional<ReportCard> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "enrollment.classroom.academicYear", "enrollment.classroom.academicYear.school", "period"})
    Page<ReportCard> findAll(Pageable pageable);
}
