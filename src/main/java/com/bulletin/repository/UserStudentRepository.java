package com.bulletin.repository;

import com.bulletin.entity.UserStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserStudentRepository extends JpaRepository<UserStudent, Long> {
    List<UserStudent> findBySchoolId(Long schoolId);
}
