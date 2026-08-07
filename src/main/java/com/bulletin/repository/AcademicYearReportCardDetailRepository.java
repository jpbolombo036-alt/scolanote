package com.bulletin.repository;

import com.bulletin.entity.AcademicYearReportCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AcademicYearReportCardDetailRepository extends JpaRepository<AcademicYearReportCardDetail, Long> {
    List<AcademicYearReportCardDetail> findByAcademicYearReportCardId(Long academicYearReportCardId);
}
