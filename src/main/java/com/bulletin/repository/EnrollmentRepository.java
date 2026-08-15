package com.bulletin.repository;

import com.bulletin.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByClassroomId(Long classroomId);
    List<Enrollment> findBySchoolId(Long schoolId);
    Page<Enrollment> findBySchoolId(Long schoolId, Pageable pageable);
    List<Enrollment> findByStudentIdIn(List<Long> studentIds);

    long countByClassroomIdAndSchoolIdAndNumeroOrdre(Long classroomId, Long schoolId, Integer numeroOrdre);

    @Query("SELECT MAX(e.numeroOrdre) FROM Enrollment e WHERE e.classroom.id = :classroomId AND e.schoolId = :schoolId")
    Integer maxNumeroOrdreByClassroomIdAndSchoolId(@Param("classroomId") Long classroomId, @Param("schoolId") Long schoolId);
}
