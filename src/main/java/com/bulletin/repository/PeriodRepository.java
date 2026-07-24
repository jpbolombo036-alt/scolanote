package com.bulletin.repository;

import com.bulletin.entity.Period;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PeriodRepository extends JpaRepository<Period, Long> {
    List<Period> findByTrimesterId(Long trimesterId);
    List<Period> findByTrimester_AcademicYearId(Long academicYearId);
    List<Period> findByTrimester_AcademicYearIdIn(List<Long> academicYearIds);
    Page<Period> findByTrimester_AcademicYearIdIn(List<Long> academicYearIds, Pageable pageable);
    List<Period> findByVerrouilleTrue();
    List<Period> findByVerrouilleFalse();
}
