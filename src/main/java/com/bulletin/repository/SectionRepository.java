package com.bulletin.repository;

import com.bulletin.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findBySchoolId(Long schoolId);
    Page<Section> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);
}
