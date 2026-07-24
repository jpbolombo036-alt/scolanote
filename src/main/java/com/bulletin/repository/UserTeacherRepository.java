package com.bulletin.repository;

import com.bulletin.entity.UserTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserTeacherRepository extends JpaRepository<UserTeacher, Long> {
    List<UserTeacher> findBySchoolId(Long schoolId);
}
