package com.bulletin.service;

import com.bulletin.dto.school.SchoolCreationResponse;
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
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final SecurityUtils securityUtils;

    @Transactional
    public SchoolCreationResponse createSchool(SchoolRequest request) {
        School school = schoolMapper.toEntity(request);
        School saved = schoolRepository.save(school);
        log.info("École créée: {}", saved.getId());

        String adminUsername = "admin@" + saved.getCode();
        String adminPassword = generateSecurePassword();
        User admin = User.builder()
                .username(adminUsername)
                .email(saved.getEmail())
                .password(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .schoolId(saved.getId())
                .passwordResetRequired(false)
                .build();
        admin = userRepository.save(admin);

        Role adminRole = roleRepository.findByNom("ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle ADMIN non trouvé"));
        userRoleRepository.save(UserRole.builder()
                .user(admin)
                .role(adminRole)
                .build());

        log.info("Admin automatique créé pour l'école {}: {}", saved.getId(), adminUsername);
        return SchoolCreationResponse.builder()
                .school(schoolMapper.toResponse(saved))
                .adminUsername(adminUsername)
                .adminPassword(adminPassword)
                .build();
    }

    @Transactional(readOnly = true)
    public SchoolResponse getSchool(Long id) {
        return schoolMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<SchoolResponse> getAccessibleSchools(Pageable pageable) {
        if (isSuperAdmin()) {
            return schoolRepository.findAll(pageable)
                    .map(schoolMapper::toResponse);
        }

        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            return Page.empty();
        }

        List<SchoolResponse> filtered = schoolRepository.findAll().stream()
                .filter(school -> schoolId.equals(school.getId()))
                .map(schoolMapper::toResponse)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<SchoolResponse> pageContent = start > end ? List.of() : filtered.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllAccessibleSchools() {
        if (isSuperAdmin()) {
            return schoolRepository.findAll().stream()
                    .map(schoolMapper::toResponse)
                    .toList();
        }

        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            return List.of();
        }

        return schoolRepository.findAll().stream()
                .filter(school -> schoolId.equals(school.getId()))
                .map(schoolMapper::toResponse)
                .toList();
    }

    private boolean isSuperAdmin() {
        return securityUtils.isSuperAdmin();
    }

    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = findById(id);
        schoolMapper.updateEntity(request, school);
        School saved = schoolRepository.save(school);
        log.info("École mise à jour: {}", saved.getId());
        return schoolMapper.toResponse(saved);
    }

    private String generateSecurePassword() {
        return UUID.randomUUID().toString().replaceAll("[-_]{1}", "");
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = findById(id);
        school.setDeletedAt(java.time.LocalDateTime.now());
        schoolRepository.save(school);
        log.info("École supprimée (soft): {}", id);
    }

    public School findById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + id));
        if (!isSuperAdmin()) {
            Long schoolId = securityUtils.getCurrentSchoolId();
            if (schoolId == null || !schoolId.equals(school.getId())) {
                throw new SecurityException("Accès refusé : école hors de votre périmètre");
            }
        }
        return school;
    }

}
