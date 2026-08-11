package com.bulletin.repository;

import com.bulletin.entity.AcademicYearReportCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AcademicYearReportCardRepository extends JpaRepository<AcademicYearReportCard, Long> {
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "academicYear"})
    List<AcademicYearReportCard> findByEnrollmentIdAndAcademicYearId(Long enrollmentId, Long academicYearId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "academicYear"})
    List<AcademicYearReportCard> findByAcademicYearId(Long academicYearId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "academicYear"})
    Optional<AcademicYearReportCard> findByEnrollmentIdAndAcademicYearIdAndDeletedAtIsNull(Long enrollmentId, Long academicYearId);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom"})
    List<AcademicYearReportCard> findByEnrollmentId(Long enrollmentId);

    @Override
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "academicYear"})
    Page<AcademicYearReportCard> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "enrollment.classroom", "academicYear"})
    Page<AcademicYearReportCard> findBySchoolId(Long schoolId, Pageable pageable);
}
