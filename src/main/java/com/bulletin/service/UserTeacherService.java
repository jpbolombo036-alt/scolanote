package com.bulletin.service;

import com.bulletin.dto.user.UserTeacherRequest;
import com.bulletin.dto.user.UserTeacherResponse;
import com.bulletin.entity.Student;
import com.bulletin.entity.Teacher;
import com.bulletin.entity.User;
import com.bulletin.entity.UserTeacher;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.UserTeacherMapper;
import com.bulletin.repository.TeacherRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserTeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTeacherService {

    private final UserTeacherRepository userTeacherRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final UserTeacherMapper userTeacherMapper;

    @Transactional
    public UserTeacherResponse createUserTeacher(UserTeacherRequest request) {
        UserTeacher userTeacher = userTeacherMapper.toEntity(request);
        userTeacher.setUser(findUser(request.getUserId()));
        userTeacher.setTeacher(findTeacher(request.getTeacherId()));
        UserTeacher saved = userTeacherRepository.save(userTeacher);
        log.info("Lien user-professeur créé: {}", saved.getId());
        return userTeacherMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserTeacherResponse> getAllUserTeachers() {
        return userTeacherRepository.findAll().stream()
                .map(userTeacherMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteUserTeacher(Long id) {
        UserTeacher userTeacher = userTeacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lien user-professeur non trouvé avec l'ID: " + id));
        userTeacherRepository.delete(userTeacher);
        log.info("Lien user-professeur supprimé: {}", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    private Teacher findTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professeur non trouvé avec l'ID: " + id));
    }
}
