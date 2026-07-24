package com.bulletin.repository;

import com.bulletin.entity.Discipline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
    List<Discipline> findByStudentId(Long studentId);
    List<Discipline> findByPeriodId(Long periodId);
    Discipline findByStudentIdAndPeriodId(Long studentId, Long periodId);
    List<Discipline> findBySchoolId(Long schoolId);
    Page<Discipline> findBySchoolId(Long schoolId, Pageable pageable);
}
