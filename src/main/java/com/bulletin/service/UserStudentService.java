package com.bulletin.service;

import com.bulletin.dto.user.UserStudentRequest;
import com.bulletin.dto.user.UserStudentResponse;
import com.bulletin.entity.Student;
import com.bulletin.entity.User;
import com.bulletin.entity.UserStudent;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.UserStudentMapper;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStudentService {

    private final UserStudentRepository userStudentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final UserStudentMapper userStudentMapper;

    @Transactional
    public UserStudentResponse createUserStudent(UserStudentRequest request) {
        UserStudent userStudent = userStudentMapper.toEntity(request);
        userStudent.setUser(findUser(request.getUserId()));
        userStudent.setStudent(findStudent(request.getStudentId()));
        UserStudent saved = userStudentRepository.save(userStudent);
        log.info("Lien user-élève créé: {}", saved.getId());
        return userStudentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserStudentResponse> getAllUserStudents() {
        return userStudentRepository.findAll().stream()
                .map(userStudent -> {
                    if (userStudent.getUser() == null || userStudent.getStudent() == null) {
                        return null;
                    }
                    return userStudentMapper.toResponse(userStudent);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public void deleteUserStudent(Long id) {
        UserStudent userStudent = userStudentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lien user-élève non trouvé avec l'ID: " + id));
        userStudentRepository.delete(userStudent);
        log.info("Lien user-élève supprimé: {}", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }
}
