package com.bulletin.repository;

import com.bulletin.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findBySchoolId(Long schoolId);
    Page<Level> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);
}
