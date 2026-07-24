package com.bulletin.repository;

import com.bulletin.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByClassroomId(Long classroomId);
    List<Enrollment> findBySchoolId(Long schoolId);
    Page<Enrollment> findBySchoolId(Long schoolId, Pageable pageable);
}
