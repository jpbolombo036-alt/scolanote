package com.bulletin.repository;

import com.bulletin.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchoolId(Long schoolId);
    Page<Subject> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);
}
