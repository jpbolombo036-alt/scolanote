package com.bulletin.repository;

import com.bulletin.entity.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PeriodRepository extends JpaRepository<Period, Long> {
    List<Period> findByTrimesterId(Long trimesterId);
    List<Period> findByTrimester_AcademicYearId(Long academicYearId);
    List<Period> findByVerrouilleTrue();
    List<Period> findByVerrouilleFalse();
}
