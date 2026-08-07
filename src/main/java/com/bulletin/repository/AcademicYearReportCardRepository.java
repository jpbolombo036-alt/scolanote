package com.bulletin.repository;

import com.bulletin.entity.AcademicYearReportCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AcademicYearReportCardRepository extends JpaRepository<AcademicYearReportCard, Long> {
    List<AcademicYearReportCard> findByEnrollmentIdAndAcademicYearId(Long enrollmentId, Long academicYearId);
    List<AcademicYearReportCard> findByAcademicYearId(Long academicYearId);
    Optional<AcademicYearReportCard> findByEnrollmentIdAndAcademicYearIdAndDeletedAtIsNull(Long enrollmentId, Long academicYearId);
}
