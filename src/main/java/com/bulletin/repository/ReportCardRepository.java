package com.bulletin.repository;

import com.bulletin.entity.ReportCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {
    List<ReportCard> findByEnrollmentId(Long enrollmentId);
    List<ReportCard> findByTermId(Long termId);
}
