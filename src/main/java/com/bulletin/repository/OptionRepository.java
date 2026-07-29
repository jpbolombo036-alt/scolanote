package com.bulletin.repository;

import com.bulletin.entity.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findBySectionId(Long sectionId);
    List<Option> findBySchoolId(Long schoolId);
    Page<Option> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);
}
