package com.bulletin.repository;

import com.bulletin.entity.UserStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStudentRepository extends JpaRepository<UserStudent, Long> {
}
