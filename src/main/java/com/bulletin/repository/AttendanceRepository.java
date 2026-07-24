package com.bulletin.repository;

import com.bulletin.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    long countByStudentIdAndPeriodIdAndRetardFalseAndAbsenceTrue(Long studentId, Long periodId);
    long countByStudentIdAndPeriodIdAndRetardTrueAndAbsenceFalse(Long studentId, Long periodId);
    List<Attendance> findBySchoolId(Long schoolId);
    Page<Attendance> findBySchoolId(Long schoolId, Pageable pageable);
}
