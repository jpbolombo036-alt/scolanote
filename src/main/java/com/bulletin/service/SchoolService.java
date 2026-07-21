package com.bulletin.service;

import com.bulletin.dto.school.SchoolRequest;
import com.bulletin.dto.school.SchoolResponse;
import com.bulletin.entity.Role;
import com.bulletin.entity.School;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.SchoolMapper;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.SchoolRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolMapper schoolMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        School school = schoolMapper.toEntity(request);
        School saved = schoolRepository.save(school);
        log.info("École créée: {}", saved.getId());
        createSchoolAdmin(saved);
        return schoolMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SchoolResponse getSchool(Long id) {
        return schoolMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<SchoolResponse> getAllSchools(Pageable pageable) {
        return schoolRepository.findAll(pageable)
                .map(schoolMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(schoolMapper::toResponse)
                .toList();
    }

    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = findById(id);
        schoolMapper.updateEntity(request, school);
        School saved = schoolRepository.save(school);
        log.info("École mise à jour: {}", saved.getId());
        return schoolMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = findById(id);
        school.setDeletedAt(java.time.LocalDateTime.now());
        schoolRepository.save(school);
        log.info("École supprimée (soft): {}", id);
    }

    public School findById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + id));
    }

    private void createSchoolAdmin(School school) {
        Role superAdminRole = roleRepository.findByNom("SUPER_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle SUPER_ADMIN non trouvé"));

        String username = school.getEmail();
        String rawPassword = "123456";

        User admin = User.builder()
                .username(username)
                .email(school.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        log.info("Utilisateur admin créé pour l'école {}: {}", school.getId(), username);

        UserRole userRole = UserRole.builder()
                .user(admin)
                .role(superAdminRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);
        log.info("Rôle SUPER_ADMIN attribué à l'utilisateur {}", username);
    }
}
